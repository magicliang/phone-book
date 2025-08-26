package com.example.phonebook.repository;

import com.example.phonebook.entity.Contact;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ContactRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ContactRepository contactRepository;

    private Contact testContact1;
    private Contact testContact2;
    private Contact testContact3;

    @BeforeEach
    void setUp() {
        testContact1 = createContact("张三", "13800138001", "zhangsan@example.com", "personal");
        testContact2 = createContact("李四", "13900139002", "lisi@example.com", "business");
        testContact3 = createContact("王五", "13700137003", "wangwu@example.com", "personal");

        entityManager.persistAndFlush(testContact1);
        entityManager.persistAndFlush(testContact2);
        entityManager.persistAndFlush(testContact3);
    }

    private Contact createContact(String name, String phoneNumber, String email, String category) {
        Contact contact = new Contact();
        contact.setName(name);
        contact.setPhoneNumber(phoneNumber);
        contact.setEmail(email);
        contact.setCategory(category);
        contact.setAddress("测试地址");
        contact.setNotes("测试备注");
        contact.setCreatedAt(LocalDateTime.now());
        contact.setUpdatedAt(LocalDateTime.now());
        return contact;
    }

    @Test
    void findByPhoneNumber_ExistingPhone_ReturnsContact() {
        // When
        Optional<Contact> found = contactRepository.findByPhoneNumber("13800138001");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("张三");
        assertThat(found.get().getPhoneNumber()).isEqualTo("13800138001");
    }

    @Test
    void findByPhoneNumber_NonExistingPhone_ReturnsEmpty() {
        // When
        Optional<Contact> found = contactRepository.findByPhoneNumber("99999999999");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void findByEmail_ExistingEmail_ReturnsContact() {
        // When
        Optional<Contact> found = contactRepository.findByEmail("zhangsan@example.com");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("张三");
        assertThat(found.get().getEmail()).isEqualTo("zhangsan@example.com");
    }

    @Test
    void findByEmail_NonExistingEmail_ReturnsEmpty() {
        // When
        Optional<Contact> found = contactRepository.findByEmail("notexist@example.com");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void findByNameContainingIgnoreCase_MatchingName_ReturnsContacts() {
        // When
        List<Contact> found = contactRepository.findByNameContainingIgnoreCase("张");

        // Then
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getName()).isEqualTo("张三");
    }

    @Test
    void findByNameContainingIgnoreCase_CaseInsensitive_ReturnsContacts() {
        // When
        List<Contact> found = contactRepository.findByNameContainingIgnoreCase("张");

        // Then
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getName()).isEqualTo("张三");
    }

    @Test
    void findByCategory_ExistingCategory_ReturnsContacts() {
        // When
        List<Contact> personalContacts = contactRepository.findByCategory("personal");

        // Then
        assertThat(personalContacts).hasSize(2);
        assertThat(personalContacts)
                .extracting(Contact::getName)
                .containsExactlyInAnyOrder("张三", "王五");
    }

    @Test
    void findByCategory_WithPageable_ReturnsPagedContacts() {
        // Given
        Pageable pageable = PageRequest.of(0, 1);

        // When
        Page<Contact> personalContacts = contactRepository.findByCategory("personal", pageable);

        // Then
        assertThat(personalContacts.getContent()).hasSize(1);
        assertThat(personalContacts.getTotalElements()).isEqualTo(2);
        assertThat(personalContacts.getTotalPages()).isEqualTo(2);
    }

    @Test
    void searchByKeyword_MatchingName_ReturnsContacts() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Contact> results = contactRepository.searchByKeyword("张", pageable);

        // Then
        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().get(0).getName()).isEqualTo("张三");
    }

    @Test
    void searchByKeyword_MatchingPhoneNumber_ReturnsContacts() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Contact> results = contactRepository.searchByKeyword("138001", pageable);

        // Then
        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().get(0).getPhoneNumber()).contains("138001");
    }

    @Test
    void searchByKeyword_MatchingEmail_ReturnsContacts() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Contact> results = contactRepository.searchByKeyword("lisi", pageable);

        // Then
        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().get(0).getEmail()).contains("lisi");
    }

    @Test
    void searchByKeyword_NoMatch_ReturnsEmpty() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Contact> results = contactRepository.searchByKeyword("不存在", pageable);

        // Then
        assertThat(results.getContent()).isEmpty();
    }

    @Test
    void existsByPhoneNumber_ExistingPhone_ReturnsTrue() {
        // When
        boolean exists = contactRepository.existsByPhoneNumber("13800138001");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByPhoneNumber_NonExistingPhone_ReturnsFalse() {
        // When
        boolean exists = contactRepository.existsByPhoneNumber("99999999999");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void existsByEmail_ExistingEmail_ReturnsTrue() {
        // When
        boolean exists = contactRepository.existsByEmail("zhangsan@example.com");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmail_NonExistingEmail_ReturnsFalse() {
        // When
        boolean exists = contactRepository.existsByEmail("notexist@example.com");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void existsByPhoneNumberAndIdNot_ExcludingSelf_ReturnsFalse() {
        // When
        boolean exists = contactRepository.existsByPhoneNumberAndIdNot("13800138001", testContact1.getId());

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void existsByPhoneNumberAndIdNot_NotExcludingSelf_ReturnsTrue() {
        // When
        boolean exists = contactRepository.existsByPhoneNumberAndIdNot("13800138001", 999L);

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmailAndIdNot_ExcludingSelf_ReturnsFalse() {
        // When
        boolean exists = contactRepository.existsByEmailAndIdNot("zhangsan@example.com", testContact1.getId());

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void existsByEmailAndIdNot_NotExcludingSelf_ReturnsTrue() {
        // When
        boolean exists = contactRepository.existsByEmailAndIdNot("zhangsan@example.com", 999L);

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void countAllContacts_ReturnsCorrectCount() {
        // When
        Long count = contactRepository.countAllContacts();

        // Then
        assertThat(count).isEqualTo(3L);
    }

    @Test
    void countByCategory_ReturnsCorrectCounts() {
        // When
        List<Object[]> results = contactRepository.countByCategory();

        // Then
        assertThat(results).hasSize(2);
        
        // 验证结果包含正确的分类和数量
        boolean foundPersonal = false;
        boolean foundBusiness = false;
        
        for (Object[] result : results) {
            String category = (String) result[0];
            Long count = (Long) result[1];
            
            if ("personal".equals(category)) {
                assertThat(count).isEqualTo(2L);
                foundPersonal = true;
            } else if ("business".equals(category)) {
                assertThat(count).isEqualTo(1L);
                foundBusiness = true;
            }
        }
        
        assertThat(foundPersonal).isTrue();
        assertThat(foundBusiness).isTrue();
    }

    @Test
    void findIdsByIdIn_ExistingIds_ReturnsIds() {
        // Given
        List<Long> inputIds = Arrays.asList(testContact1.getId(), testContact2.getId(), 999L);

        // When
        List<Long> foundIds = contactRepository.findIdsByIdIn(inputIds);

        // Then
        assertThat(foundIds).hasSize(2);
        assertThat(foundIds).containsExactlyInAnyOrder(testContact1.getId(), testContact2.getId());
    }

    @Test
    void findAllCategories_ReturnsDistinctCategories() {
        // When
        List<String> categories = contactRepository.findAllCategories();

        // Then
        assertThat(categories).hasSize(2);
        assertThat(categories).containsExactlyInAnyOrder("personal", "business");
    }

    @Test
    void deleteAllById_RemovesSpecifiedContacts() {
        // Given
        List<Long> idsToDelete = Arrays.asList(testContact1.getId(), testContact2.getId());

        // When
        contactRepository.deleteAllById(idsToDelete);
        entityManager.flush();

        // Then
        List<Contact> remaining = contactRepository.findAll();
        assertThat(remaining).hasSize(1);
        assertThat(remaining.get(0).getName()).isEqualTo("王五");
    }

    @Test
    void customQueryMethods_WorkCorrectly() {
        // Test pagination with findAll
        Pageable pageable = PageRequest.of(0, 2);
        Page<Contact> page = contactRepository.findAll(pageable);

        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.hasNext()).isTrue();
    }
}