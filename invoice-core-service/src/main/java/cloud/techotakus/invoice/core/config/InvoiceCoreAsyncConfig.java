package cloud.techotakus.invoice.core.config;

import org.springframework.boot.task.ThreadPoolTaskExecutorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.TaskDecorator;
import org.springframework.core.task.support.ContextPropagatingTaskDecorator;

@Configuration
public class InvoiceCoreAsyncConfig {

    @Bean
    public TaskDecorator taskDecorator() {
        return new ContextPropagatingTaskDecorator();
    }

    @Bean
    public AsyncTaskExecutor applicationTaskExecutor(
            ThreadPoolTaskExecutorBuilder builder,
            TaskDecorator taskDecorator
    ) {
        return builder
                .threadNamePrefix("invoice-core-async-")
                .taskDecorator(taskDecorator)
                .build();
    }
}
