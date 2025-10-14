// Templates Products JavaScript
document.addEventListener('DOMContentLoaded', function() {
    console.log('Templates Products JS loaded');
    
    // Initialize cart count
    updateCartCount();
    
    // Add to cart functionality
    window.addToCart = function(productId) {
        console.log('Adding product to cart:', productId);
        
        // Show loading state
        const button = document.querySelector(`button[onclick="addToCart(${productId})"]`);
        if (button) {
            const originalContent = button.innerHTML;
            button.innerHTML = '<i class="fas fa-spinner fa-spin"></i>';
            button.disabled = true;
        }
        
        // Get CSRF token
        const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
        
        // Use jQuery AJAX like product-detail.js
        $.ajax({
            url: '/cart/add',
            method: 'POST',
            headers: {
                'X-CSRF-TOKEN': csrfToken
            },
            data: {
                productId: productId,
                quantity: 1
            },
            success: function(response) {
                console.log('AJAX success response:', response);
                
                const result = typeof response === 'string' ? JSON.parse(response) : response;
                console.log('Parsed result:', result);
                
                if (result.success) {
                    showNotification(result.message || 'Đã thêm sản phẩm vào giỏ hàng!', 'success');
                    updateCartCount();
                } else {
                    showNotification(result.message || 'Có lỗi xảy ra', 'error');
                }
                
                // Reset button
                if (button) {
                    button.innerHTML = originalContent;
                    button.disabled = false;
                }
            },
            error: function(xhr, status, error) {
                console.error('AJAX error:', xhr, status, error);
                showNotification('Có lỗi xảy ra khi thêm sản phẩm', 'error');
                
                // Reset button
                if (button) {
                    button.innerHTML = originalContent;
                    button.disabled = false;
                }
            }
        });
    };
    
    // Update cart count
    function updateCartCount() {
        $.ajax({
            url: '/cart/count',
            method: 'GET',
            success: function(data) {
                const cartCountElement = document.getElementById('cartCount');
                if (cartCountElement) {
                    cartCountElement.textContent = data.count || 0;
                }
            },
            error: function(xhr, status, error) {
                console.error('Error updating cart count:', error);
            }
        });
    }
    
    // Show notification
    function showNotification(message, type) {
        // Remove existing notifications
        const existingNotifications = document.querySelectorAll('.cart-notification');
        existingNotifications.forEach(notification => notification.remove());
        
        // Create notification element
        const notification = document.createElement('div');
        notification.className = `alert alert-${type === 'success' ? 'success' : 'danger'} cart-notification`;
        notification.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            z-index: 9999;
            min-width: 300px;
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
        `;
        notification.innerHTML = `
            <div class="d-flex align-items-center">
                <i class="fas fa-${type === 'success' ? 'check-circle' : 'exclamation-circle'} me-2"></i>
                <span>${message}</span>
                <button type="button" class="btn-close ms-auto" onclick="this.parentElement.parentElement.remove()"></button>
            </div>
        `;
        
        // Add to page
        document.body.appendChild(notification);
        
        // Auto remove after 3 seconds
        setTimeout(() => {
            if (notification.parentElement) {
                notification.remove();
            }
        }, 3000);
    }
    
    // Sort functionality
    const sortSelect = document.getElementById('sortBy');
    if (sortSelect) {
        sortSelect.addEventListener('change', function() {
            const sortValue = this.value;
            if (sortValue) {
                const url = new URL(window.location);
                url.searchParams.set('sortBy', sortValue);
                window.location.href = url.toString();
            }
        });
    }
    
    // Filter form submission
    const filterForm = document.querySelector('form[action="/products"]');
    if (filterForm) {
        filterForm.addEventListener('submit', function(e) {
            // Reset to first page when filtering
            const pageInput = this.querySelector('input[name="page"]');
            if (pageInput) {
                pageInput.value = '0';
            }
        });
    }
});
