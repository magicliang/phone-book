package com.example.phonebook.service.impl;

import com.example.phonebook.dto.ContactDTO;
import com.example.phonebook.entity.Contact;
import com.example.phonebook.repository.ContactRepository;
import com.example.phonebook.service.ContactService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Transactional
public class ContactServiceImpl implements ContactService {
    
    @Autowired
    private ContactRepository contactRepository;
    
    @Override
    @CacheEvict(value = {"contacts", "searchResults", "categoryStats"}, allEntries = true)
    public ContactDTO createContact(ContactDTO contactDTO) {
        // 检查电话号码是否已存在
        if (contactRepository.findByPhoneNumber(contactDTO.getPhoneNumber()).isPresent()) {
            throw new RuntimeException("电话号码已存在: " + contactDTO.getPhoneNumber());
        }
        
        // 检查邮箱是否已存在（如果提供了邮箱）
        if (contactDTO.getEmail() != null && !contactDTO.getEmail().trim().isEmpty()) {
            if (contactRepository.findByEmail(contactDTO.getEmail()).isPresent()) {
                throw new RuntimeException("邮箱已存在: " + contactDTO.getEmail());
            }
        }
        
        Contact contact = convertToEntity(contactDTO);
        Contact savedContact = contactRepository.save(contact);
        return convertToDTO(savedContact);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<ContactDTO> getContactById(Long id) {
        return contactRepository.findById(id)
                .map(this::convertToDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ContactDTO> getAllContacts() {
        List<Contact> contacts = contactRepository.findAll();
        return contacts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "contacts", key = "#pageable.pageNumber + '_' + #pageable.pageSize + '_' + #pageable.sort.toString()")
    public Page<ContactDTO> getAllContacts(Pageable pageable) {
        Page<Contact> contacts = contactRepository.findAll(pageable);
        List<ContactDTO> contactDTOs = contacts.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(contactDTOs, pageable, contacts.getTotalElements());
    }
    
    @Override
    @CacheEvict(value = {"contacts", "searchResults", "categoryStats"}, allEntries = true)
    @CachePut(value = "contact", key = "#id")
    public ContactDTO updateContact(Long id, ContactDTO contactDTO) {
        Contact existingContact = contactRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("联系人不存在，ID: " + id));
        
        // 检查电话号码是否被其他联系人使用
        if (!existingContact.getPhoneNumber().equals(contactDTO.getPhoneNumber())) {
            if (isPhoneNumberExists(contactDTO.getPhoneNumber(), id)) {
                throw new RuntimeException("电话号码已被其他联系人使用: " + contactDTO.getPhoneNumber());
            }
        }
        
        // 检查邮箱是否被其他联系人使用
        if (contactDTO.getEmail() != null && !contactDTO.getEmail().trim().isEmpty()) {
            if (!contactDTO.getEmail().equals(existingContact.getEmail())) {
                if (isEmailExists(contactDTO.getEmail(), id)) {
                    throw new RuntimeException("邮箱已被其他联系人使用: " + contactDTO.getEmail());
                }
            }
        }
        
        // 更新联系人信息
        updateContactFields(existingContact, contactDTO);
        Contact updatedContact = contactRepository.save(existingContact);
        return convertToDTO(updatedContact);
    }
    
    @Override
    @CacheEvict(value = {"contacts", "searchResults", "categoryStats", "contact"}, allEntries = true)
    public void deleteContact(Long id) {
        if (!contactRepository.existsById(id)) {
            throw new RuntimeException("联系人不存在，ID: " + id);
        }
        contactRepository.deleteById(id);
    }
    
    @Override
    @CacheEvict(value = {"contacts", "searchResults", "categoryStats", "contact"}, allEntries = true)
    public void deleteContacts(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        contactRepository.deleteAllById(ids);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ContactDTO> searchByName(String name) {
        List<Contact> contacts = contactRepository.findByNameContainingIgnoreCase(name);
        return contacts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<ContactDTO> findByPhoneNumber(String phoneNumber) {
        return contactRepository.findByPhoneNumber(phoneNumber)
                .map(this::convertToDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ContactDTO> getContactsByCategory(String category) {
        List<Contact> contacts = contactRepository.findByCategory(category);
        return contacts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "contacts", key = "'category_' + #category + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<ContactDTO> getContactsByCategory(String category, Pageable pageable) {
        Page<Contact> contacts = contactRepository.findByCategory(category, pageable);
        List<ContactDTO> contactDTOs = contacts.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(contactDTOs, pageable, contacts.getTotalElements());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ContactDTO> searchContacts(String keyword) {
        List<Contact> contacts = contactRepository.findByNameContainingIgnoreCaseOrPhoneNumberContaining(keyword, keyword);
        return contacts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "searchResults", key = "'search_' + #keyword + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<ContactDTO> searchContacts(String keyword, Pageable pageable) {
        Page<Contact> contacts = contactRepository.findByNameContainingIgnoreCaseOrPhoneNumberContaining(keyword, keyword, pageable);
        List<ContactDTO> contactDTOs = contacts.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(contactDTOs, pageable, contacts.getTotalElements());
    }
    
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "categoryStats", key = "'stats'")
    public Map<String, Long> getContactStatistics() {
        Map<String, Long> statistics = new HashMap<>();
        statistics.put("total", contactRepository.count());
        
        // 获取按分类统计的数据
        List<Object[]> categoryStats = contactRepository.countByCategory();
        for (Object[] stat : categoryStats) {
            String category = (String) stat[0];
            Long count = (Long) stat[1];
            if (category != null && !category.trim().isEmpty()) {
                statistics.put(category, count);
            }
        }
        
        return statistics;
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isPhoneNumberExists(String phoneNumber, Long excludeId) {
        if (excludeId == null) {
            return contactRepository.findByPhoneNumber(phoneNumber).isPresent();
        }
        return contactRepository.existsByPhoneNumberAndIdNot(phoneNumber, excludeId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isEmailExists(String email, Long excludeId) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        if (excludeId == null) {
            return contactRepository.findByEmail(email).isPresent();
        }
        return contactRepository.existsByEmailAndIdNot(email, excludeId);
    }
    
    // 异步方法：批量处理联系人
    @Async("taskExecutor")
    public CompletableFuture<Void> batchProcessContacts(List<Long> contactIds) {
        // 这里可以添加批量处理逻辑，比如批量更新、批量导出等
        return CompletableFuture.completedFuture(null);
    }
    
    // 异步方法：预热缓存
    @Async("taskExecutor")
    public CompletableFuture<Void> warmUpCache() {
        // 预热常用查询的缓存
        getContactStatistics();
        return CompletableFuture.completedFuture(null);
    }
    
    // 优化的实体转换方法
    private ContactDTO convertToDTO(Contact contact) {
        if (contact == null) {
            return null;
        }
        
        ContactDTO dto = new ContactDTO();
        // 使用BeanUtils进行快速属性复制
        BeanUtils.copyProperties(contact, dto);
        return dto;
    }
    
    private Contact convertToEntity(ContactDTO contactDTO) {
        if (contactDTO == null) {
            return null;
        }
        
        Contact contact = new Contact();
        BeanUtils.copyProperties(contactDTO, contact);
        return contact;
    }
    
    // 优化的字段更新方法
    private void updateContactFields(Contact existingContact, ContactDTO contactDTO) {
        existingContact.setName(contactDTO.getName());
        existingContact.setPhoneNumber(contactDTO.getPhoneNumber());
        existingContact.setEmail(contactDTO.getEmail());
        existingContact.setAddress(contactDTO.getAddress());
        existingContact.setCategory(contactDTO.getCategory());
        existingContact.setNotes(contactDTO.getNotes());
    }
}