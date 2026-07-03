package cloud.techotakus.invoice.authorization;

import cloud.techotakus.invoice.authorization.config.AuthProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@MapperScan("cloud.techotakus.invoice.authorization.infra.mapper")
@EnableConfigurationProperties(AuthProperties.class)
@SpringBootApplication
public class InvoiceAuthorizationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(InvoiceAuthorizationServiceApplication.class, args);
    }
}
