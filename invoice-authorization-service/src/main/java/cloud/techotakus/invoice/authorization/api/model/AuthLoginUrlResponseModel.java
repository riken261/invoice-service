package cloud.techotakus.invoice.authorization.api.model;

public record AuthLoginUrlResponseModel(
    String loginUrl,
    long expiresInSeconds
) {
}
