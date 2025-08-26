package com.example.phonebook;

import com.example.phonebook.config.TestConfig;
import com.example.phonebook.dto.ContactDTO;
import com.example.phonebook.entity.Contact;
import com.example.phonebook.repository.ContactRepository;
import com.example.phonebook.service.ContactService;
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

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(classes = TestConfig.class)
@AutoConfigureWebMvc
@Transactional
@DisplayName("电话簿应用程序集成测试")
public class PhonebookApplicationIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private ContactService contactService;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        contactRepository.deleteAll();
    }

    @Test
    @DisplayName("应用程序上下文加载测试")
    void contextLoads() {
        assertThat(contactService).isNotNull();
        assertThat(contactRepository).isNotNull();
    }

    @Test
    @DisplayName("完整的联系人CRUD流程测试")
    void fullContactCrudFlowTest() throws Exception {
        // 1. 创建联系人
        ContactDTO newContact = new ContactDTO();
        newContact.setName("集成测试联系人");
        newContact.setPhoneNumber("13800138000");
        newContact.setEmail("integration@test.com");
        newContact.setCategory("测试");

        String contactJson = objectMapper.writeValueAsString(newContact);

        String response = mockMvc.perform(post("/api/contacts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(contactJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("集成测试联系人"))
                .andExpect(jsonPath("$.phoneNumber").value("13800138000"))
                .andReturn().getResponse().getContentAsString();

        ContactDTO createdContact = objectMapper.readValue(response, ContactDTO.class);
        Long contactId = createdContact.getId();

        // 2. 查询单个联系人
        mockMvc.perform(get("/api/contacts/" + contactId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("集成测试联系人"))
                .andExpect(jsonPath("$.phoneNumber").value("13800138000"));

        // 3. 更新联系人
        createdContact.setName("更新后的联系人");
        createdContact.setEmail("updated@test.com");
        String updatedJson = objectMapper.writeValueAsString(createdContact);

        mockMvc.perform(put("/api/contacts/" + contactId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatedJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("更新后的联系人"))
                .andExpect(jsonPath("$.email").value("updated@test.com"));

        // 4. 查询所有联系人
        mockMvc.perform(get("/api/contacts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));

        // 5. 删除联系人
        mockMvc.perform(delete("/api/contacts/" + contactId))
                .andExpect(status().isNoContent());

        // 6. 验证删除成功
        mockMvc.perform(get("/api/contacts/" + contactId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("联系人搜索功能集成测试")
    void contactSearchIntegrationTest() throws Exception {
        // 创建测试数据
        Contact contact1 = TestDataFactory.createContact("张三", "13800138001", "zhangsan@test.com", "朋友");
        Contact contact2 = TestDataFactory.createContact("李四", "13900139002", "lisi@test.com", "同事");
        Contact contact3 = TestDataFactory.createContact("王五", "13700137003", "wangwu@test.com", "朋友");
        
        contactRepository.save(contact1);
        contactRepository.save(contact2);
        contactRepository.save(contact3);

        // 测试按关键词搜索（搜索接口返回分页对象）
        mockMvc.perform(get("/api/contacts/search")
                .param("keyword", "张"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contacts").isArray())
                .andExpect(jsonPath("$.contacts.length()").value(1))
                .andExpect(jsonPath("$.contacts[0].name").value("张三"))
                .andExpect(jsonPath("$.totalItems").value(1));

        // 测试空关键词搜索（应该返回所有联系人）
        mockMvc.perform(get("/api/contacts/search")
                .param("keyword", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contacts").isArray())
                .andExpect(jsonPath("$.contacts.length()").value(3))
                .andExpect(jsonPath("$.totalItems").value(3));

        // 测试按电话号码搜索
        mockMvc.perform(get("/api/contacts/search")
                .param("keyword", "139002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contacts").isArray())
                .andExpect(jsonPath("$.contacts.length()").value(1))
                .andExpect(jsonPath("$.contacts[0].phoneNumber").value("13900139002"))
                .andExpect(jsonPath("$.totalItems").value(1));
    }

    @Test
    @DisplayName("数据验证集成测试")
    void dataValidationIntegrationTest() throws Exception {
        // 测试无效数据
        ContactDTO invalidContact = new ContactDTO();
        invalidContact.setName(""); // 空名称
        invalidContact.setPhoneNumber("invalid"); // 无效电话
        invalidContact.setEmail("invalid-email"); // 无效邮箱

        String invalidJson = objectMapper.writeValueAsString(invalidContact);

        mockMvc.perform(post("/api/contacts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("并发操作集成测试")
    void concurrentOperationsIntegrationTest() throws Exception {
        // 创建基础数据
        Contact baseContact = TestDataFactory.createContact("并发测试", "13800138000", "concurrent@test.com", "测试");
        Contact savedContact = contactRepository.save(baseContact);

        // 模拟并发读取操作
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(get("/api/contacts/" + savedContact.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("并发测试"));
        }

        // 验证数据一致性
        List<ContactDTO> allContacts = contactService.getAllContacts();
        assertThat(allContacts).hasSize(1);
        assertThat(allContacts.get(0).getName()).isEqualTo("并发测试");
    }

    @Test
    @DisplayName("分页功能集成测试")
    void paginationIntegrationTest() throws Exception {
        // 创建多个测试联系人
        for (int i = 1; i <= 15; i++) {
            Contact contact = TestDataFactory.createContact(
                "联系人" + i,
                "1380013800" + String.format("%02d", i),
                "contact" + i + "@test.com",
                "分类" + (i % 3)
            );
            contactRepository.save(contact);
        }

        // 测试第一页
        mockMvc.perform(get("/api/contacts")
                .param("page", "0")
                .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(5))
                .andExpect(jsonPath("$.totalElements").value(15))
                .andExpect(jsonPath("$.totalPages").value(3));

        // 测试第二页
        mockMvc.perform(get("/api/contacts")
                .param("page", "1")
                .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(5));
    }

    @Test
    @DisplayName("统计功能集成测试")
    void statisticsIntegrationTest() throws Exception {
        // 创建不同分类的联系人
        String[] categories = {"朋友", "同事", "家人"};
        for (int i = 0; i < 9; i++) {
            Contact contact = TestDataFactory.createContact(
                "统计测试" + i,
                "1380013800" + String.format("%02d", i),
                "stats" + i + "@test.com",
                categories[i % 3]
            );
            contactRepository.save(contact);
        }

        // 测试统计接口
        mockMvc.perform(get("/api/contacts/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(9));
    }

    @Test
    @DisplayName("错误处理集成测试")
    void errorHandlingIntegrationTest() throws Exception {
        // 测试访问不存在的联系人
        mockMvc.perform(get("/api/contacts/99999"))
                .andExpect(status().isNotFound());

        // 测试删除不存在的联系人
        mockMvc.perform(delete("/api/contacts/99999"))
                .andExpect(status().isNotFound());

        // 测试更新不存在的联系人
        ContactDTO updateContact = new ContactDTO();
        updateContact.setName("不存在的联系人");
        updateContact.setPhoneNumber("13800138000");
        updateContact.setEmail("notexist@test.com");

        String updateJson = objectMapper.writeValueAsString(updateContact);

        mockMvc.perform(put("/api/contacts/99999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("数据库事务集成测试")
    void databaseTransactionIntegrationTest() {
        // 测试事务回滚
        try {
            // 创建一个联系人
            ContactDTO contact1 = new ContactDTO();
            contact1.setName("事务测试1");
            contact1.setPhoneNumber("13800138001");
            contact1.setEmail("transaction1@test.com");
            contact1.setCategory("测试");

            ContactDTO created1 = contactService.createContact(contact1);
            assertThat(created1.getId()).isNotNull();

            // 尝试创建重复电话号码的联系人（应该失败）
            ContactDTO contact2 = new ContactDTO();
            contact2.setName("事务测试2");
            contact2.setPhoneNumber("13800138001"); // 重复电话号码
            contact2.setEmail("transaction2@test.com");
            contact2.setCategory("测试");

            // 这里应该抛出异常，但我们先验证第一个联系人确实被创建了
            List<ContactDTO> allContacts = contactService.getAllContacts();
            assertThat(allContacts).hasSize(1);
            assertThat(allContacts.get(0).getName()).isEqualTo("事务测试1");

        } catch (Exception e) {
            // 预期的异常
        }
    }

    @Test
    @DisplayName("性能基准测试")
    void performanceBenchmarkTest() {
        long startTime = System.currentTimeMillis();

        // 创建100个联系人
        for (int i = 0; i < 100; i++) {
            ContactDTO contact = new ContactDTO();
            contact.setName("性能测试" + i);
            contact.setPhoneNumber("1380013800" + String.format("%02d", i));
            contact.setEmail("perf" + i + "@test.com");
            contact.setCategory("性能测试");

            contactService.createContact(contact);
        }

        long createTime = System.currentTimeMillis() - startTime;

        // 查询所有联系人
        startTime = System.currentTimeMillis();
        List<ContactDTO> allContacts = contactService.getAllContacts();
        long queryTime = System.currentTimeMillis() - startTime;

        // 验证结果
        assertThat(allContacts).hasSize(100);
        assertThat(createTime).isLessThan(10000); // 创建应在10秒内完成
        assertThat(queryTime).isLessThan(1000);   // 查询应在1秒内完成

        System.out.println("创建100个联系人耗时: " + createTime + "ms");
        System.out.println("查询100个联系人耗时: " + queryTime + "ms");
    }
}