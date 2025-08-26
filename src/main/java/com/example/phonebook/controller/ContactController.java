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
            // 检查电话号码是否已存在
            if (contactService.isPhoneNumberExists(contactDTO.getPhoneNumber(), null)) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "电话号码已存在");
                return ResponseEntity.badRequest().body(error);
            }
            
            // 检查邮箱是否已存在
            if (contactDTO.getEmail() != null && !contactDTO.getEmail().isEmpty() && 
                contactService.isEmailExists(contactDTO.getEmail(), null)) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "邮箱已存在");
                return ResponseEntity.badRequest().body(error);
            }
            
            createContactCounter.increment();
            ContactDTO createdContact = contactService.createContact(contactDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdContact);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "创建联系人失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 获取所有联系人（分页）- 添加缓存控制
     */
    @GetMapping
    @Timed(value = "contacts.list", description = "Time taken to list contacts")
    public ResponseEntity<?> getAllContacts(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        // 如果没有分页参数，返回所有联系人的简单数组
        if (page == null && size == null) {
            List<ContactDTO> contacts = contactService.getAllContacts();
            return ResponseEntity.ok()
                    .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES).cachePublic())
                    .body(contacts);
        }
        
        // 有分页参数时，返回分页对象
        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 10;
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(pageNum, pageSize, sort);
        Page<ContactDTO> contactPage = contactService.getAllContacts(pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", contactPage.getContent());  // 改为content以符合Spring Data标准
        
        // 创建pageable对象
        Map<String, Object> pageableMap = new HashMap<>();
        pageableMap.put("pageNumber", contactPage.getNumber());
        pageableMap.put("pageSize", contactPage.getSize());
        
        Map<String, Object> sortMap = new HashMap<>();
        sortMap.put("sorted", contactPage.getSort().isSorted());
        sortMap.put("unsorted", contactPage.getSort().isUnsorted());
        pageableMap.put("sort", sortMap);
        
        response.put("pageable", pageableMap);
        response.put("totalElements", contactPage.getTotalElements());
        response.put("totalPages", contactPage.getTotalPages());
        response.put("last", contactPage.isLast());
        response.put("first", contactPage.isFirst());
        response.put("numberOfElements", contactPage.getNumberOfElements());
        response.put("size", contactPage.getSize());
        response.put("number", contactPage.getNumber());
        response.put("sort", sortMap);
        response.put("empty", contactPage.isEmpty());
        
        // 添加缓存控制头
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES).cachePublic())
                .body(response);
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
                    .cacheControl(CacheControl.maxAge(10, TimeUnit.MINUTES).cachePublic())
                    .body(contact.get());
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("error", "联系人不存在");
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 更新联系人
     */
    @PutMapping("/{id}")
    @Timed(value = "contacts.update", description = "Time taken to update contact")
    public ResponseEntity<?> updateContact(@PathVariable Long id, @Valid @RequestBody ContactDTO contactDTO) {
        try {
            // 检查联系人是否存在
            if (!contactService.getContactById(id).isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "联系人不存在");
                return ResponseEntity.notFound().build();
            }
            
            // 检查电话号码是否已被其他联系人使用
            if (contactService.isPhoneNumberExists(contactDTO.getPhoneNumber(), id)) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "电话号码已被其他联系人使用");
                return ResponseEntity.badRequest().body(error);
            }
            
            // 检查邮箱是否已被其他联系人使用
            if (contactDTO.getEmail() != null && !contactDTO.getEmail().isEmpty() && 
                contactService.isEmailExists(contactDTO.getEmail(), id)) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "邮箱已被其他联系人使用");
                return ResponseEntity.badRequest().body(error);
            }
            
            ContactDTO updatedContact = contactService.updateContact(id, contactDTO);
            return ResponseEntity.ok(updatedContact);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "更新联系人失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 删除联系人
     */
    @DeleteMapping("/{id}")
    @Timed(value = "contacts.delete", description = "Time taken to delete contact")
    public ResponseEntity<?> deleteContact(@PathVariable Long id) {
        try {
            if (!contactService.getContactById(id).isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "联系人不存在");
                return ResponseEntity.notFound().build();
            }
            
            contactService.deleteContact(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "删除联系人失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 搜索联系人
     */
    @GetMapping("/search")
    @Timed(value = "contacts.search", description = "Time taken to search contacts")
    public ResponseEntity<Map<String, Object>> searchContacts(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        searchContactCounter.increment();
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ContactDTO> contactPage = contactService.searchContacts(keyword, pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("contacts", contactPage.getContent());
        response.put("currentPage", contactPage.getNumber());
        response.put("totalItems", contactPage.getTotalElements());
        response.put("totalPages", contactPage.getTotalPages());
        response.put("keyword", keyword);
        
        return ResponseEntity.ok(response);
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
     * 获取联系人统计信息
     */
    @GetMapping("/statistics")
    @Timed(value = "contacts.statistics", description = "Time taken to get contact statistics")
    public ResponseEntity<Map<String, Long>> getContactStatistics() {
        Map<String, Long> statistics = contactService.getContactStatistics();
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePublic())
                .body(statistics);
    }
    
    /**
     * 批量删除联系人
     */
    @DeleteMapping("/batch")
    @Timed(value = "contacts.batch.delete", description = "Time taken to batch delete contacts")
    public ResponseEntity<?> batchDeleteContacts(@RequestBody List<Long> ids) {
        try {
            contactService.deleteContacts(ids);
            Map<String, String> response = new HashMap<>();
            response.put("message", "成功删除 " + ids.size() + " 个联系人");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "批量删除失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}