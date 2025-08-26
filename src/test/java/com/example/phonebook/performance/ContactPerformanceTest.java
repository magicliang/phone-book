package com.example.phonebook.performance;

import com.example.phonebook.config.TestConfig;
import com.example.phonebook.dto.ContactDTO;
import com.example.phonebook.repository.ContactRepository;
import com.example.phonebook.service.ContactService;
import com.example.phonebook.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(classes = TestConfig.class)
@DisplayName("联系人性能测试")
public class ContactPerformanceTest {

    @Autowired
    private ContactService contactService;

    @Autowired
    private ContactRepository contactRepository;

    private static final int BATCH_SIZE = 100;
    private static final int LARGE_BATCH_SIZE = 1000;

    @BeforeEach
    void setUp() {
        contactRepository.deleteAll();
    }

    @Test
    @DisplayName("批量创建联系人性能测试")
    void batchCreatePerformanceTest() {
        StopWatch stopWatch = new StopWatch("批量创建性能测试");

        // 准备测试数据
        List<ContactDTO> contacts = new ArrayList<>();
        for (int i = 0; i < BATCH_SIZE; i++) {
            ContactDTO contact = new ContactDTO();
            contact.setName("性能测试联系人" + i);
            contact.setPhoneNumber("1380013800" + String.format("%02d", i));
            contact.setEmail("perf" + i + "@test.com");
            contact.setCategory("性能测试");
            contacts.add(contact);
        }

        // 执行批量创建并测量时间
        stopWatch.start("批量创建" + BATCH_SIZE + "个联系人");
        List<ContactDTO> createdContacts = new ArrayList<>();
        for (ContactDTO contact : contacts) {
            ContactDTO created = contactService.createContact(contact);
            createdContacts.add(created);
        }
        stopWatch.stop();

        // 验证结果
        assertThat(createdContacts).hasSize(BATCH_SIZE);
        
        // 输出性能统计
        System.out.println(stopWatch.prettyPrint());
        
        // 性能断言 - 批量创建应在合理时间内完成
        long totalTimeMillis = stopWatch.getTotalTimeMillis();
        assertThat(totalTimeMillis).isLessThan(30000); // 30秒内完成
        
        System.out.println("批量创建" + BATCH_SIZE + "个联系人耗时: " + totalTimeMillis + "ms");
        System.out.println("平均每个联系人创建耗时: " + (totalTimeMillis / BATCH_SIZE) + "ms");
    }

    @Test
    @DisplayName("批量查询性能测试")
    void batchQueryPerformanceTest() {
        // 先创建测试数据
        List<ContactDTO> testContacts = new ArrayList<>();
        for (int i = 0; i < BATCH_SIZE; i++) {
            ContactDTO contact = new ContactDTO();
            contact.setName("查询测试" + i);
            contact.setPhoneNumber("1380013801" + String.format("%02d", i));
            contact.setEmail("query" + i + "@test.com");
            contact.setCategory("查询测试");
            
            ContactDTO created = contactService.createContact(contact);
            testContacts.add(created);
        }

        StopWatch stopWatch = new StopWatch("批量查询性能测试");

        // 测试查询所有联系人
        stopWatch.start("查询所有联系人");
        List<ContactDTO> allContacts = contactService.getAllContacts();
        stopWatch.stop();

        // 测试按分类查询
        stopWatch.start("按分类查询联系人");
        List<ContactDTO> categoryContacts = contactService.getContactsByCategory("查询测试");
        stopWatch.stop();

        // 测试模糊搜索
        stopWatch.start("模糊搜索联系人");
        List<ContactDTO> searchResults = contactService.searchContacts("查询测试");
        stopWatch.stop();

        // 验证结果
        assertThat(allContacts).hasSizeGreaterThanOrEqualTo(BATCH_SIZE);
        assertThat(categoryContacts).hasSize(BATCH_SIZE);
        assertThat(searchResults).hasSize(BATCH_SIZE);

        // 输出性能统计
        System.out.println(stopWatch.prettyPrint());
        
        // 性能断言
        long totalTimeMillis = stopWatch.getTotalTimeMillis();
        assertThat(totalTimeMillis).isLessThan(5000); // 5秒内完成所有查询
        
        System.out.println("批量查询操作总耗时: " + totalTimeMillis + "ms");
    }

    @Test
    @DisplayName("批量更新性能测试")
    void batchUpdatePerformanceTest() {
        // 先创建测试数据
        List<ContactDTO> testContacts = new ArrayList<>();
        for (int i = 0; i < BATCH_SIZE; i++) {
            ContactDTO contact = new ContactDTO();
            contact.setName("更新测试" + i);
            contact.setPhoneNumber("1380013802" + String.format("%02d", i));
            contact.setEmail("update" + i + "@test.com");
            contact.setCategory("更新测试");
            
            ContactDTO created = contactService.createContact(contact);
            testContacts.add(created);
        }

        StopWatch stopWatch = new StopWatch("批量更新性能测试");

        // 执行批量更新
        stopWatch.start("批量更新" + BATCH_SIZE + "个联系人");
        List<ContactDTO> updatedContacts = new ArrayList<>();
        for (ContactDTO contact : testContacts) {
            contact.setName("已更新-" + contact.getName());
            contact.setCategory("已更新分类");
            ContactDTO updated = contactService.updateContact(contact.getId(), contact);
            updatedContacts.add(updated);
        }
        stopWatch.stop();

        // 验证结果
        assertThat(updatedContacts).hasSize(BATCH_SIZE);
        for (ContactDTO contact : updatedContacts) {
            assertThat(contact.getName()).startsWith("已更新-");
            assertThat(contact.getCategory()).isEqualTo("已更新分类");
        }

        // 输出性能统计
        System.out.println(stopWatch.prettyPrint());
        
        // 性能断言
        long totalTimeMillis = stopWatch.getTotalTimeMillis();
        assertThat(totalTimeMillis).isLessThan(30000); // 30秒内完成
        
        System.out.println("批量更新" + BATCH_SIZE + "个联系人耗时: " + totalTimeMillis + "ms");
        System.out.println("平均每个联系人更新耗时: " + (totalTimeMillis / BATCH_SIZE) + "ms");
    }

    @Test
    @DisplayName("并发操作性能测试")
    void concurrentOperationsPerformanceTest() throws Exception {
        // 先创建基础数据（在单独的事务中）
        createTestDataForConcurrentTest();
        
        // 验证数据已创建
        List<ContactDTO> allContactsBeforeTest = contactService.getAllContacts();
        System.out.println("测试前联系人数量: " + allContactsBeforeTest.size());
        assertThat(allContactsBeforeTest).hasSizeGreaterThanOrEqualTo(50);

        StopWatch stopWatch = new StopWatch("并发操作性能测试");
        ExecutorService executor = Executors.newFixedThreadPool(10);

        stopWatch.start("并发查询操作");
        
        // 创建并发查询任务
        List<CompletableFuture<List<ContactDTO>>> futures = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            CompletableFuture<List<ContactDTO>> future = CompletableFuture.supplyAsync(() -> {
                try {
                    List<ContactDTO> result = contactService.getAllContacts();
                    System.out.println("并发查询线程 " + Thread.currentThread().getName() + " 查询到 " + result.size() + " 个联系人");
                    return result;
                } catch (Exception e) {
                    System.err.println("并发查询异常: " + e.getMessage());
                    e.printStackTrace();
                    return new ArrayList<>();
                }
            }, executor);
            futures.add(future);
        }

        // 等待所有任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        stopWatch.stop();

        // 验证结果
        for (int i = 0; i < futures.size(); i++) {
            CompletableFuture<List<ContactDTO>> future = futures.get(i);
            List<ContactDTO> result = future.get();
            System.out.println("Future " + i + " 结果数量: " + result.size());
            assertThat(result).hasSizeGreaterThanOrEqualTo(50);
        }

        executor.shutdown();

        // 输出性能统计
        System.out.println(stopWatch.prettyPrint());
        
        // 性能断言
        long totalTimeMillis = stopWatch.getTotalTimeMillis();
        assertThat(totalTimeMillis).isLessThan(10000); // 10秒内完成
        
        System.out.println("并发查询操作耗时: " + totalTimeMillis + "ms");
    }
    
    @Transactional
    @Commit
    private void createTestDataForConcurrentTest() {
        // 创建基础数据并确保事务提交
        for (int i = 0; i < 50; i++) {
            ContactDTO contact = new ContactDTO();
            contact.setName("并发测试" + i);
            contact.setPhoneNumber("1380013803" + String.format("%02d", i));
            contact.setEmail("concurrent" + i + "@test.com");
            contact.setCategory("并发测试");
            contactService.createContact(contact);
        }
    }

    @Test
    @DisplayName("大数据量性能测试")
    void largeDataSetPerformanceTest() {
        StopWatch stopWatch = new StopWatch("大数据量性能测试");

        // 创建大量数据
        stopWatch.start("创建" + LARGE_BATCH_SIZE + "个联系人");
        for (int i = 0; i < LARGE_BATCH_SIZE; i++) {
            ContactDTO contact = new ContactDTO();
            contact.setName("大数据测试" + i);
            contact.setPhoneNumber("1380013804" + String.format("%04d", i));
            contact.setEmail("large" + i + "@test.com");
            contact.setCategory("大数据测试" + (i % 10)); // 10个不同分类
            contactService.createContact(contact);
            
            // 每100个输出一次进度
            if ((i + 1) % 100 == 0) {
                System.out.println("已创建 " + (i + 1) + " 个联系人");
            }
        }
        stopWatch.stop();

        // 测试大数据量查询
        stopWatch.start("查询所有" + LARGE_BATCH_SIZE + "个联系人");
        List<ContactDTO> allContacts = contactService.getAllContacts();
        stopWatch.stop();

        // 测试统计功能
        stopWatch.start("统计分析");
        Map<String, Long> statistics = contactService.getContactStatistics();
        stopWatch.stop();

        // 验证结果
        assertThat(allContacts).hasSize(LARGE_BATCH_SIZE);
        assertThat(statistics).isNotEmpty();

        // 输出性能统计
        System.out.println(stopWatch.prettyPrint());
        
        // 性能断言
        long totalTimeMillis = stopWatch.getTotalTimeMillis();
        assertThat(totalTimeMillis).isLessThan(120000); // 2分钟内完成
        
        System.out.println("大数据量操作总耗时: " + totalTimeMillis + "ms");
        System.out.println("平均每个联系人处理耗时: " + (totalTimeMillis / LARGE_BATCH_SIZE) + "ms");
    }

    @Test
    @DisplayName("内存使用性能测试")
    void memoryUsagePerformanceTest() {
        Runtime runtime = Runtime.getRuntime();
        
        // 记录初始内存使用
        runtime.gc(); // 强制垃圾回收
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
        
        System.out.println("初始内存使用: " + (initialMemory / 1024 / 1024) + " MB");

        // 创建大量联系人
        List<ContactDTO> contacts = new ArrayList<>();
        for (int i = 0; i < BATCH_SIZE; i++) {
            ContactDTO contact = new ContactDTO();
            contact.setName("内存测试" + i);
            contact.setPhoneNumber("1380013805" + String.format("%02d", i));
            contact.setEmail("memory" + i + "@test.com");
            contact.setCategory("内存测试");
            
            ContactDTO created = contactService.createContact(contact);
            contacts.add(created);
        }

        // 记录峰值内存使用
        long peakMemory = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("峰值内存使用: " + (peakMemory / 1024 / 1024) + " MB");
        
        // 清理引用
        contacts.clear();
        contacts = null;
        
        // 强制垃圾回收
        runtime.gc();
        Thread.yield();
        
        // 记录清理后内存使用
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("清理后内存使用: " + (finalMemory / 1024 / 1024) + " MB");
        
        // 内存使用断言
        long memoryIncrease = peakMemory - initialMemory;
        System.out.println("内存增长: " + (memoryIncrease / 1024 / 1024) + " MB");
        
        // 确保内存增长在合理范围内
        assertThat(memoryIncrease).isLessThan(100 * 1024 * 1024); // 小于100MB
    }

    @Test
    @DisplayName("数据库连接池性能测试")
    void databaseConnectionPoolPerformanceTest() throws Exception {
        StopWatch stopWatch = new StopWatch("数据库连接池性能测试");
        ExecutorService executor = Executors.newFixedThreadPool(20);

        stopWatch.start("高并发数据库操作");
        
        // 创建大量并发数据库操作
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            final int index = i;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                // 每个线程执行多个数据库操作
                for (int j = 0; j < 5; j++) {
                    ContactDTO contact = new ContactDTO();
                    contact.setName("连接池测试" + index + "-" + j);
                    contact.setPhoneNumber("1380013806" + String.format("%02d", index) + j);
                    contact.setEmail("pool" + index + j + "@test.com");
                    contact.setCategory("连接池测试");
                    
                    ContactDTO created = contactService.createContact(contact);
                    
                    // 立即查询验证
                    contactService.getContactById(created.getId());
                }
            }, executor);
            futures.add(future);
        }

        // 等待所有任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        stopWatch.stop();

        executor.shutdown();

        // 验证结果
        List<ContactDTO> allContacts = contactService.getAllContacts();
        assertThat(allContacts).hasSizeGreaterThanOrEqualTo(500); // 100 * 5 = 500

        // 输出性能统计
        System.out.println(stopWatch.prettyPrint());
        
        // 性能断言
        long totalTimeMillis = stopWatch.getTotalTimeMillis();
        assertThat(totalTimeMillis).isLessThan(60000); // 1分钟内完成
        
        System.out.println("数据库连接池测试耗时: " + totalTimeMillis + "ms");
    }
}