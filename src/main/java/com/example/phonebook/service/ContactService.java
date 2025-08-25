package com.example.phonebook.service;

import com.example.phonebook.dto.ContactDTO;
import com.example.phonebook.entity.Contact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ContactService {
    
    /**
     * 创建新联系人
     */
    ContactDTO createContact(ContactDTO contactDTO);
    
    /**
     * 根据ID获取联系人
     */
    Optional<ContactDTO> getContactById(Long id);
    
    /**
     * 获取所有联系人
     */
    List<ContactDTO> getAllContacts();
    
    /**
     * 分页获取所有联系人
     */
    Page<ContactDTO> getAllContacts(Pageable pageable);
    
    /**
     * 更新联系人信息
     */
    ContactDTO updateContact(Long id, ContactDTO contactDTO);
    
    /**
     * 删除联系人
     */
    void deleteContact(Long id);
    
    /**
     * 根据姓名搜索联系人
     */
    List<ContactDTO> searchByName(String name);
    
    /**
     * 根据电话号码查找联系人
     */
    Optional<ContactDTO> findByPhoneNumber(String phoneNumber);
    
    /**
     * 根据分类获取联系人
     */
    List<ContactDTO> getContactsByCategory(String category);
    
    /**
     * 分页根据分类获取联系人
     */
    Page<ContactDTO> getContactsByCategory(String category, Pageable pageable);
    
    /**
     * 模糊搜索联系人
     */
    List<ContactDTO> searchContacts(String keyword);
    
    /**
     * 分页模糊搜索联系人
     */
    Page<ContactDTO> searchContacts(String keyword, Pageable pageable);
    
    /**
     * 获取联系人统计信息
     */
    Map<String, Long> getContactStatistics();
    
    /**
     * 检查电话号码是否已存在
     */
    boolean isPhoneNumberExists(String phoneNumber, Long excludeId);
    
    /**
     * 检查邮箱是否已存在
     */
    boolean isEmailExists(String email, Long excludeId);
    
    /**
     * 批量删除联系人
     */
    void deleteContacts(List<Long> ids);
}