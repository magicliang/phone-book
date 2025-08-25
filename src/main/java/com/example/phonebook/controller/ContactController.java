package com.example.phonebook.controller;

import com.example.phonebook.dto.ContactDTO;
import com.example.phonebook.service.ContactService;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/contacts")
@CrossOrigin(origins = "*")
public class ContactController {
    
    @Autowired
    private ContactService contactService;
    
    private final Counter createContactCounter;
    private final Counter searchContactCounter;
    
    public ContactController(MeterRegistry meterRegistry) {
        this.createContactCounter = Counter.builder("contacts.created")
                .description("Number of contacts created")
                .register(meterRegistry);
        this.searchContactCounter = Counter.builder("contacts.searched")
                .description("Number of contact searches")
                .register(meterRegistry);
    }
    
    /**
     * 创建新联系人
     */
    @PostMapping
    @Timed(value = "contacts.create", description = "Time taken to create contact")
    public ResponseEntity<?> createContact(@Valid @RequestBody ContactDTO contactDTO) {
        try {
            ContactDTO createdContact = contactService.createContact(contactDTO);
            createContactCounter.increment();
            return ResponseEntity.status(HttpStatus.CREATED).body(createdContact);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * 获取所有联系人（分页）- 添加缓存控制
     */
    @GetMapping
    @Timed(value = "contacts.list", description = "Time taken to list contacts")
    public ResponseEntity<Map<String, Object>> getAllContacts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ContactDTO> contactPage = contactService.getAllContacts(pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("contacts", contactPage.getContent());
        response.put("currentPage", contactPage.getNumber());
        response.put("totalItems", contactPage.getTotalElements());
        response.put("totalPages", contactPage.getTotalPages());
        response.put("hasNext", contactPage.hasNext());
        response.put("hasPrevious", contactPage.hasPrevious());
        
        // 添加缓存控制头
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES).cachePublic())
                .body(response);
    }
    
    /**
     * 获取所有联系人（不分页）
     */
    @GetMapping("/all")
    public ResponseEntity<List<ContactDTO>> getAllContactsNoPaging() {
        List<ContactDTO> contacts = contactService.getAllContacts();
        return ResponseEntity.ok(contacts);
    }
    
    /**
     * 根据ID获取联系人
     */
    @GetMapping("/{id}")
    @Timed(value = "contacts.get", description = "Time taken to get contact by id")
    public ResponseEntity<?> getContactById(@PathVariable Long id) {
        Optional<ContactDTO> contact = contactService.getContactById(id);
        if (contact.isPresent()) {
            return ResponseEntity.ok()
                    .cacheControl(CacheControl.maxAge(15, TimeUnit.MINUTES).cachePublic())
                    .body(contact.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 更新联系人
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateContact(@PathVariable Long id, @Valid @RequestBody ContactDTO contactDTO) {
        try {
            ContactDTO updatedContact = contactService.updateContact(id, contactDTO);
            return ResponseEntity.ok(updatedContact);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * 删除联系人
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteContact(@PathVariable Long id) {
        try {
            contactService.deleteContact(id);
            return ResponseEntity.ok().body(createSuccessResponse("联系人删除成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * 批量删除联系人
     */
    @DeleteMapping("/batch")
    public ResponseEntity<?> deleteContacts(@RequestBody List<Long> ids) {
        try {
            contactService.deleteContacts(ids);
            return ResponseEntity.ok().body(createSuccessResponse("批量删除成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    /**
     * 搜索联系人
     */
    @GetMapping("/search")
    @Timed(value = "contacts.search", description = "Time taken to search contacts")
    public ResponseEntity<Map<String, Object>> searchContacts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllContacts(page, size, "name", "asc");
        }
        
        searchContactCounter.increment();
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<ContactDTO> contactPage = contactService.searchContacts(keyword.trim(), pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("contacts", contactPage.getContent());
        response.put("currentPage", contactPage.getNumber());
        response.put("totalItems", contactPage.getTotalElements());
        response.put("totalPages", contactPage.getTotalPages());
        response.put("hasNext", contactPage.hasNext());
        response.put("hasPrevious", contactPage.hasPrevious());
        response.put("keyword", keyword);
        
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(3, TimeUnit.MINUTES).cachePublic())
                .body(response);
    }
    
    /**
     * 根据分类获取联系人
     */
    @GetMapping("/category/{category}")
    @Timed(value = "contacts.category", description = "Time taken to get contacts by category")
    public ResponseEntity<Map<String, Object>> getContactsByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<ContactDTO> contactPage = contactService.getContactsByCategory(category, pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("contacts", contactPage.getContent());
        response.put("currentPage", contactPage.getNumber());
        response.put("totalItems", contactPage.getTotalElements());
        response.put("totalPages", contactPage.getTotalPages());
        response.put("hasNext", contactPage.hasNext());
        response.put("hasPrevious", contactPage.hasPrevious());
        response.put("category", category);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 根据电话号码查找联系人
     */
    @GetMapping("/phone/{phoneNumber}")
    public ResponseEntity<?> getContactByPhoneNumber(@PathVariable String phoneNumber) {
        Optional<ContactDTO> contact = contactService.findByPhoneNumber(phoneNumber);
        if (contact.isPresent()) {
            return ResponseEntity.ok(contact.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 获取联系人统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Long>> getContactStatistics() {
        Map<String, Long> statistics = contactService.getContactStatistics();
        return ResponseEntity.ok(statistics);
    }
    
    /**
     * 检查电话号码是否存在
     */
    @GetMapping("/check-phone")
    public ResponseEntity<Map<String, Boolean>> checkPhoneNumber(
            @RequestParam String phoneNumber,
            @RequestParam(required = false) Long excludeId) {
        
        boolean exists = contactService.isPhoneNumberExists(phoneNumber, excludeId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 检查邮箱是否存在
     */
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Boolean>> checkEmail(
            @RequestParam String email,
            @RequestParam(required = false) Long excludeId) {
        
        boolean exists = contactService.isEmailExists(email, excludeId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }
    
    // 创建错误响应
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
    
    // 创建成功响应
    private Map<String, Object> createSuccessResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
}