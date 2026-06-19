package com.dairyfarm.milk.config;

import com.dairyfarm.milk.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AuthService authService;

    @Override
    public void run(String... args) throws Exception {
        log.info("开始初始化系统默认用户...");
        try {
            authService.initDefaultUsers();
            log.info("系统默认用户初始化完成");
        } catch (Exception e) {
            log.warn("初始化用户失败（可能已存在）: {}", e.getMessage());
        }
    }
}
