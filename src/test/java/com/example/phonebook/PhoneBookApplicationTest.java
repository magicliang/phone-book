package com.example.phonebook;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class PhoneBookApplicationTest {

    @Test
    void contextLoads() {
        // 测试Spring上下文是否能正常加载
    }
}