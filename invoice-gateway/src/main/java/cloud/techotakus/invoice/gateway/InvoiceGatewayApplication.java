package cloud.techotakus.invoice.gateway;

import cloud.techotakus.invoice.gateway.config.GatewaySessionProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import reactor.core.publisher.Hooks;

@EnableConfigurationProperties(GatewaySessionProperties.class)
@SpringBootApplication
public class InvoiceGatewayApplication {

    public static void main(String[] args) {
        Hooks.enableAutomaticContextPropagation();
        SpringApplication.run(InvoiceGatewayApplication.class, args);
    }
}
