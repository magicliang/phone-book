package com.example.phonebook.util;

import com.example.phonebook.dto.ContactDTO;
import com.example.phonebook.entity.Contact;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TestDataFactory {

    public static Contact createContact(String name, String phoneNumber) {
        return createContact(name, phoneNumber, name.toLowerCase() + "@example.com", "personal");
    }

    public static Contact createContact(String name, String phoneNumber, String email, String category) {
        Contact contact = new Contact();
        contact.setName(name);
        contact.setPhoneNumber(phoneNumber);
        contact.setEmail(email);
        contact.setAddress("测试地址 - " + name);
        contact.setCategory(category);
        contact.setNotes("测试备注 - " + name);
        contact.setCreatedAt(LocalDateTime.now());
        contact.setUpdatedAt(LocalDateTime.now());
        return contact;
    }

    public static ContactDTO createContactDTO(String name, String phoneNumber) {
        return createContactDTO(name, phoneNumber, name.toLowerCase() + "@example.com", "personal");
    }

    public static ContactDTO createContactDTO(String name, String phoneNumber, String email, String category) {
        ContactDTO contactDTO = new ContactDTO();
        contactDTO.setName(name);
        contactDTO.setPhoneNumber(phoneNumber);
        contactDTO.setEmail(email);
        contactDTO.setAddress("测试地址 - " + name);
        contactDTO.setCategory(category);
        contactDTO.setNotes("测试备注 - " + name);
        contactDTO.setCreatedAt(LocalDateTime.now());
        contactDTO.setUpdatedAt(LocalDateTime.now());
        return contactDTO;
    }

    public static ContactDTO createInvalidContactDTO() {
        ContactDTO contactDTO = new ContactDTO();
        contactDTO.setName(""); // 无效：空名称
        contactDTO.setPhoneNumber(""); // 无效：空电话
        contactDTO.setEmail("invalid-email"); // 无效：邮箱格式错误
        return contactDTO;
    }

    public static List<Contact> createContactList(int count) {
        List<Contact> contacts = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            String name = "测试用户" + i;
            String phoneNumber = String.format("1380013%04d", i);
            String email = "user" + i + "@example.com";
            String category = (i % 2 == 0) ? "business" : "personal";
            contacts.add(createContact(name, phoneNumber, email, category));
        }
        return contacts;
    }

    public static List<ContactDTO> createContactDTOList(int count) {
        List<ContactDTO> contacts = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            String name = "测试用户" + i;
            String phoneNumber = String.format("1380013%04d", i);
            String email = "user" + i + "@example.com";
            String category = (i % 2 == 0) ? "business" : "personal";
            contacts.add(createContactDTO(name, phoneNumber, email, category));
        }
        return contacts;
    }
}