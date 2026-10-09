package com.portfolio.scheduler;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DistributedTaskSchedulerApplication {
    public static void main(String[] args) {
        // Keep JDBC startup parameters and persisted instants consistent across host locales.
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(DistributedTaskSchedulerApplication.class, args);
    }
}
