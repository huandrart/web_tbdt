/**
 * Admin Category View JavaScript
 * Handles category view page interactions
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
    console.log('Admin Category View page loaded');
    
    // Add any additional initialization code here
    initializeProductImages();
    initializeTooltips();
});

// Initialize product images with lazy loading
function initializeProductImages() {
    const productImages = document.querySelectorAll('.product-image-container img');
    
    productImages.forEach(img => {
        // Add loading state
        img.addEventListener('load', function() {
            this.style.opacity = '1';
        });
        
        // Add error handling
        img.addEventListener('error', function() {
            this.style.display = 'none';
            const fallback = this.parentElement.querySelector('.fallback-icon');
            if (fallback) {
                fallback.style.display = 'flex';
            }
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
