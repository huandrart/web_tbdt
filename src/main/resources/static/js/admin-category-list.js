/**
 * Admin Category List JavaScript
 * Handles category list page interactions
 */

// Xác nhận xóa danh mục
window.confirmDeleteCategory = function(element) {
    const categoryName = element.dataset.name;
    const productCount = element.dataset.productCount;
    
    let message = `Bạn có chắc chắn muốn XÓA HẲN danh mục "${categoryName}"?\n\n`;
    
    if (productCount > 0) {
        message += `⚠️ CẢNH BÁO: Danh mục này có ${productCount} sản phẩm!\n`;
        message += `Các sản phẩm sẽ không còn thuộc danh mục nào (category_id = NULL).\n\n`;
    }
    
    message += `❌ Hành động này sẽ XÓA VĨNH VIỄN danh mục khỏi hệ thống!\n`;
    message += `🔄 Không thể hoàn tác sau khi xóa!\n\n`;
    message += `Bạn có chắc chắn muốn tiếp tục?`;
    
    return confirm(message);
};

// Initialize page when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    console.log('Admin Category List page loaded');
    
    initializeToggleStatus();
    initializeSearch();
    initializeFilters();
    initializeTooltips();
});

// Initialize toggle status functionality
function initializeToggleStatus() {
    document.querySelectorAll('.toggle-status').forEach(button => {
        button.addEventListener('click', function() {
            const categoryId = this.dataset.id;
            const icon = this.querySelector('i');
            
            // Disable button during request
            this.disabled = true;
            
            fetch(`/admin/categories/toggle-status/${categoryId}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').content
                }
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    // Toggle icon
                    if (icon.classList.contains('fa-toggle-on')) {
                        icon.classList.remove('fa-toggle-on');
                        icon.classList.add('fa-toggle-off');
                    } else {
                        icon.classList.remove('fa-toggle-off');
                        icon.classList.add('fa-toggle-on');
                    }
                    
                    // Show success message
                    showAlert('success', 'Trạng thái đã được cập nhật thành công!');
                } else {
                    showAlert('error', 'Có lỗi xảy ra khi cập nhật trạng thái!');
                }
            })
            .catch(error => {
                console.error('Error:', error);
                showAlert('error', 'Có lỗi xảy ra khi cập nhật trạng thái!');
            })
            .finally(() => {
                // Re-enable button
                this.disabled = false;
            });
        });
    });
}

// Initialize search functionality
function initializeSearch() {
    const searchInput = document.querySelector('input[name="search"]');
    if (searchInput) {
        let searchTimeout;
        
        searchInput.addEventListener('input', function() {
            clearTimeout(searchTimeout);
            searchTimeout = setTimeout(() => {
                // Auto-submit form after 500ms of no typing
                this.form.submit();
            }, 500);
        });
    }
}

// Initialize filter buttons
function initializeFilters() {
    document.querySelectorAll('.filter-btn').forEach(button => {
        button.addEventListener('click', function() {
            // Remove active class from all buttons
            document.querySelectorAll('.filter-btn').forEach(btn => {
                btn.classList.remove('active');
            });
            
            // Add active class to clicked button
            this.classList.add('active');
            
            // Update hidden input
            const statusInput = document.querySelector('input[name="status"]');
            if (statusInput) {
                statusInput.value = this.dataset.status;
            }
            
            // Submit form
            this.form.submit();
        });
    });
}

// Initialize Bootstrap tooltips
function initializeTooltips() {
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });
}

// Show alert message
function showAlert(type, message) {
    const alertContainer = document.getElementById('alert-container') || createAlertContainer();
    
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type === 'success' ? 'success' : 'danger'} alert-dismissible fade show`;
    alertDiv.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
    
    alertContainer.appendChild(alertDiv);
    
    // Auto remove after 5 seconds
    setTimeout(() => {
        if (alertDiv.parentNode) {
            alertDiv.remove();
        }
    }, 5000);
}

// Create alert container if it doesn't exist
function createAlertContainer() {
    const container = document.createElement('div');
    container.id = 'alert-container';
    container.style.position = 'fixed';
    container.style.top = '20px';
    container.style.right = '20px';
    container.style.zIndex = '9999';
    document.body.appendChild(container);
    return container;
}

// Utility function to format currency
function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    }).format(amount);
}

// Utility function to format date
function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('vi-VN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    });
}
