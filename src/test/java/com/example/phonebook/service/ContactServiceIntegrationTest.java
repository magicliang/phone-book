package com.example.phonebook.service;

import com.example.phonebook.config.TestConfig;
import com.example.phonebook.dto.ContactDTO;
import com.example.phonebook.entity.Contact;
import com.example.phonebook.repository.ContactRepository;
import com.example.phonebook.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(classes = TestConfig.class)
@Transactional
@DisplayName("ContactService 集成测试")
class ContactServiceIntegrationTest {

    @Autowired
    private ContactService contactService;

    @Autowired
    private ContactRepository contactRepository;

    @BeforeEach
    void setUp() {
        contactRepository.deleteAll();
    }

    @Test
    @DisplayName("创建联系人 - 成功")
    void createContact_Success() {
        // Given
        ContactDTO contactDTO = TestDataFactory.createContactDTO("张三", "13800138001");

        // When
        ContactDTO result = contactService.createContact(contactDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("张三");
        assertThat(result.getPhoneNumber()).isEqualTo("13800138001");
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("创建联系人 - 电话号码重复")
    void createContact_DuplicatePhoneNumber() {
        // Given
        ContactDTO firstContact = TestDataFactory.createContactDTO("张三", "13800138001");
        contactService.createContact(firstContact);

        ContactDTO duplicateContact = TestDataFactory.createContactDTO("李四", "13800138001");

        // When & Then
        assertThatThrownBy(() -> contactService.createContact(duplicateContact))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("电话号码已存在");
    }

    @Test
    @DisplayName("获取所有联系人 - 分页")
    void getAllContacts_WithPagination() {
        // Given
        List<Contact> contacts = TestDataFactory.createContactList(15);
        contactRepository.saveAll(contacts);

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<ContactDTO> result = contactService.getAllContacts(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(10);
        assertThat(result.getTotalElements()).isEqualTo(15);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.hasNext()).isTrue();
    }

    @Test
    @DisplayName("获取所有联系人 - 不分页")
    void getAllContacts_WithoutPagination() {
        // Given
        List<Contact> contacts = TestDataFactory.createContactList(5);
        contactRepository.saveAll(contacts);

        // When
        List<ContactDTO> result = contactService.getAllContacts();

        // Then
        assertThat(result).hasSize(5);
        assertThat(result).allMatch(dto -> dto.getName().startsWith("测试用户"));
    }

    @Test
    @DisplayName("根据ID获取联系人 - 存在")
    void getContactById_Exists() {
        // Given
        Contact contact = TestDataFactory.createContact("张三", "13800138001");
        Contact saved = contactRepository.save(contact);

        // When
        Optional<ContactDTO> result = contactService.getContactById(saved.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("张三");
        assertThat(result.get().getPhoneNumber()).isEqualTo("13800138001");
    }

    @Test
    @DisplayName("根据ID获取联系人 - 不存在")
    void getContactById_NotExists() {
        // When
        Optional<ContactDTO> result = contactService.getContactById(999L);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("更新联系人 - 成功")
    void updateContact_Success() {
        // Given
        Contact contact = TestDataFactory.createContact("张三", "13800138001");
        Contact saved = contactRepository.save(contact);

        ContactDTO updateDTO = new ContactDTO();
        updateDTO.setName("张三更新");
        updateDTO.setPhoneNumber("13800138002");
        updateDTO.setEmail("zhangsan.updated@example.com");
        updateDTO.setAddress("更新地址");
        updateDTO.setCategory("business");
        updateDTO.setNotes("更新备注");

        // When
        ContactDTO result = contactService.updateContact(saved.getId(), updateDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("张三更新");
        assertThat(result.getPhoneNumber()).isEqualTo("13800138002");
        assertThat(result.getEmail()).isEqualTo("zhangsan.updated@example.com");
        assertThat(result.getCategory()).isEqualTo("business");
    }

    @Test
    @DisplayName("更新联系人 - 不存在")
    void updateContact_NotExists() {
        // Given
        ContactDTO updateDTO = TestDataFactory.createContactDTO("张三", "13800138001");

        // When & Then
        assertThatThrownBy(() -> contactService.updateContact(999L, updateDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("联系人不存在");
    }

    @Test
    @DisplayName("删除联系人 - 成功")
    void deleteContact_Success() {
        // Given
        Contact contact = TestDataFactory.createContact("张三", "13800138001");
        Contact saved = contactRepository.save(contact);

        // When
        contactService.deleteContact(saved.getId());

        // Then
        Optional<Contact> result = contactRepository.findById(saved.getId());
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("删除联系人 - 不存在")
    void deleteContact_NotExists() {
        // When & Then
        assertThatThrownBy(() -> contactService.deleteContact(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("联系人不存在");
    }

    @Test
    @DisplayName("批量删除联系人")
    void deleteContacts_Batch() {
        // Given
        List<Contact> contacts = TestDataFactory.createContactList(5);
        List<Contact> saved = contactRepository.saveAll(contacts);
        List<Long> ids = Arrays.asList(saved.get(0).getId(), saved.get(1).getId(), saved.get(2).getId());

        // When
        contactService.deleteContacts(ids);

        // Then
        List<Contact> remaining = contactRepository.findAll();
        assertThat(remaining).hasSize(2);
    }

    @Test
    @DisplayName("搜索联系人 - 按姓名")
    void searchContacts_ByName() {
        // Given
        Contact contact1 = TestDataFactory.createContact("张三", "13800138001");
        Contact contact2 = TestDataFactory.createContact("张四", "13800138002");
        Contact contact3 = TestDataFactory.createContact("李五", "13800138003");
        contactRepository.saveAll(Arrays.asList(contact1, contact2, contact3));

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<ContactDTO> result = contactService.searchContacts("张", pageable);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(dto -> dto.getName().contains("张"));
    }

    @Test
    @DisplayName("搜索联系人 - 按电话号码")
    void searchContacts_ByPhoneNumber() {
        // Given
        Contact contact1 = TestDataFactory.createContact("张三", "13800138001");
        Contact contact2 = TestDataFactory.createContact("李四", "13900139002");
        contactRepository.saveAll(Arrays.asList(contact1, contact2));

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<ContactDTO> result = contactService.searchContacts("138", pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getPhoneNumber()).contains("138");
    }

    @Test
    @DisplayName("根据分类获取联系人")
    void getContactsByCategory() {
        // Given
        Contact personal1 = TestDataFactory.createContact("张三", "13800138001", "zhang@example.com", "personal");
        Contact personal2 = TestDataFactory.createContact("李四", "13800138002", "li@example.com", "personal");
        Contact business = TestDataFactory.createContact("王五", "13800138003", "wang@example.com", "business");
        contactRepository.saveAll(Arrays.asList(personal1, personal2, business));

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<ContactDTO> result = contactService.getContactsByCategory("personal", pageable);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(dto -> dto.getCategory().equals("personal"));
    }

    @Test
    @DisplayName("根据电话号码查找联系人")
    void findByPhoneNumber() {
        // Given
        Contact contact = TestDataFactory.createContact("张三", "13800138001");
        contactRepository.save(contact);

        // When
        Optional<ContactDTO> result = contactService.findByPhoneNumber("13800138001");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("张三");
    }

    @Test
    @DisplayName("获取联系人统计信息")
    void getContactStatistics() {
        // Given
        Contact personal1 = TestDataFactory.createContact("张三", "13800138001", "zhang@example.com", "personal");
        Contact personal2 = TestDataFactory.createContact("李四", "13800138002", "li@example.com", "personal");
        Contact business = TestDataFactory.createContact("王五", "13800138003", "wang@example.com", "business");
        contactRepository.saveAll(Arrays.asList(personal1, personal2, business));

        // When
        Map<String, Long> result = contactService.getContactStatistics();

        // Then
        assertThat(result).containsEntry("total", 3L);
        assertThat(result).containsEntry("personal", 2L);
        assertThat(result).containsEntry("business", 1L);
    }

    @Test
    @DisplayName("检查电话号码是否存在")
    void isPhoneNumberExists() {
        // Given
        Contact contact = TestDataFactory.createContact("张三", "13800138001");
        Contact saved = contactRepository.save(contact);

        // When & Then
        assertThat(contactService.isPhoneNumberExists("13800138001", null)).isTrue();
        assertThat(contactService.isPhoneNumberExists("13800138001", saved.getId())).isFalse();
        assertThat(contactService.isPhoneNumberExists("13800138999", null)).isFalse();
    }

    @Test
    @DisplayName("检查邮箱是否存在")
    void isEmailExists() {
        // Given
        Contact contact = TestDataFactory.createContact("张三", "13800138001");
        contact.setEmail("test@example.com");
        Contact saved = contactRepository.save(contact);

        // When & Then
        assertThat(contactService.isEmailExists("test@example.com", null)).isTrue();
        assertThat(contactService.isEmailExists("test@example.com", saved.getId())).isFalse();
        assertThat(contactService.isEmailExists("notexist@example.com", null)).isFalse();
    }
}