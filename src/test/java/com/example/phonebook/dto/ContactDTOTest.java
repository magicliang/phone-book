package com.example.phonebook.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ContactDTOTest {

    private Validator validator;
    private ContactDTO contactDTO;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        contactDTO = new ContactDTO();
        contactDTO.setId(1L);
        contactDTO.setName("张三");
        contactDTO.setPhoneNumber("13800138000");
        contactDTO.setEmail("zhangsan@example.com");
        contactDTO.setAddress("北京市朝阳区");
        contactDTO.setCategory("personal");
        contactDTO.setNotes("测试联系人");
        contactDTO.setCreatedAt(LocalDateTime.now());
        contactDTO.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void validContactDTO_NoViolations() {
        Set<ConstraintViolation<ContactDTO>> violations = validator.validate(contactDTO);
        assertThat(violations).isEmpty();
    }

    @Test
    void invalidName_BlankName_HasViolation() {
        contactDTO.setName("");
        Set<ConstraintViolation<ContactDTO>> violations = validator.validate(contactDTO);
        assertThat(violations).hasSize(1);
    }

    @Test
    void invalidPhoneNumber_BlankPhone_HasViolation() {
        contactDTO.setPhoneNumber("");
        Set<ConstraintViolation<ContactDTO>> violations = validator.validate(contactDTO);
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

        ContactDTO dto = new ContactDTO();
        dto.setId(id);
        dto.setName(name);
        dto.setPhoneNumber(phoneNumber);
        dto.setEmail(email);
        dto.setAddress(address);
        dto.setCategory(category);
        dto.setNotes(notes);
        dto.setCreatedAt(now);
        dto.setUpdatedAt(now);

        assertThat(dto.getId()).isEqualTo(id);
        assertThat(dto.getName()).isEqualTo(name);
        assertThat(dto.getPhoneNumber()).isEqualTo(phoneNumber);
        assertThat(dto.getEmail()).isEqualTo(email);
        assertThat(dto.getAddress()).isEqualTo(address);
        assertThat(dto.getCategory()).isEqualTo(category);
        assertThat(dto.getNotes()).isEqualTo(notes);
        assertThat(dto.getCreatedAt()).isEqualTo(now);
        assertThat(dto.getUpdatedAt()).isEqualTo(now);
    }
}