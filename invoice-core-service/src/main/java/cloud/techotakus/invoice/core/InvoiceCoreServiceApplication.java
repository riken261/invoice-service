package cloud.techotakus.invoice.core;

import cloud.techotakus.common.pojo.feign.FeignClientDefaultConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

@MapperScan("cloud.techotakus.invoice.core.infra.mapper")
@EnableAsync
@EnableFeignClients(defaultConfiguration = FeignClientDefaultConfiguration.class)
@SpringBootApplication
public class InvoiceCoreServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(InvoiceCoreServiceApplication.class, args);
    }
}
