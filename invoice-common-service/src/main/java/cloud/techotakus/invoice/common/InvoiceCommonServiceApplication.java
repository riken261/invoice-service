package cloud.techotakus.invoice.common;

import cloud.techotakus.common.pojo.feign.FeignClientDefaultConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@MapperScan("cloud.techotakus.invoice.common.infra.mapper")
@EnableFeignClients(defaultConfiguration = FeignClientDefaultConfiguration.class)
@SpringBootApplication
public class InvoiceCommonServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(InvoiceCommonServiceApplication.class, args);
    }
}
