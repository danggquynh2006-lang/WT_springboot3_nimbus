package vn.iotstar.services;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import jakarta.annotation.PostConstruct;
import vn.iotstar.exception.InvalidJwtException;
import vn.iotstar.exception.JwtExpiredException;

/**
 * Tao / xac thuc JWT bang thu vien Nimbus JOSE + JWT (thuat toan HS256).
 */
@Service
public class JwtService {

	@Value("${security.jwt.secret-key}")
	private String secretKey;

	@Value("${security.jwt.expiration-time}")
	private long jwtExpiration;

	@PostConstruct
	void checkSecret() {
		// HS256 yeu cau khoa >= 256 bit (32 byte)
		if (secretKey == null || secretKey.getBytes(StandardCharsets.UTF_8).length < 32) {
			throw new IllegalStateException("security.jwt.secret-key must be at least 32 bytes for HS256");
		}
	}

	// ------------------------------------------------------------------ TAO TOKEN

	public String generateToken(UserDetails userDetails) {
		return generateToken(new HashMap<>(), userDetails);
	}

	public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
		return buildToken(extraClaims, userDetails, jwtExpiration);
	}

	public long getExpirationTime() {
		return jwtExpiration;
	}

	private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
		try {
			Date now = new Date();
			Date expiryTime = new Date(now.getTime() + expiration);

			// PAYLOAD: registered claims (sub, iat, exp, jti) + private claims
			JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder()
					.subject(userDetails.getUsername())
					.issueTime(now)
					.expirationTime(expiryTime)
					.jwtID(UUID.randomUUID().toString());
			extraClaims.forEach(claimsBuilder::claim);

			// HEADER: {"alg":"HS256","typ":"JWT"}
			JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.HS256).type(JOSEObjectType.JWT).build();

			SignedJWT signedJWT = new SignedJWT(header, claimsBuilder.build());

			// SIGNATURE: HMAC-SHA256(base64url(header) + "." + base64url(payload), secret)
			signedJWT.sign(new MACSigner(getSecretBytes()));

			return signedJWT.serialize(); // header.payload.signature
		} catch (JOSEException e) {
			throw new IllegalStateException("Cannot sign JWT", e);
		}
	}

	// ---------------------------------------------------------------- DOC TOKEN

	public String extractUsername(String token) {
		return extractClaim(token, JWTClaimsSet::getSubject);
	}

	public <T> T extractClaim(String token, Function<JWTClaimsSet, T> claimsResolver) {
		return claimsResolver.apply(extractAllClaims(token));
	}

	public boolean isTokenValid(String token, UserDetails userDetails) {
		final String username = extractUsername(token);
		return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
	}

	private boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}

	private Date extractExpiration(String token) {
		return extractClaim(token, JWTClaimsSet::getExpirationTime);
	}

	/**
	 * Parse + verify chu ky + kiem tra het han.
	 *
	 * @throws InvalidJwtException sai dinh dang / sai chu ky / sai thuat toan
	 * @throws JwtExpiredException token het han
	 */
	private JWTClaimsSet extractAllClaims(String token) {
		try {
			SignedJWT signedJWT = SignedJWT.parse(token);

			// Chong tan cong "alg": "none" / doi thuat toan
			if (!JWSAlgorithm.HS256.equals(signedJWT.getHeader().getAlgorithm())) {
				throw new InvalidJwtException("Unsupported JWT algorithm");
			}

			if (!signedJWT.verify(new MACVerifier(getSecretBytes()))) {
				throw new InvalidJwtException("The JWT signature is invalid");
			}

			JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

			Date exp = claims.getExpirationTime();
			if (exp == null || exp.before(new Date())) {
				throw new JwtExpiredException("The JWT token has expired");
			}
			return claims;
		} catch (ParseException | JOSEException e) {
			throw new InvalidJwtException("Invalid JWT token: " + e.getMessage(), e);
		}
	}

	private byte[] getSecretBytes() {
		return secretKey.getBytes(StandardCharsets.UTF_8);
	}
}
