package com.example.phonebook.controller;

import com.example.phonebook.dto.ContactDTO;
import com.example.phonebook.service.ContactService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ContactController.class)
class ContactControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContactService contactService;

    @MockBean
    private MeterRegistry meterRegistry;

    @Autowired
    private ObjectMapper objectMapper;

    private ContactDTO testContactDTO;

    @BeforeEach
    void setUp() {
        testContactDTO = new ContactDTO();
        testContactDTO.setId(1L);
        testContactDTO.setName("张三");
        testContactDTO.setPhoneNumber("13800138000");
        testContactDTO.setEmail("zhangsan@example.com");
        testContactDTO.setAddress("北京市朝阳区");
        testContactDTO.setCategory("personal");
        testContactDTO.setNotes("测试联系人");
    }

    @Test
    void createContact_ValidInput_ReturnsCreated() throws Exception {
        // Given
        when(contactService.isPhoneNumberExists(anyString(), any())).thenReturn(false);
        when(contactService.isEmailExists(anyString(), any())).thenReturn(false);
        when(contactService.createContact(any(ContactDTO.class))).thenReturn(testContactDTO);

        // When & Then
        mockMvc.perform(post("/api/contacts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testContactDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("张三"))
                .andExpect(jsonPath("$.phoneNumber").value("13800138000"));

        verify(contactService, times(1)).createContact(any(ContactDTO.class));
    }

    @Test
    void createContact_DuplicatePhoneNumber_ReturnsBadRequest() throws Exception {
        // Given
        when(contactService.isPhoneNumberExists(anyString(), any())).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/api/contacts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testContactDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("电话号码已存在"));

        verify(contactService, never()).createContact(any(ContactDTO.class));
    }

    @Test
    void createContact_DuplicateEmail_ReturnsBadRequest() throws Exception {
        // Given
        when(contactService.isPhoneNumberExists(anyString(), any())).thenReturn(false);
        when(contactService.isEmailExists(anyString(), any())).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/api/contacts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testContactDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("邮箱已存在"));

        verify(contactService, never()).createContact(any(ContactDTO.class));
    }

    @Test
    void getAllContacts_WithoutPagination_ReturnsAllContacts() throws Exception {
        // Given
        List<ContactDTO> contacts = Arrays.asList(testContactDTO);
        when(contactService.getAllContacts()).thenReturn(contacts);

        // When & Then
        mockMvc.perform(get("/api/contacts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("张三"));

        verify(contactService, times(1)).getAllContacts();
    }

    @Test
    void getAllContacts_WithPagination_ReturnsPagedContacts() throws Exception {
        // Given
        List<ContactDTO> contacts = Arrays.asList(testContactDTO);
        Page<ContactDTO> contactPage = new PageImpl<>(contacts, PageRequest.of(0, 10), 1);
        when(contactService.getAllContacts(any())).thenReturn(contactPage);

        // When & Then
        mockMvc.perform(get("/api/contacts")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].name").value("张三"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(contactService, times(1)).getAllContacts(any());
    }

    @Test
    void getContactById_ExistingContact_ReturnsContact() throws Exception {
        // Given
        when(contactService.getContactById(1L)).thenReturn(Optional.of(testContactDTO));

        // When & Then
        mockMvc.perform(get("/api/contacts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("张三"))
                .andExpect(jsonPath("$.phoneNumber").value("13800138000"));

        verify(contactService, times(1)).getContactById(1L);
    }

    @Test
    void getContactById_NonExistingContact_ReturnsNotFound() throws Exception {
        // Given
        when(contactService.getContactById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/contacts/999"))
                .andExpect(status().isNotFound());

        verify(contactService, times(1)).getContactById(999L);
    }

    @Test
    void updateContact_ValidInput_ReturnsUpdatedContact() throws Exception {
        // Given
        ContactDTO updatedContact = new ContactDTO();
        updatedContact.setId(1L);
        updatedContact.setName("李四");
        updatedContact.setPhoneNumber("13900139000");

        when(contactService.getContactById(1L)).thenReturn(Optional.of(testContactDTO));
        when(contactService.isPhoneNumberExists(anyString(), eq(1L))).thenReturn(false);
        when(contactService.updateContact(eq(1L), any(ContactDTO.class))).thenReturn(updatedContact);

        // When & Then
        mockMvc.perform(put("/api/contacts/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedContact)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("李四"))
                .andExpect(jsonPath("$.phoneNumber").value("13900139000"));

        verify(contactService, times(1)).updateContact(eq(1L), any(ContactDTO.class));
    }

    @Test
    void deleteContact_ExistingContact_ReturnsNoContent() throws Exception {
        // Given
        when(contactService.getContactById(1L)).thenReturn(Optional.of(testContactDTO));
        doNothing().when(contactService).deleteContact(1L);

        // When & Then
        mockMvc.perform(delete("/api/contacts/1"))
                .andExpect(status().isNoContent());

        verify(contactService, times(1)).deleteContact(1L);
    }

    @Test
    void searchContacts_ReturnsMatchingContacts() throws Exception {
        // Given
        List<ContactDTO> contacts = Arrays.asList(testContactDTO);
        Page<ContactDTO> contactPage = new PageImpl<>(contacts, PageRequest.of(0, 10), 1);
        when(contactService.searchContacts(eq("张"), any())).thenReturn(contactPage);

        // When & Then
        mockMvc.perform(get("/api/contacts/search")
                .param("keyword", "张"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contacts").isArray())
                .andExpect(jsonPath("$.contacts[0].name").value("张三"))
                .andExpect(jsonPath("$.keyword").value("张"));

        verify(contactService, times(1)).searchContacts(eq("张"), any());
    }

    @Test
    void getContactStatistics_ReturnsStatistics() throws Exception {
        // Given
        Map<String, Long> statistics = new HashMap<>();
        statistics.put("total", 10L);
        statistics.put("personal", 5L);
        statistics.put("business", 3L);
        statistics.put("family", 2L);
        when(contactService.getContactStatistics()).thenReturn(statistics);

        // When & Then
        mockMvc.perform(get("/api/contacts/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(10))
                .andExpect(jsonPath("$.personal").value(5))
                .andExpect(jsonPath("$.business").value(3))
                .andExpect(jsonPath("$.family").value(2));

        verify(contactService, times(1)).getContactStatistics();
    }
}