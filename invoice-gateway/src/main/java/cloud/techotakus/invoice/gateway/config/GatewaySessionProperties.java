package cloud.techotakus.invoice.gateway.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "invoice.gateway.session")
public class GatewaySessionProperties {

    private String cookieName = "INVOICE_BFF_SESSION";
    private String redisKeyPrefix = "invoice:gateway:bff-session:";
    private List<String> publicBffSuffixes = List.of(
        "/bff/v1/auth/login-url",
        "/bff/v1/auth/callback",
        "/bff/v1/auth/logout"
    );

    public String getCookieName() {
        return cookieName;
    }

    public void setCookieName(String cookieName) {
        this.cookieName = cookieName;
    }

    public String getRedisKeyPrefix() {
        return redisKeyPrefix;
    }

    public void setRedisKeyPrefix(String redisKeyPrefix) {
        this.redisKeyPrefix = redisKeyPrefix;
    }

    public List<String> getPublicBffSuffixes() {
        return publicBffSuffixes;
    }

    public void setPublicBffSuffixes(List<String> publicBffSuffixes) {
        this.publicBffSuffixes = publicBffSuffixes;
    }
}
