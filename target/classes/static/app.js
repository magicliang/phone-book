// 全局变量
let currentPage = 0;
let pageSize = 10;
let totalPages = 0;
let currentKeyword = '';
let currentCategory = '';
let contactToDelete = null;
let isEditing = false;

// API 基础URL
const API_BASE_URL = '/api/contacts';

// DOM 元素
const elements = {
    // 按钮
    addContactBtn: document.getElementById('addContactBtn'),
    statisticsBtn: document.getElementById('statisticsBtn'),
    clearFiltersBtn: document.getElementById('clearFiltersBtn'),
    closeModalBtn: document.getElementById('closeModalBtn'),
    closeStatisticsBtn: document.getElementById('closeStatisticsBtn'),
    cancelBtn: document.getElementById('cancelBtn'),
    saveBtn: document.getElementById('saveBtn'),
    cancelDeleteBtn: document.getElementById('cancelDeleteBtn'),
    confirmDeleteBtn: document.getElementById('confirmDeleteBtn'),
    
    // 输入框和选择框
    searchInput: document.getElementById('searchInput'),
    categoryFilter: document.getElementById('categoryFilter'),
    pageSizeSelect: document.getElementById('pageSizeSelect'),
    
    // 表单元素
    contactForm: document.getElementById('contactForm'),
    contactId: document.getElementById('contactId'),
    contactName: document.getElementById('contactName'),
    contactPhone: document.getElementById('contactPhone'),
    contactEmail: document.getElementById('contactEmail'),
    contactAddress: document.getElementById('contactAddress'),
    contactCategory: document.getElementById('contactCategory'),
    contactNotes: document.getElementById('contactNotes'),
    
    // 容器和显示元素
    contactsContainer: document.getElementById('contactsContainer'),
    paginationContainer: document.getElementById('paginationContainer'),
    contactCount: document.getElementById('contactCount'),
    loadingSpinner: document.getElementById('loadingSpinner'),
    
    // 模态框
    contactModal: document.getElementById('contactModal'),
    statisticsModal: document.getElementById('statisticsModal'),
    deleteModal: document.getElementById('deleteModal'),
    modalTitle: document.getElementById('modalTitle'),
    statisticsContent: document.getElementById('statisticsContent'),
    
    // Toast
    toast: document.getElementById('toast'),
    toastIcon: document.getElementById('toastIcon'),
    toastMessage: document.getElementById('toastMessage')
};

// 初始化应用
document.addEventListener('DOMContentLoaded', function() {
    initializeEventListeners();
    loadContacts();
});

// 初始化事件监听器
function initializeEventListeners() {
    // 按钮事件
    elements.addContactBtn.addEventListener('click', () => openContactModal());
    elements.statisticsBtn.addEventListener('click', () => showStatistics());
    elements.clearFiltersBtn.addEventListener('click', () => clearFilters());
    elements.closeModalBtn.addEventListener('click', () => closeContactModal());
    elements.closeStatisticsBtn.addEventListener('click', () => closeStatisticsModal());
    elements.cancelBtn.addEventListener('click', () => closeContactModal());
    elements.cancelDeleteBtn.addEventListener('click', () => closeDeleteModal());
    elements.confirmDeleteBtn.addEventListener('click', () => confirmDelete());
    
    // 表单提交
    elements.contactForm.addEventListener('submit', handleFormSubmit);
    
    // 搜索和筛选
    elements.searchInput.addEventListener('input', debounce(handleSearch, 300));
    elements.categoryFilter.addEventListener('change', handleCategoryFilter);
    elements.pageSizeSelect.addEventListener('change', handlePageSizeChange);
    
    // 模态框点击外部关闭
    elements.contactModal.addEventListener('click', (e) => {
        if (e.target === elements.contactModal) closeContactModal();
    });
    elements.statisticsModal.addEventListener('click', (e) => {
        if (e.target === elements.statisticsModal) closeStatisticsModal();
    });
    elements.deleteModal.addEventListener('click', (e) => {
        if (e.target === elements.deleteModal) closeDeleteModal();
    });
}

// 防抖函数
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// 加载联系人列表
async function loadContacts() {
    try {
        showLoading(true);
        
        let url = `${API_BASE_URL}?page=${currentPage}&size=${pageSize}`;
        
        if (currentKeyword) {
            url = `${API_BASE_URL}/search?keyword=${encodeURIComponent(currentKeyword)}&page=${currentPage}&size=${pageSize}`;
        } else if (currentCategory) {
            url = `${API_BASE_URL}/category/${encodeURIComponent(currentCategory)}?page=${currentPage}&size=${pageSize}`;
        }
        
        const response = await fetch(url);
        const data = await response.json();
        
        if (response.ok) {
            displayContacts(data.contacts);
            updatePagination(data);
            updateContactCount(data.totalItems);
        } else {
            showToast('加载联系人失败', 'error');
        }
    } catch (error) {
        console.error('Error loading contacts:', error);
        showToast('网络错误，请稍后重试', 'error');
    } finally {
        showLoading(false);
    }
}

// 显示联系人列表
function displayContacts(contacts) {
    const container = elements.contactsContainer;
    
    if (contacts.length === 0) {
        container.innerHTML = `
            <div class="text-center py-12">
                <i class="fas fa-address-book text-6xl text-gray-300 mb-4"></i>
                <h3 class="text-lg font-medium text-gray-900 mb-2">暂无联系人</h3>
                <p class="text-gray-500">点击"添加联系人"按钮创建第一个联系人</p>
            </div>
        `;
        return;
    }
    
    const contactsHTML = contacts.map(contact => `
        <div class="contact-card bg-white border border-gray-200 rounded-lg p-4 fade-in">
            <div class="flex justify-between items-start mb-3">
                <div class="flex-1">
                    <h3 class="text-lg font-semibold text-gray-900 mb-1">${escapeHtml(contact.name)}</h3>
                    <div class="flex items-center text-gray-600 mb-2">
                        <i class="fas fa-phone text-blue-500 mr-2"></i>
                        <span>${escapeHtml(contact.phoneNumber)}</span>
                    </div>
                    ${contact.email ? `
                        <div class="flex items-center text-gray-600 mb-2">
                            <i class="fas fa-envelope text-green-500 mr-2"></i>
                            <span>${escapeHtml(contact.email)}</span>
                        </div>
                    ` : ''}
                    ${contact.address ? `
                        <div class="flex items-center text-gray-600 mb-2">
                            <i class="fas fa-map-marker-alt text-red-500 mr-2"></i>
                            <span>${escapeHtml(contact.address)}</span>
                        </div>
                    ` : ''}
                </div>
                <div class="flex flex-col items-end space-y-2">
                    <span class="category-badge category-${contact.category}">
                        ${getCategoryDisplayName(contact.category)}
                    </span>
                    <div class="flex space-x-2">
                        <button onclick="editContact(${contact.id})" 
                                class="text-blue-600 hover:text-blue-800 p-1" title="编辑">
                            <i class="fas fa-edit"></i>
                        </button>
                        <button onclick="deleteContact(${contact.id})" 
                                class="text-red-600 hover:text-red-800 p-1" title="删除">
                            <i class="fas fa-trash"></i>
                        </button>
                    </div>
                </div>
            </div>
            ${contact.notes ? `
                <div class="border-t border-gray-100 pt-3">
                    <p class="text-sm text-gray-600">
                        <i class="fas fa-sticky-note text-yellow-500 mr-2"></i>
                        ${escapeHtml(contact.notes)}
                    </p>
                </div>
            ` : ''}
            <div class="border-t border-gray-100 pt-3 mt-3">
                <div class="flex justify-between text-xs text-gray-400">
                    <span>创建: ${formatDateTime(contact.createdAt)}</span>
                    ${contact.updatedAt !== contact.createdAt ? 
                        `<span>更新: ${formatDateTime(contact.updatedAt)}</span>` : ''
                    }
                </div>
            </div>
        </div>
    `).join('');
    
    container.innerHTML = `<div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">${contactsHTML}</div>`;
}

// 更新分页控件
function updatePagination(data) {
    totalPages = data.totalPages;
    const container = elements.paginationContainer;
    
    if (totalPages <= 1) {
        container.innerHTML = '';
        return;
    }
    
    let paginationHTML = `
        <div class="flex items-center justify-between">
            <div class="text-sm text-gray-700">
                显示第 ${currentPage * pageSize + 1} - ${Math.min((currentPage + 1) * pageSize, data.totalItems)} 条，
                共 ${data.totalItems} 条记录
            </div>
            <div class="flex items-center space-x-2">
    `;
    
    // 上一页按钮
    paginationHTML += `
        <button onclick="changePage(${currentPage - 1})" 
                ${!data.hasPrevious ? 'disabled' : ''} 
                class="px-3 py-1 text-sm border border-gray-300 rounded ${!data.hasPrevious ? 'bg-gray-100 text-gray-400 cursor-not-allowed' : 'bg-white text-gray-700 hover:bg-gray-50'}">
            上一页
        </button>
    `;
    
    // 页码按钮
    const startPage = Math.max(0, currentPage - 2);
    const endPage = Math.min(totalPages - 1, currentPage + 2);
    
    if (startPage > 0) {
        paginationHTML += `<button onclick="changePage(0)" class="px-3 py-1 text-sm border border-gray-300 rounded bg-white text-gray-700 hover:bg-gray-50">1</button>`;
        if (startPage > 1) {
            paginationHTML += `<span class="px-2 text-gray-500">...</span>`;
        }
    }
    
    for (let i = startPage; i <= endPage; i++) {
        paginationHTML += `
            <button onclick="changePage(${i})" 
                    class="px-3 py-1 text-sm border rounded ${i === currentPage ? 'bg-blue-600 text-white border-blue-600' : 'bg-white text-gray-700 border-gray-300 hover:bg-gray-50'}">
                ${i + 1}
            </button>
        `;
    }
    
    if (endPage < totalPages - 1) {
        if (endPage < totalPages - 2) {
            paginationHTML += `<span class="px-2 text-gray-500">...</span>`;
        }
        paginationHTML += `<button onclick="changePage(${totalPages - 1})" class="px-3 py-1 text-sm border border-gray-300 rounded bg-white text-gray-700 hover:bg-gray-50">${totalPages}</button>`;
    }
    
    // 下一页按钮
    paginationHTML += `
        <button onclick="changePage(${currentPage + 1})" 
                ${!data.hasNext ? 'disabled' : ''} 
                class="px-3 py-1 text-sm border border-gray-300 rounded ${!data.hasNext ? 'bg-gray-100 text-gray-400 cursor-not-allowed' : 'bg-white text-gray-700 hover:bg-gray-50'}">
            下一页
        </button>
    `;
    
    paginationHTML += `
            </div>
        </div>
    `;
    
    container.innerHTML = paginationHTML;
}

// 更新联系人数量显示
function updateContactCount(total) {
    elements.contactCount.textContent = `总计: ${total} 个联系人`;
}

// 显示/隐藏加载动画
function showLoading(show) {
    elements.loadingSpinner.style.display = show ? 'flex' : 'none';
}

// 切换页面
function changePage(page) {
    if (page >= 0 && page < totalPages && page !== currentPage) {
        currentPage = page;
        loadContacts();
    }
}

// 处理搜索
function handleSearch() {
    currentKeyword = elements.searchInput.value.trim();
    currentPage = 0;
    loadContacts();
}

// 处理分类筛选
function handleCategoryFilter() {
    currentCategory = elements.categoryFilter.value;
    currentPage = 0;
    loadContacts();
}

// 处理页面大小变化
function handlePageSizeChange() {
    pageSize = parseInt(elements.pageSizeSelect.value);
    currentPage = 0;
    loadContacts();
}

// 清除筛选条件
function clearFilters() {
    elements.searchInput.value = '';
    elements.categoryFilter.value = '';
    currentKeyword = '';
    currentCategory = '';
    currentPage = 0;
    loadContacts();
}

// 打开联系人模态框
function openContactModal(contact = null) {
    isEditing = !!contact;
    elements.modalTitle.textContent = isEditing ? '编辑联系人' : '添加联系人';
    
    if (contact) {
        elements.contactId.value = contact.id;
        elements.contactName.value = contact.name;
        elements.contactPhone.value = contact.phoneNumber;
        elements.contactEmail.value = contact.email || '';
        elements.contactAddress.value = contact.address || '';
        elements.contactCategory.value = contact.category || 'personal';
        elements.contactNotes.value = contact.notes || '';
    } else {
        elements.contactForm.reset();
        elements.contactId.value = '';
        elements.contactCategory.value = 'personal';
    }
    
    elements.contactModal.classList.remove('hidden');
    elements.contactName.focus();
}

// 关闭联系人模态框
function closeContactModal() {
    elements.contactModal.classList.add('hidden');
    elements.contactForm.reset();
}

// 处理表单提交
async function handleFormSubmit(e) {
    e.preventDefault();
    
    const formData = {
        name: elements.contactName.value.trim(),
        phoneNumber: elements.contactPhone.value.trim(),
        email: elements.contactEmail.value.trim() || null,
        address: elements.contactAddress.value.trim() || null,
        category: elements.contactCategory.value,
        notes: elements.contactNotes.value.trim() || null
    };
    
    try {
        elements.saveBtn.disabled = true;
        elements.saveBtn.innerHTML = '<i class="fas fa-spinner fa-spin mr-2"></i>保存中...';
        
        let response;
        if (isEditing) {
            const contactId = elements.contactId.value;
            response = await fetch(`${API_BASE_URL}/${contactId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(formData)
            });
        } else {
            response = await fetch(API_BASE_URL, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(formData)
            });
        }
        
        const result = await response.json();
        
        if (response.ok) {
            showToast(isEditing ? '联系人更新成功' : '联系人添加成功', 'success');
            closeContactModal();
            loadContacts();
        } else {
            showToast(result.message || '操作失败', 'error');
        }
    } catch (error) {
        console.error('Error saving contact:', error);
        showToast('网络错误，请稍后重试', 'error');
    } finally {
        elements.saveBtn.disabled = false;
        elements.saveBtn.innerHTML = '保存';
    }
}

// 编辑联系人
async function editContact(id) {
    try {
        const response = await fetch(`${API_BASE_URL}/${id}`);
        if (response.ok) {
            const contact = await response.json();
            openContactModal(contact);
        } else {
            showToast('获取联系人信息失败', 'error');
        }
    } catch (error) {
        console.error('Error fetching contact:', error);
        showToast('网络错误，请稍后重试', 'error');
    }
}

// 删除联系人
function deleteContact(id) {
    contactToDelete = id;
    elements.deleteModal.classList.remove('hidden');
}

// 关闭删除确认模态框
function closeDeleteModal() {
    elements.deleteModal.classList.add('hidden');
    contactToDelete = null;
}

// 确认删除
async function confirmDelete() {
    if (!contactToDelete) return;
    
    try {
        elements.confirmDeleteBtn.disabled = true;
        elements.confirmDeleteBtn.innerHTML = '<i class="fas fa-spinner fa-spin mr-2"></i>删除中...';
        
        const response = await fetch(`${API_BASE_URL}/${contactToDelete}`, {
            method: 'DELETE'
        });
        
        if (response.ok) {
            showToast('联系人删除成功', 'success');
            closeDeleteModal();
            loadContacts();
        } else {
            const result = await response.json();
            showToast(result.message || '删除失败', 'error');
        }
    } catch (error) {
        console.error('Error deleting contact:', error);
        showToast('网络错误，请稍后重试', 'error');
    } finally {
        elements.confirmDeleteBtn.disabled = false;
        elements.confirmDeleteBtn.innerHTML = '删除';
    }
}

// 显示统计信息
async function showStatistics() {
    try {
        const response = await fetch(`${API_BASE_URL}/statistics`);
        if (response.ok) {
            const statistics = await response.json();
            displayStatistics(statistics);
            elements.statisticsModal.classList.remove('hidden');
        } else {
            showToast('获取统计信息失败', 'error');
        }
    } catch (error) {
        console.error('Error fetching statistics:', error);
        showToast('网络错误，请稍后重试', 'error');
    }
}

// 显示统计信息内容
function displayStatistics(statistics) {
    const total = statistics.total || 0;
    delete statistics.total;
    
    let statisticsHTML = `
        <div class="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-4">
            <div class="flex items-center">
                <i class="fas fa-users text-blue-600 text-2xl mr-3"></i>
                <div>
                    <h4 class="text-lg font-semibold text-blue-900">总联系人数</h4>
                    <p class="text-2xl font-bold text-blue-600">${total}</p>
                </div>
            </div>
        </div>
        
        <h4 class="text-md font-semibold text-gray-900 mb-3">分类统计</h4>
        <div class="space-y-3">
    `;
    
    const categoryIcons = {
        personal: 'fas fa-user',
        business: 'fas fa-briefcase',
        family: 'fas fa-home',
        friend: 'fas fa-user-friends',
        other: 'fas fa-ellipsis-h'
    };
    
    for (const [category, count] of Object.entries(statistics)) {
        const percentage = total > 0 ? ((count / total) * 100).toFixed(1) : 0;
        statisticsHTML += `
            <div class="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                <div class="flex items-center">
                    <i class="${categoryIcons[category] || 'fas fa-tag'} text-gray-600 mr-3"></i>
                    <span class="font-medium text-gray-900">${getCategoryDisplayName(category)}</span>
                </div>
                <div class="text-right">
                    <span class="text-lg font-semibold text-gray-900">${count}</span>
                    <span class="text-sm text-gray-500 ml-2">(${percentage}%)</span>
                </div>
            </div>
        `;
    }
    
    statisticsHTML += '</div>';
    elements.statisticsContent.innerHTML = statisticsHTML;
}

// 关闭统计信息模态框
function closeStatisticsModal() {
    elements.statisticsModal.classList.add('hidden');
}

// 显示Toast通知
function showToast(message, type = 'info') {
    const icons = {
        success: '<i class="fas fa-check-circle text-green-500"></i>',
        error: '<i class="fas fa-exclamation-circle text-red-500"></i>',
        info: '<i class="fas fa-info-circle text-blue-500"></i>',
        warning: '<i class="fas fa-exclamation-triangle text-yellow-500"></i>'
    };
    
    elements.toastIcon.innerHTML = icons[type] || icons.info;
    elements.toastMessage.textContent = message;
    
    elements.toast.classList.remove('hidden');
    elements.toast.classList.add('fade-in');
    
    setTimeout(() => {
        elements.toast.classList.add('hidden');
        elements.toast.classList.remove('fade-in');
    }, 3000);
}

// 工具函数
function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function getCategoryDisplayName(category) {
    const categoryNames = {
        personal: '个人',
        business: '商务',
        family: '家庭',
        friend: '朋友',
        other: '其他'
    };
    return categoryNames[category] || category;
}

function formatDateTime(dateTimeString) {
    if (!dateTimeString) return '';
    const date = new Date(dateTimeString);
    return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    });
}