package com.portfolio.scheduler.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExecutionConfiguration {
    @Bean(destroyMethod = "close")
    ExecutorService taskExecutorService() { return Executors.newVirtualThreadPerTaskExecutor(); }
}
