package com.example.phonebook.service;

import com.example.phonebook.dto.ContactDTO;
import com.example.phonebook.entity.Contact;
import com.example.phonebook.repository.ContactRepository;
import com.example.phonebook.service.impl.ContactServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContactServiceTest {

    @Mock
    private ContactRepository contactRepository;

    @InjectMocks
    private ContactServiceImpl contactService;

    private Contact testContact;
    private ContactDTO testContactDTO;

    @BeforeEach
    void setUp() {
        testContact = new Contact();
        testContact.setId(1L);
        testContact.setName("张三");
        testContact.setPhoneNumber("13800138000");
        testContact.setEmail("zhangsan@example.com");
        testContact.setAddress("北京市朝阳区");
        testContact.setCategory("personal");
        testContact.setNotes("测试联系人");
        testContact.setCreatedAt(LocalDateTime.now());
        testContact.setUpdatedAt(LocalDateTime.now());

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
    void createContact_Success() {
        // Given
        when(contactRepository.save(any(Contact.class))).thenReturn(testContact);

        // When
        ContactDTO result = contactService.createContact(testContactDTO);

        // Then
        assertNotNull(result);
        assertEquals(testContactDTO.getName(), result.getName());
        assertEquals(testContactDTO.getPhoneNumber(), result.getPhoneNumber());
        assertEquals(testContactDTO.getEmail(), result.getEmail());
        verify(contactRepository, times(1)).save(any(Contact.class));
    }

    @Test
    void createContact_WithNullDTO_ThrowsException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            contactService.createContact(null);
        });
        verify(contactRepository, never()).save(any(Contact.class));
    }

    @Test
    void getContactById_ExistingContact_ReturnsContactDTO() {
        // Given
        when(contactRepository.findById(1L)).thenReturn(Optional.of(testContact));

        // When
        Optional<ContactDTO> result = contactService.getContactById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testContact.getName(), result.get().getName());
        assertEquals(testContact.getPhoneNumber(), result.get().getPhoneNumber());
        verify(contactRepository, times(1)).findById(1L);
    }

    @Test
    void getContactById_NonExistingContact_ReturnsEmpty() {
        // Given
        when(contactRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<ContactDTO> result = contactService.getContactById(999L);

        // Then
        assertFalse(result.isPresent());
        verify(contactRepository, times(1)).findById(999L);
    }

    @Test
    void getAllContacts_ReturnsListOfContactDTOs() {
        // Given
        List<Contact> contacts = Arrays.asList(testContact);
        when(contactRepository.findAll()).thenReturn(contacts);

        // When
        List<ContactDTO> result = contactService.getAllContacts();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testContact.getName(), result.get(0).getName());
        verify(contactRepository, times(1)).findAll();
    }

    @Test
    void getAllContacts_WithPageable_ReturnsPageOfContactDTOs() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Contact> contacts = Arrays.asList(testContact);
        Page<Contact> contactPage = new PageImpl<>(contacts, pageable, 1);
        when(contactRepository.findAll(pageable)).thenReturn(contactPage);

        // When
        Page<ContactDTO> result = contactService.getAllContacts(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testContact.getName(), result.getContent().get(0).getName());
        verify(contactRepository, times(1)).findAll(pageable);
    }

    @Test
    void updateContact_ExistingContact_ReturnsUpdatedContactDTO() {
        // Given
        ContactDTO updateDTO = new ContactDTO();
        updateDTO.setName("李四");
        updateDTO.setPhoneNumber("13900139000");
        updateDTO.setEmail("lisi@example.com");

        Contact updatedContact = new Contact();
        updatedContact.setId(1L);
        updatedContact.setName("李四");
        updatedContact.setPhoneNumber("13900139000");
        updatedContact.setEmail("lisi@example.com");

        when(contactRepository.findById(1L)).thenReturn(Optional.of(testContact));
        when(contactRepository.save(any(Contact.class))).thenReturn(updatedContact);

        // When
        ContactDTO result = contactService.updateContact(1L, updateDTO);

        // Then
        assertNotNull(result);
        assertEquals("李四", result.getName());
        assertEquals("13900139000", result.getPhoneNumber());
        verify(contactRepository, times(1)).findById(1L);
        verify(contactRepository, times(1)).save(any(Contact.class));
    }

    @Test
    void updateContact_NonExistingContact_ThrowsException() {
        // Given
        when(contactRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            contactService.updateContact(999L, testContactDTO);
        });
        verify(contactRepository, times(1)).findById(999L);
        verify(contactRepository, never()).save(any(Contact.class));
    }

    @Test
    void deleteContact_ExistingContact_Success() {
        // Given
        when(contactRepository.existsById(1L)).thenReturn(true);
        doNothing().when(contactRepository).deleteById(1L);

        // When
        contactService.deleteContact(1L);

        // Then
        verify(contactRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteContact_NonExistingContact_ThrowsException() {
        // Given
        when(contactRepository.existsById(999L)).thenReturn(false);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            contactService.deleteContact(999L);
        });
        verify(contactRepository, never()).deleteById(anyLong());
    }

    @Test
    void searchByName_ReturnsMatchingContacts() {
        // Given
        List<Contact> contacts = Arrays.asList(testContact);
        when(contactRepository.findByNameContainingIgnoreCase("张")).thenReturn(contacts);

        // When
        List<ContactDTO> result = contactService.searchByName("张");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testContact.getName(), result.get(0).getName());
        verify(contactRepository, times(1)).findByNameContainingIgnoreCase("张");
    }

    @Test
    void findByPhoneNumber_ExistingPhone_ReturnsContactDTO() {
        // Given
        when(contactRepository.findByPhoneNumber("13800138000")).thenReturn(Optional.of(testContact));

        // When
        Optional<ContactDTO> result = contactService.findByPhoneNumber("13800138000");

        // Then
        assertTrue(result.isPresent());
        assertEquals(testContact.getPhoneNumber(), result.get().getPhoneNumber());
        verify(contactRepository, times(1)).findByPhoneNumber("13800138000");
    }

    @Test
    void getContactsByCategory_ReturnsFilteredContacts() {
        // Given
        List<Contact> contacts = Arrays.asList(testContact);
        when(contactRepository.findByCategory("personal")).thenReturn(contacts);

        // When
        List<ContactDTO> result = contactService.getContactsByCategory("personal");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("personal", result.get(0).getCategory());
        verify(contactRepository, times(1)).findByCategory("personal");
    }

    @Test
    void getContactsByCategory_WithPageable_ReturnsPagedResults() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Contact> contacts = Arrays.asList(testContact);
        Page<Contact> contactPage = new PageImpl<>(contacts, pageable, 1);
        when(contactRepository.findByCategory("personal", pageable)).thenReturn(contactPage);

        // When
        Page<ContactDTO> result = contactService.getContactsByCategory("personal", pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("personal", result.getContent().get(0).getCategory());
        verify(contactRepository, times(1)).findByCategory("personal", pageable);
    }

    @Test
    void searchContacts_ReturnsMatchingContacts() {
        // Given
        List<Contact> contacts = Arrays.asList(testContact);
        when(contactRepository.searchByKeyword("张")).thenReturn(contacts);

        // When
        List<ContactDTO> result = contactService.searchContacts("张");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(contactRepository, times(1)).searchByKeyword("张");
    }

    @Test
    void searchContacts_WithPageable_ReturnsPagedResults() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Contact> contacts = Arrays.asList(testContact);
        Page<Contact> contactPage = new PageImpl<>(contacts, pageable, 1);
        when(contactRepository.searchByKeyword("张", pageable)).thenReturn(contactPage);

        // When
        Page<ContactDTO> result = contactService.searchContacts("张", pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(contactRepository, times(1)).searchByKeyword("张", pageable);
    }

    @Test
    void getContactStatistics_ReturnsStatisticsMap() {
        // Given
        when(contactRepository.countAllContacts()).thenReturn(10L);
        when(contactRepository.countByCategory()).thenReturn(Arrays.asList(
            new Object[]{"personal", 5L},
            new Object[]{"business", 3L},
            new Object[]{"family", 2L}
        ));

        // When
        Map<String, Long> result = contactService.getContactStatistics();

        // Then
        assertNotNull(result);
        assertEquals(10L, result.get("total"));
        assertEquals(5L, result.get("personal"));
        assertEquals(3L, result.get("business"));
        assertEquals(2L, result.get("family"));
        verify(contactRepository, times(1)).countAllContacts();
        verify(contactRepository, times(1)).countByCategory();
    }

    @Test
    void isPhoneNumberExists_ExistingPhone_ReturnsTrue() {
        // Given
        when(contactRepository.existsByPhoneNumber("13800138000")).thenReturn(true);

        // When
        boolean result = contactService.isPhoneNumberExists("13800138000", null);

        // Then
        assertTrue(result);
        verify(contactRepository, times(1)).existsByPhoneNumber("13800138000");
    }

    @Test
    void isPhoneNumberExists_WithExcludeId_ReturnsCorrectResult() {
        // Given
        when(contactRepository.existsByPhoneNumberAndIdNot("13800138000", 1L)).thenReturn(false);

        // When
        boolean result = contactService.isPhoneNumberExists("13800138000", 1L);

        // Then
        assertFalse(result);
        verify(contactRepository, times(1)).existsByPhoneNumberAndIdNot("13800138000", 1L);
    }

    @Test
    void isEmailExists_ExistingEmail_ReturnsTrue() {
        // Given
        when(contactRepository.existsByEmail("test@example.com")).thenReturn(true);

        // When
        boolean result = contactService.isEmailExists("test@example.com", null);

        // Then
        assertTrue(result);
        verify(contactRepository, times(1)).existsByEmail("test@example.com");
    }

    @Test
    void isEmailExists_WithExcludeId_ReturnsCorrectResult() {
        // Given
        when(contactRepository.existsByEmailAndIdNot("test@example.com", 1L)).thenReturn(false);

        // When
        boolean result = contactService.isEmailExists("test@example.com", 1L);

        // Then
        assertFalse(result);
        verify(contactRepository, times(1)).existsByEmailAndIdNot("test@example.com", 1L);
    }

    @Test
    void deleteContacts_BatchDelete_Success() {
        // Given
        List<Long> ids = Arrays.asList(1L, 2L, 3L);
        when(contactRepository.findIdsByIdIn(ids)).thenReturn(ids);
        doNothing().when(contactRepository).deleteAllById(ids);

        // When
        contactService.deleteContacts(ids);

        // Then
        verify(contactRepository, times(1)).findIdsByIdIn(ids);
        verify(contactRepository, times(1)).deleteAllById(ids);
    }

    @Test
    void deleteContacts_EmptyList_DoesNothing() {
        // Given
        List<Long> ids = Collections.emptyList();

        // When
        contactService.deleteContacts(ids);

        // Then
        verify(contactRepository, never()).findIdsByIdIn(anyList());
        verify(contactRepository, never()).deleteAllById(anyList());
    }

    @Test
    void deleteContacts_NullList_ThrowsException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            contactService.deleteContacts(null);
        });
    }
}