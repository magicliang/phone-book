package com.example.phonebook.controller;

import com.example.phonebook.config.TestConfig;
import com.example.phonebook.dto.ContactDTO;
import com.example.phonebook.entity.Contact;
import com.example.phonebook.repository.ContactRepository;
import com.example.phonebook.util.TestDataFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@ContextConfiguration(classes = TestConfig.class)
@Transactional
@DisplayName("ContactController 集成测试")
public class ContactControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        contactRepository.deleteAll();
    }

    @Test
    @DisplayName("创建联系人 - 成功")
    void createContact_Success() throws Exception {
        // Given
        ContactDTO contactDTO = TestDataFactory.createContactDTO("张三", "13800138001");

        // When & Then
        mockMvc.perform(post("/api/contacts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(contactDTO)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("张三"))
                .andExpect(jsonPath("$.phoneNumber").value("13800138001"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    @DisplayName("创建联系人 - 验证失败")
    void createContact_ValidationFailed() throws Exception {
        // Given
        ContactDTO invalidDTO = TestDataFactory.createInvalidContactDTO();

        // When & Then
        mockMvc.perform(post("/api/contacts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("获取所有联系人 - 分页")
    void getAllContacts_WithPagination() throws Exception {
        // Given
        List<Contact> contacts = TestDataFactory.createContactList(15);
        contactRepository.saveAll(contacts);

        // When & Then
        mockMvc.perform(get("/api/contacts")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "name")
                        .param("sortDir", "asc"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(10)))
                .andExpect(jsonPath("$.totalElements").value(15))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.last").value(false))
                .andExpect(jsonPath("$.first").value(true));
    }

    @Test
    @DisplayName("获取所有联系人 - 不分页")
    void getAllContactsNoPaging() throws Exception {
        // Given
        List<Contact> contacts = TestDataFactory.createContactList(5);
        contactRepository.saveAll(contacts);

        // When & Then - 使用不同的路径避免与 {id} 冲突
        mockMvc.perform(get("/api/contacts")
                        .param("page", "0")
                        .param("size", "100")) // 使用大的 size 来获取所有数据
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(5)));
    }

    @Test
    @DisplayName("根据ID获取联系人 - 存在")
    void getContactById_Exists() throws Exception {
        // Given
        Contact contact = TestDataFactory.createContact("张三", "13800138001");
        Contact saved = contactRepository.save(contact);

        // When & Then
        mockMvc.perform(get("/api/contacts/{id}", saved.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("张三"))
                .andExpect(jsonPath("$.phoneNumber").value("13800138001"));
    }

    @Test
    @DisplayName("根据ID获取联系人 - 不存在")
    void getContactById_NotExists() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/contacts/{id}", 999L))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("更新联系人 - 成功")
    void updateContact_Success() throws Exception {
        // Given
        Contact contact = TestDataFactory.createContact("张三", "13800138001");
        Contact saved = contactRepository.save(contact);

        ContactDTO updateDTO = TestDataFactory.createContactDTO("张三更新", "13900139002");

        // When & Then
        mockMvc.perform(put("/api/contacts/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("张三更新"))
                .andExpect(jsonPath("$.phoneNumber").value("13900139002"));
    }

    @Test
    @DisplayName("更新联系人 - 不存在")
    void updateContact_NotExists() throws Exception {
        // Given
        ContactDTO updateDTO = TestDataFactory.createContactDTO("张三", "13800138001");

        // When & Then
        mockMvc.perform(put("/api/contacts/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("删除联系人 - 成功")
    void deleteContact_Success() throws Exception {
        // Given
        Contact contact = TestDataFactory.createContact("张三", "13800138001");
        Contact saved = contactRepository.save(contact);

        // When & Then
        mockMvc.perform(delete("/api/contacts/{id}", saved.getId()))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("批量删除联系人")
    void deleteContacts_Batch() throws Exception {
        // Given
        List<Contact> contacts = TestDataFactory.createContactList(3);
        List<Contact> saved = contactRepository.saveAll(contacts);
        List<Long> ids = Arrays.asList(saved.get(0).getId(), saved.get(1).getId());

        // When & Then
        mockMvc.perform(delete("/api/contacts/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ids)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("成功删除 2 个联系人"));
    }

    @Test
    @DisplayName("搜索联系人")
    void searchContacts() throws Exception {
        // Given
        Contact contact1 = TestDataFactory.createContact("张三", "13800138001");
        Contact contact2 = TestDataFactory.createContact("张四", "13900139002");
        Contact contact3 = TestDataFactory.createContact("李五", "13700137003");
        contactRepository.saveAll(Arrays.asList(contact1, contact2, contact3));

        // When & Then
        mockMvc.perform(get("/api/contacts/search")
                        .param("keyword", "张")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contacts", hasSize(2)))
                .andExpect(jsonPath("$.keyword").value("张"));
    }

    @Test
    @DisplayName("根据分类获取联系人")
    void getContactsByCategory() throws Exception {
        // Given
        Contact personal = TestDataFactory.createContact("张三", "13800138001", "zhang@example.com", "personal");
        Contact business = TestDataFactory.createContact("李四", "13900139002", "li@example.com", "business");
        contactRepository.saveAll(Arrays.asList(personal, business));

        // When & Then
        mockMvc.perform(get("/api/contacts/category/{category}", "personal")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contacts", hasSize(1)))
                .andExpect(jsonPath("$.category").value("personal"));
    }

    @Test
    @DisplayName("根据电话号码查找联系人")
    void getContactByPhoneNumber() throws Exception {
        // Given
        Contact contact = TestDataFactory.createContact("张三", "13800138001");
        contactRepository.save(contact);

        // When & Then
        mockMvc.perform(get("/api/contacts/phone/{phoneNumber}", "13800138001"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("张三"));
    }

    @Test
    @DisplayName("获取联系人统计信息")
    void getContactStatistics() throws Exception {
        // Given
        Contact personal = TestDataFactory.createContact("张三", "13800138001", "zhang@example.com", "personal");
        Contact business = TestDataFactory.createContact("李四", "13800138002", "li@example.com", "business");
        contactRepository.saveAll(Arrays.asList(personal, business));

        // When & Then
        mockMvc.perform(get("/api/contacts/statistics"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.personal").value(1))
                .andExpect(jsonPath("$.business").value(1));
    }

    @Test
    @DisplayName("检查电话号码是否存在")
    void checkPhoneNumber() throws Exception {
        // Given
        Contact contact = TestDataFactory.createContact("张三", "13800138001");
        contactRepository.save(contact);

        // When & Then - 存在的电话号码
        mockMvc.perform(get("/api/contacts/check-phone")
                        .param("phoneNumber", "13800138001"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exists").value(true));

        // When & Then - 不存在的电话号码
        mockMvc.perform(get("/api/contacts/check-phone")
                        .param("phoneNumber", "13800138999"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exists").value(false));
    }

    @Test
    @DisplayName("检查邮箱是否存在")
    void checkEmail() throws Exception {
        // Given
        Contact contact = TestDataFactory.createContact("张三", "13800138001");
        contact.setEmail("test@example.com");
        contactRepository.save(contact);

        // When & Then - 存在的邮箱
        mockMvc.perform(get("/api/contacts/check-email")
                        .param("email", "test@example.com"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exists").value(true));

        // When & Then - 不存在的邮箱
        mockMvc.perform(get("/api/contacts/check-email")
                        .param("email", "notexist@example.com"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exists").value(false));
    }
}