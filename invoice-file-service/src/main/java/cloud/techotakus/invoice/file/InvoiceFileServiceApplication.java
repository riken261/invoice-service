package cloud.techotakus.invoice.file;

import cloud.techotakus.common.pojo.feign.FeignClientDefaultConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@MapperScan("cloud.techotakus.invoice.file.infra.mapper")
@EnableFeignClients(defaultConfiguration = FeignClientDefaultConfiguration.class)
@SpringBootApplication
public class InvoiceFileServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(InvoiceFileServiceApplication.class, args);
    }
}
