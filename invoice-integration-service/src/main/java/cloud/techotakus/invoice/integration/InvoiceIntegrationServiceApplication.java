package cloud.techotakus.invoice.integration;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("cloud.techotakus.invoice.integration.infra.provider")
@SpringBootApplication
public class InvoiceIntegrationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(InvoiceIntegrationServiceApplication.class, args);
    }
}
