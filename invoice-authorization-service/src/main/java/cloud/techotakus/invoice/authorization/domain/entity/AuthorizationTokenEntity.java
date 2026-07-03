package cloud.techotakus.invoice.authorization.domain.entity;

public record AuthorizationTokenEntity(
    String accessToken,
    String refreshToken,
    String idToken,
    long expiresIn
) {
}
