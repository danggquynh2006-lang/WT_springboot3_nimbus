# JWT với Spring Boot 3 – Security 6 (dùng thư viện Nimbus JOSE + JWT)

Bài tập: làm lại ví dụ trong bài giảng JWT, **thay thư viện jjwt bằng Nimbus (`com.nimbusds:nimbus-jose-jwt`)**.

## Yêu cầu
- JDK 17+, Maven, MySQL (chạy ở `localhost:3306`)
- Sửa `spring.datasource.password` trong `src/main/resources/application.properties` cho đúng mật khẩu MySQL của bạn (database `jwt_springboot3` sẽ tự tạo).

## Chạy
```bash
mvn spring-boot:run
```
Ứng dụng chạy ở http://localhost:8005

## Thay đổi so với slide (jjwt → Nimbus)
| jjwt (slide) | Nimbus (bài này) |
|---|---|
| 3 dependency `jjwt-api/impl/jackson` | 1 dependency `nimbus-jose-jwt` |
| `Jwts.builder()...compact()` | `JWTClaimsSet.Builder` + `SignedJWT` + `serialize()` |
| `.signWith(key, Jwts.SIG.HS256)` | `signedJWT.sign(new MACSigner(secret))` |
| `Jwts.parser().verifyWith(key).build().parseSignedClaims(t)` | `SignedJWT.parse(t)` + `verify(new MACVerifier(secret))` |
| `ExpiredJwtException`, `SignatureException` | tự định nghĩa `JwtExpiredException`, `InvalidJwtException` |

Code JWT nằm hết trong `services/JwtService.java`.

## Test bằng Postman
1. `POST http://localhost:8005/auth/signup`
```json
{ "email": "trungnh@hcmute.edu.vn", "password": "123456", "fullName": "Nguyễn Hữu Trung" }
```
2. `POST http://localhost:8005/auth/login` (cùng email/password) → nhận `token` + `expiresIn`
3. `GET http://localhost:8005/users/me` và `GET http://localhost:8005/users`
   - Không có token → **401**
   - Authorization → Bearer Token → dán token → **200**
4. Token sai (ví dụ `dfdfdf`) → **401** (`InvalidJwtException`); token hết hạn → **401** (`JwtExpiredException`)

## Test giao diện (Render in Ajax)
- Mở http://localhost:8005/login → đăng nhập → chuyển sang `/user/profile`, token được lưu trong `localStorage`, trang gọi `/users/me` với header `Authorization: Bearer <token>`.

## Đẩy lên GitHub
```bash
git init
git add .
git commit -m "JWT Spring Boot 3 - Nimbus"
git branch -M main
git remote add origin https://github.com/<username>/<ten-repo>.git
git push -u origin main
```
