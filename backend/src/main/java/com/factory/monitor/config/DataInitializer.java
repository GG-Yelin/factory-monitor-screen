package com.factory.monitor.config;

import com.factory.monitor.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 数据初始化器
 */
@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {

    private final AuthService authService;

    public DataInitializer(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void run(String... args) {
        log.info("开始初始化系统数据...");

        // 初始化管理员账号
        authService.initAdminUser();

        log.info("系统数据初始化完成");
    }
}
