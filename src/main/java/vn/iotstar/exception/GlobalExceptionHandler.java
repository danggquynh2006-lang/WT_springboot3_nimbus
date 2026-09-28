package vn.iotstar.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(Exception.class)
	public ProblemDetail handleSecurityException(Exception exception) {
		ProblemDetail errorDetail;

		if (exception instanceof BadCredentialsException) {
			errorDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, exception.getMessage());
			errorDetail.setProperty("description", "The username or password is incorrect");
			return errorDetail;
		}
		if (exception instanceof AccountStatusException) {
			errorDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, exception.getMessage());
			errorDetail.setProperty("description", "The account is locked");
			return errorDetail;
		}
		if (exception instanceof AccessDeniedException) {
			errorDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, exception.getMessage());
			errorDetail.setProperty("description", "You are not authorized to access this resource");
			return errorDetail;
		}
		if (exception instanceof InvalidJwtException) {
			errorDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, exception.getMessage());
			errorDetail.setProperty("description", "The JWT is invalid (bad format or bad signature)");
			return errorDetail;
		}
		if (exception instanceof JwtExpiredException) {
			errorDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, exception.getMessage());
			errorDetail.setProperty("description", "The JWT token has expired");
			return errorDetail;
		}
		// ResponseStatusException, NoResourceFoundException, 405, ...
		if (exception instanceof ErrorResponse errorResponse) {
			return errorResponse.getBody();
		}

		log.error("Unhandled exception", exception);
		errorDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
		errorDetail.setProperty("description", "Unknown internal server error.");
		return errorDetail;
	}
}
