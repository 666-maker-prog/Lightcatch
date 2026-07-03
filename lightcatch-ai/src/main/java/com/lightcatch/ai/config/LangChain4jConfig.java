package com.lightcatch.ai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class LangChain4jConfig {

    @Bean
    public ExecutorService sseExecutorService() {
        return Executors.newFixedThreadPool(10);
    }
}
