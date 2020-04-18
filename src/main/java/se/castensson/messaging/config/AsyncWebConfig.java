package se.castensson.messaging.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ComponentScan
public class AsyncWebConfig implements WebMvcConfigurer {

    @Value("${async.threadpool.core-pool-size}")
    private int corePoolSize;

    @Value("${async.threadpool.max-pool-size}")
    private int maxPoolSize;

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setThreadNamePrefix("MessageProcessing-");
        executor.initialize();
        configurer.setTaskExecutor(executor);
    }
}

