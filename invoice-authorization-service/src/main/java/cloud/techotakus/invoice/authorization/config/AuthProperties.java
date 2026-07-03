package cloud.techotakus.invoice.authorization.config;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "invoice.authorization.auth")
public class AuthProperties {

    private final Keycloak keycloak = new Keycloak();
    private final LoginState loginState = new LoginState();
    private final Session session = new Session();

    @Getter
    @Setter
    public static class Keycloak {
        private String issuerBaseUri;
        private String realm = "invoice";
        private String clientId;
        private String clientSecret;
        private String callbackUri;
        private String redirectUrl;
    }

    @Getter
    @Setter
    public static class LoginState {
        private Duration expiresIn = Duration.ofMinutes(5);
        private String redisKeyPrefix = "invoice:authorization:login-state:";
    }

    @Getter
    @Setter
    public static class Session {
        private String cookieName = "INVOICE_BFF_SESSION";
        private Duration expiresIn = Duration.ofHours(8);
        private boolean secure = true;
        private String sameSite = "Lax";
        private String redisKeyPrefix = "invoice:gateway:bff-session:";
    }
}
