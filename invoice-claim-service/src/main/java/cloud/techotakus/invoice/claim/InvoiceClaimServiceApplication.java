package cloud.techotakus.invoice.claim;

import cloud.techotakus.common.pojo.feign.FeignClientDefaultConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@MapperScan("cloud.techotakus.invoice.claim.infra.mapper")
@EnableFeignClients(defaultConfiguration = FeignClientDefaultConfiguration.class)
@SpringBootApplication
public class InvoiceClaimServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(InvoiceClaimServiceApplication.class, args);
    }
}
