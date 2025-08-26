package com.example.phonebook.repository;

import com.example.phonebook.entity.Contact;
import com.example.phonebook.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ContactRepositoryIntegrationTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private ContactRepository contactRepository;
    
    private Contact testContact1;
    private Contact testContact2;
    private Contact testContact3;
    
    @BeforeEach
    void setUp() {
        testContact1 = TestDataFactory.createContact("张三", "13800138001", "zhangsan@example.com", "personal");
        testContact2 = TestDataFactory.createContact("李四", "13900139002", "lisi@example.com", "business");
        testContact3 = TestDataFactory.createContact("王五", "13700137003", "wangwu@example.com", "personal");
        
        entityManager.persistAndFlush(testContact1);
        entityManager.persistAndFlush(testContact2);
        entityManager.persistAndFlush(testContact3);
    }
    
    @Test
    void testFindByPhoneNumber() {
        // 测试根据电话号码查找
        Optional<Contact> found = contactRepository.findByPhoneNumber("13800138001");
        
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("张三");
        assertThat(found.get().getPhoneNumber()).isEqualTo("13800138001");
    }
    
    @Test
    void testFindByPhoneNumberNotFound() {
        // 测试查找不存在的电话号码
        Optional<Contact> found = contactRepository.findByPhoneNumber("99999999999");
        
        assertThat(found).isEmpty();
    }
    
    @Test
    void testFindByEmail() {
        // 测试根据邮箱查找
        Optional<Contact> found = contactRepository.findByEmail("zhangsan@example.com");
        
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("张三");
        assertThat(found.get().getEmail()).isEqualTo("zhangsan@example.com");
    }
    
    @Test
    void testFindByCategory() {
        // 测试根据分类查找
        Pageable pageable = PageRequest.of(0, 10);
        Page<Contact> personalContacts = contactRepository.findByCategory("personal", pageable);
        
        assertThat(personalContacts.getContent()).hasSize(2);
        assertThat(personalContacts.getContent())
                .extracting(Contact::getName)
                .containsExactlyInAnyOrder("张三", "王五");
    }
    
    @Test
    void testSearchByKeyword() {
        // 测试关键词搜索
        Pageable pageable = PageRequest.of(0, 10);
        Page<Contact> results = contactRepository.searchByKeyword("张", pageable);
        
        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().get(0).getName()).isEqualTo("张三");
    }
    
    @Test
    void testSearchByKeywordInPhoneNumber() {
        // 测试在电话号码中搜索关键词
        Pageable pageable = PageRequest.of(0, 10);
        Page<Contact> results = contactRepository.searchByKeyword("138001", pageable);
        
        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().get(0).getPhoneNumber()).isEqualTo("13800138001");
    }
    
    @Test
    void testSearchByKeywordInEmail() {
        // 测试在邮箱中搜索关键词
        Pageable pageable = PageRequest.of(0, 10);
        Page<Contact> results = contactRepository.searchByKeyword("lisi", pageable);
        
        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().get(0).getEmail()).isEqualTo("lisi@example.com");
    }
    
    @Test
    void testCountByCategory() {
        // 测试按分类统计数量
        long personalCount = contactRepository.countByCategory("personal");
        long businessCount = contactRepository.countByCategory("business");
        
        assertThat(personalCount).isEqualTo(2);
        assertThat(businessCount).isEqualTo(1);
    }
    
    @Test
    void testExistsByPhoneNumber() {
        // 测试电话号码是否存在
        boolean exists = contactRepository.existsByPhoneNumber("13800138001");
        boolean notExists = contactRepository.existsByPhoneNumber("99999999999");
        
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }
    
    @Test
    void testExistsByEmail() {
        // 测试邮箱是否存在
        boolean exists = contactRepository.existsByEmail("zhangsan@example.com");
        boolean notExists = contactRepository.existsByEmail("notexist@example.com");
        
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }
    
    @Test
    void testExistsByPhoneNumberAndIdNot() {
        // 测试排除指定ID的电话号码是否存在
        Long existingId = testContact1.getId();
        
        boolean exists = contactRepository.existsByPhoneNumberAndIdNot("13800138001", existingId);
        boolean existsOtherId = contactRepository.existsByPhoneNumberAndIdNot("13800138001", 999L);
        
        assertThat(exists).isFalse(); // 排除自己，应该不存在
        assertThat(existsOtherId).isTrue(); // 不排除自己，应该存在
    }
    
    @Test
    void testExistsByEmailAndIdNot() {
        // 测试排除指定ID的邮箱是否存在
        Long existingId = testContact1.getId();
        
        boolean exists = contactRepository.existsByEmailAndIdNot("zhangsan@example.com", existingId);
        boolean existsOtherId = contactRepository.existsByEmailAndIdNot("zhangsan@example.com", 999L);
        
        assertThat(exists).isFalse(); // 排除自己，应该不存在
        assertThat(existsOtherId).isTrue(); // 不排除自己，应该存在
    }
    
    @Test
    void testFindAllCategories() {
        // 测试查找所有分类
        List<String> categories = contactRepository.findAllCategories();
        
        assertThat(categories).hasSize(2);
        assertThat(categories).containsExactlyInAnyOrder("personal", "business");
    }
    
    @Test
    void testPagination() {
        // 测试分页功能
        Pageable firstPage = PageRequest.of(0, 2);
        Pageable secondPage = PageRequest.of(1, 2);
        
        Page<Contact> firstPageResult = contactRepository.findAll(firstPage);
        Page<Contact> secondPageResult = contactRepository.findAll(secondPage);
        
        assertThat(firstPageResult.getContent()).hasSize(2);
        assertThat(firstPageResult.getTotalElements()).isEqualTo(3);
        assertThat(firstPageResult.getTotalPages()).isEqualTo(2);
        assertThat(firstPageResult.hasNext()).isTrue();
        
        assertThat(secondPageResult.getContent()).hasSize(1);
        assertThat(secondPageResult.hasNext()).isFalse();
    }
}