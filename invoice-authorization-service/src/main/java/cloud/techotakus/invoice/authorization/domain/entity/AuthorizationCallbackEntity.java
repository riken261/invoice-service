package cloud.techotakus.invoice.authorization.domain.entity;

public record AuthorizationCallbackEntity(
    String redirectUrl,
    AuthorizationBffSessionEntity session
) {
}
