package com.example.phonebook.repository;

import com.example.phonebook.entity.Contact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.QueryHint;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {
    
    // 根据姓名查找联系人 - 添加查询提示优化
    @QueryHints({
        @QueryHint(name = "org.hibernate.cacheable", value = "true"),
        @QueryHint(name = "org.hibernate.cacheRegion", value = "contacts")
    })
    List<Contact> findByNameContainingIgnoreCase(String name);
    
    // 根据电话号码查找联系人 - 使用索引优化
    @QueryHints({
        @QueryHint(name = "org.hibernate.cacheable", value = "true")
    })
    Optional<Contact> findByPhoneNumber(String phoneNumber);
    
    // 根据分类查找联系人 - 使用索引优化
    @QueryHints({
        @QueryHint(name = "org.hibernate.cacheable", value = "true")
    })
    List<Contact> findByCategory(String category);
    
    // 根据分类分页查找联系人
    @QueryHints({
        @QueryHint(name = "org.hibernate.cacheable", value = "true")
    })
    Page<Contact> findByCategory(String category, Pageable pageable);
    
    // 根据姓名或电话号码搜索联系人
    @QueryHints({
        @QueryHint(name = "org.hibernate.cacheable", value = "true")
    })
    List<Contact> findByNameContainingIgnoreCaseOrPhoneNumberContaining(String name, String phoneNumber);
    
    // 根据姓名或电话号码搜索联系人（分页）
    @QueryHints({
        @QueryHint(name = "org.hibernate.cacheable", value = "true")
    })
    Page<Contact> findByNameContainingIgnoreCaseOrPhoneNumberContaining(String name, String phoneNumber, Pageable pageable);
    
    // 根据邮箱查找联系人
    @QueryHints({
        @QueryHint(name = "org.hibernate.cacheable", value = "true")
    })
    Optional<Contact> findByEmail(String email);
    
    // 分页查询所有联系人 - 优化排序
    @Query("SELECT c FROM Contact c ORDER BY c.name ASC")
    @QueryHints({
        @QueryHint(name = "org.hibernate.cacheable", value = "true")
    })
    Page<Contact> findAllOrderByName(Pageable pageable);
    
    // 根据分类分页查询 - 使用复合索引
    @Query("SELECT c FROM Contact c WHERE c.category = :category ORDER BY c.name ASC")
    @QueryHints({
        @QueryHint(name = "org.hibernate.cacheable", value = "true")
    })
    Page<Contact> findByCategoryOrderByName(@Param("category") String category, Pageable pageable);
    
    // 优化的模糊搜索 - 简化查询提升性能
    @Query("SELECT c FROM Contact c WHERE " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "c.phoneNumber LIKE CONCAT('%', :keyword, '%') OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "ORDER BY c.name ASC")
    @QueryHints({
        @QueryHint(name = "org.hibernate.cacheable", value = "true"),
        @QueryHint(name = "org.hibernate.cacheRegion", value = "searchResults")
    })
    List<Contact> searchContactsOptimized(@Param("keyword") String keyword);
    
    // 分页模糊搜索 - 简化版本提升性能
    @Query("SELECT c FROM Contact c WHERE " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "c.phoneNumber LIKE CONCAT('%', :keyword, '%') OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "ORDER BY " +
           "CASE WHEN LOWER(c.name) LIKE LOWER(CONCAT(:keyword, '%')) THEN 1 " +
           "     WHEN c.phoneNumber LIKE CONCAT(:keyword, '%') THEN 2 " +
           "     ELSE 3 END, c.name ASC")
    @QueryHints({
        @QueryHint(name = "org.hibernate.cacheable", value = "true")
    })
    Page<Contact> searchContactsWithPriority(@Param("keyword") String keyword, Pageable pageable);
    
    // 统计各分类的联系人数量 - 使用索引优化
    @Query("SELECT c.category, COUNT(c) FROM Contact c GROUP BY c.category ORDER BY COUNT(c) DESC")
    @QueryHints({
        @QueryHint(name = "org.hibernate.cacheable", value = "true"),
        @QueryHint(name = "org.hibernate.cacheRegion", value = "categoryStats")
    })
    List<Object[]> countByCategory();
    
    // 检查电话号码是否已存在（排除指定ID）
    @Query("SELECT COUNT(c) > 0 FROM Contact c WHERE c.phoneNumber = :phoneNumber AND c.id <> :id")
    boolean existsByPhoneNumberAndIdNot(@Param("phoneNumber") String phoneNumber, @Param("id") Long id);
    
    // 检查邮箱是否已存在（排除指定ID）
    @Query("SELECT COUNT(c) > 0 FROM Contact c WHERE c.email = :email AND c.id <> :id")
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("id") Long id);
    
    // 批量查询联系人ID
    @Query("SELECT c.id FROM Contact c WHERE c.id IN :ids")
    @QueryHints({
        @QueryHint(name = "org.hibernate.cacheable", value = "true")
    })
    List<Long> findIdsByIdIn(@Param("ids") List<Long> ids);
    
    // 获取最近创建的联系人
    @Query("SELECT c FROM Contact c ORDER BY c.createdAt DESC")
    @QueryHints({
        @QueryHint(name = "org.hibernate.cacheable", value = "true")
    })
    List<Contact> findRecentContacts(Pageable pageable);
    
    // 获取联系人总数 - 缓存结果
    @Query("SELECT COUNT(c) FROM Contact c")
    @QueryHints({
        @QueryHint(name = "org.hibernate.cacheable", value = "true")
    })
    long countAllContacts();
}