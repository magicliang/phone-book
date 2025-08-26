package com.example.phonebook.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ContactTest {

    private Validator validator;
    private Contact contact;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        contact = new Contact();
        contact.setId(1L);
        contact.setName("张三");
        contact.setPhoneNumber("13800138000");
        contact.setEmail("zhangsan@example.com");
        contact.setAddress("北京市朝阳区");
        contact.setCategory("personal");
        contact.setNotes("测试联系人");
        contact.setCreatedAt(LocalDateTime.now());
        contact.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void validContact_NoViolations() {
        Set<ConstraintViolation<Contact>> violations = validator.validate(contact);
        assertThat(violations).isEmpty();
    }

    @Test
    void invalidName_BlankName_HasViolation() {
        contact.setName("");
        Set<ConstraintViolation<Contact>> violations = validator.validate(contact);
        assertThat(violations).hasSize(1);
    }

    @Test
    void invalidPhoneNumber_BlankPhone_HasViolation() {
        contact.setPhoneNumber("");
        Set<ConstraintViolation<Contact>> violations = validator.validate(contact);
        assertThat(violations).hasSize(1);
    }

    @Test
    void testGettersAndSetters() {
        Long id = 1L;
        String name = "测试姓名";
        String phoneNumber = "13800138000";
        String email = "test@example.com";
        String address = "测试地址";
        String category = "personal";
        String notes = "测试备注";
        LocalDateTime now = LocalDateTime.now();

        Contact entity = new Contact();
        entity.setId(id);
        entity.setName(name);
        entity.setPhoneNumber(phoneNumber);
        entity.setEmail(email);
        entity.setAddress(address);
        entity.setCategory(category);
        entity.setNotes(notes);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getName()).isEqualTo(name);
        assertThat(entity.getPhoneNumber()).isEqualTo(phoneNumber);
        assertThat(entity.getEmail()).isEqualTo(email);
        assertThat(entity.getAddress()).isEqualTo(address);
        assertThat(entity.getCategory()).isEqualTo(category);
        assertThat(entity.getNotes()).isEqualTo(notes);
        assertThat(entity.getCreatedAt()).isEqualTo(now);
        assertThat(entity.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void testEqualsAndHashCode() {
        Contact contact1 = new Contact();
        contact1.setId(1L);
        contact1.setName("张三");

        Contact contact2 = new Contact();
        contact2.setId(1L);
        contact2.setName("张三");

        Contact contact3 = new Contact();
        contact3.setId(2L);
        contact3.setName("李四");

        assertThat(contact1).isEqualTo(contact2);
        assertThat(contact1).isNotEqualTo(contact3);
        assertThat(contact1.hashCode()).isEqualTo(contact2.hashCode());
    }

    @Test
    void testToString() {
        String toString = contact.toString();
        assertThat(toString).contains("张三");
        assertThat(toString).contains("13800138000");
    }
}