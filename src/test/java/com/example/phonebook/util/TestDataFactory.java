package com.example.phonebook.util;

import com.example.phonebook.dto.ContactDTO;
import com.example.phonebook.entity.Contact;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TestDataFactory {
    
    public static Contact createContact(String name, String phoneNumber) {
        Contact contact = new Contact();
        contact.setName(name);
        contact.setPhoneNumber(phoneNumber);
        contact.setEmail(name.toLowerCase().replace(" ", ".") + "@example.com");
        contact.setAddress("测试地址");
        contact.setCategory("personal");
        contact.setNotes("测试备注");
        contact.setCreatedAt(LocalDateTime.now());
        contact.setUpdatedAt(LocalDateTime.now());
        return contact;
    }
    
    public static Contact createContact(String name, String phoneNumber, String email, String category) {
        Contact contact = createContact(name, phoneNumber);
        contact.setEmail(email);
        contact.setCategory(category);
        return contact;
    }
    
    public static ContactDTO createContactDTO(String name, String phoneNumber) {
        ContactDTO dto = new ContactDTO();
        dto.setName(name);
        dto.setPhoneNumber(phoneNumber);
        dto.setEmail(name.toLowerCase().replace(" ", ".") + "@example.com");
        dto.setAddress("测试地址");
        dto.setCategory("personal");
        dto.setNotes("测试备注");
        return dto;
    }
    
    public static ContactDTO createContactDTO(String name, String phoneNumber, String email, String category) {
        ContactDTO dto = createContactDTO(name, phoneNumber);
        dto.setEmail(email);
        dto.setCategory(category);
        return dto;
    }
    
    public static List<Contact> createContactList(int count) {
        List<Contact> contacts = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            contacts.add(createContact("测试用户" + i, "1380000000" + String.format("%02d", i)));
        }
        return contacts;
    }
    
    public static List<ContactDTO> createContactDTOList(int count) {
        List<ContactDTO> contacts = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            contacts.add(createContactDTO("测试用户" + i, "1380000000" + String.format("%02d", i)));
        }
        return contacts;
    }
    
    public static Contact createInvalidContact() {
        Contact contact = new Contact();
        contact.setName(""); // 无效的空名称
        contact.setPhoneNumber(""); // 无效的空电话
        contact.setEmail("invalid-email"); // 无效的邮箱格式
        return contact;
    }
    
    public static ContactDTO createInvalidContactDTO() {
        ContactDTO dto = new ContactDTO();
        dto.setName(""); // 无效的空名称
        dto.setPhoneNumber(""); // 无效的空电话
        dto.setEmail("invalid-email"); // 无效的邮箱格式
        return dto;
    }
}