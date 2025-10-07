// Cart functionality
document.addEventListener('DOMContentLoaded', function() {
    // Initialize quantity controls
    initQuantityControls();
    
    // Initialize remove buttons
    initRemoveButtons();
    
    // Initialize clear all button
    initClearAllButton();
    
    // Initialize cart count
    updateCartTotals();
    
    // Test function - remove this after testing
    window.testCartUpdate = function() {
        console.log('Testing cart update...');
        updateCartTotals();
    };
    
    // Test function for order summary
    window.testOrderSummary = function() {
        console.log('Testing order summary update...');
        const subtotalElement = document.querySelector('.total-section .d-flex.justify-content-between:first-of-type span:last-child');
        const taxElement = document.querySelector('.total-section .d-flex.justify-content-between:nth-of-type(3) span:last-child');
        const totalElement = document.querySelector('.total-section .text-primary.fs-5');
        const altTotalElement = document.querySelector('.total-section strong.text-primary');
        
        console.log('Subtotal element:', subtotalElement);
        console.log('Tax element:', taxElement);
        console.log('Total element:', totalElement);
        console.log('Alt total element:', altTotalElement);
        
        if (subtotalElement) {
            subtotalElement.textContent = '777,780,000 ₫';
            console.log('Updated subtotal to test value');
        }
        if (taxElement) {
            taxElement.textContent = '77,778,000 ₫';
            console.log('Updated tax to test value');
        }
        if (totalElement) {
            totalElement.textContent = '855,558,000 ₫';
            console.log('Updated total to test value');
        } else if (altTotalElement) {
            altTotalElement.textContent = '855,558,000 ₫';
            console.log('Updated total with alt selector to test value');
        }
    };
    
    // Function to find all possible total elements
    window.findAllTotalElements = function() {
        console.log('Finding all possible total elements...');
        const selectors = [
            '.total-section .text-primary.fs-5',
            '.total-section strong.text-primary',
            '.total-section .fw-bold.fs-5.text-primary',
            '.total-section strong:last-child',
            '.total-section .d-flex.justify-content-between:last-of-type strong:last-child'
        ];
        
        selectors.forEach((selector, index) => {
            const element = document.querySelector(selector);
            console.log(`Selector ${index + 1} (${selector}):`, element);
            if (element) {
                console.log(`  Text content: "${element.textContent}"`);
                console.log(`  Classes: "${element.className}"`);
            }
        });
    };
});

// Initialize quantity controls
function initQuantityControls() {
    // Plus buttons
    document.querySelectorAll('.quantity-plus').forEach(button => {
        button.addEventListener('click', function() {
            const productId = this.getAttribute('data-product-id');
            const quantityInput = document.querySelector(`input[data-product-id="${productId}"]`);
            const currentQuantity = parseInt(quantityInput.value);
            const maxQuantity = parseInt(quantityInput.getAttribute('max')) || 99;
            
            if (currentQuantity < maxQuantity) {
                updateQuantity(productId, currentQuantity + 1);
            }
        });
    });
    
    // Minus buttons
    document.querySelectorAll('.quantity-minus').forEach(button => {
        button.addEventListener('click', function() {
            const productId = this.getAttribute('data-product-id');
            const quantityInput = document.querySelector(`input[data-product-id="${productId}"]`);
            const currentQuantity = parseInt(quantityInput.value);
            
            if (currentQuantity > 1) {
                updateQuantity(productId, currentQuantity - 1);
            }
        });
    });
    
    // Quantity input changes
    document.querySelectorAll('.quantity-input').forEach(input => {
        input.addEventListener('change', function() {
            const productId = this.getAttribute('data-product-id');
            const quantity = parseInt(this.value);
            const maxQuantity = parseInt(this.getAttribute('max')) || 99;
            
            if (quantity < 1) {
                this.value = 1;
                updateQuantity(productId, 1);
            } else if (quantity > maxQuantity) {
                this.value = maxQuantity;
                updateQuantity(productId, maxQuantity);
            } else {
                updateQuantity(productId, quantity);
            }
        });
    });
}

// Initialize remove buttons
function initRemoveButtons() {
    document.querySelectorAll('.remove-item').forEach(button => {
        button.addEventListener('click', function() {
            const productId = this.getAttribute('data-product-id');
            removeItem(productId);
        });
    });
}

// Initialize clear all button
function initClearAllButton() {
    const clearAllBtn = document.getElementById('clearAllBtn');
    if (clearAllBtn) {
        clearAllBtn.addEventListener('click', function() {
            if (confirm('Bạn có chắc chắn muốn xóa tất cả sản phẩm khỏi giỏ hàng?')) {
                clearAllItems();
            }
        });
    }
}

// Update quantity via AJAX
function updateQuantity(productId, quantity) {
    console.log(`Updating quantity for product ${productId} to ${quantity}`);
    
    // Update UI first for immediate feedback
    updateCartUI(productId, quantity);
    
    // Then sync with server
    fetch('/cart/update', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            'X-CSRF-TOKEN': getCSRFToken()
        },
        body: `productId=${productId}&quantity=${quantity}`
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            showToast('Đã cập nhật số lượng!', 'success');
        } else {
            showToast('Lỗi: ' + data.message, 'error');
            // Revert UI changes if server update failed
            location.reload();
        }
    })
    .catch(error => {
        console.error('Error:', error);
        showToast('Có lỗi xảy ra khi cập nhật số lượng', 'error');
        // Revert UI changes if server update failed
        location.reload();
    });
}

// Remove item via AJAX
function removeItem(productId) {
    if (confirm('Bạn có chắc chắn muốn xóa sản phẩm này khỏi giỏ hàng?')) {
        fetch('/cart/remove', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'X-CSRF-TOKEN': getCSRFToken()
            },
            body: `productId=${productId}`
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                // Remove item from UI real-time
                removeItemFromUI(productId);
                showToast('Đã xóa sản phẩm khỏi giỏ hàng!', 'success');
            } else {
                showToast('Lỗi: ' + data.message, 'error');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            showToast('Có lỗi xảy ra khi xóa sản phẩm', 'error');
        });
    }
}

// Clear all items via AJAX
function clearAllItems() {
    fetch('/cart/clear', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            'X-CSRF-TOKEN': getCSRFToken()
        }
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            // Clear all items from UI real-time
            clearAllItemsFromUI();
            showToast('Đã xóa tất cả sản phẩm khỏi giỏ hàng!', 'success');
        } else {
            showToast('Lỗi: ' + data.message, 'error');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        showToast('Có lỗi xảy ra khi xóa tất cả sản phẩm', 'error');
    });
}

// Update cart UI real-time
function updateCartUI(productId, quantity) {
    // Update quantity input
    const quantityInput = document.querySelector(`input[data-product-id="${productId}"]`);
    if (quantityInput) {
        quantityInput.value = quantity;
    }
    
    // Update total price for this item
    updateItemTotal(productId, quantity);
    
    // Update cart totals
    updateCartTotals();
}

// Update item total price
function updateItemTotal(productId, quantity) {
    // Find the cart item by product ID
    const quantityInput = document.querySelector(`input[data-product-id="${productId}"]`);
    if (!quantityInput) {
        console.log(`Quantity input not found for product ${productId}`);
        return;
    }
    
    const cartItem = quantityInput.closest('.cart-item');
    if (!cartItem) {
        console.log(`Cart item not found for product ${productId}`);
        return;
    }
    
    const unitPriceElement = cartItem.querySelector('.text-primary.fw-bold');
    const totalPriceElement = cartItem.querySelector('.fw-bold.text-success');
    
    console.log(`Found elements for product ${productId}:`, {
        unitPriceElement: unitPriceElement,
        totalPriceElement: totalPriceElement
    });
    
    if (unitPriceElement && totalPriceElement) {
        // Extract unit price from text (remove ₫ and commas)
        const unitPriceText = unitPriceElement.textContent.replace(/[₫,\s]/g, '').replace(/\./g, '');
        const unitPrice = parseFloat(unitPriceText);
        
        console.log(`Unit price text: "${unitPriceElement.textContent}" -> "${unitPriceText}" -> ${unitPrice}`);
        
        if (!isNaN(unitPrice)) {
            const totalPrice = unitPrice * quantity;
            totalPriceElement.textContent = formatCurrency(totalPrice);
            console.log(`Updated item ${productId}: ${unitPrice} x ${quantity} = ${totalPrice}`);
        } else {
            console.log(`Invalid unit price: ${unitPriceText}`);
        }
    } else {
        console.log(`Missing elements for product ${productId}`);
    }
}

// Update cart totals
function updateCartTotals() {
    let subtotal = 0;
    const cartItems = document.querySelectorAll('.cart-item');
    
    console.log(`Found ${cartItems.length} cart items`);
    
    cartItems.forEach((item, index) => {
        const totalPriceElement = item.querySelector('.fw-bold.text-success');
        if (totalPriceElement) {
            // Remove all non-numeric characters except dots
            const totalPriceText = totalPriceElement.textContent.replace(/[₫,\s]/g, '').replace(/\./g, '');
            const totalPrice = parseFloat(totalPriceText);
            console.log(`Item ${index}: "${totalPriceElement.textContent}" -> "${totalPriceText}" -> ${totalPrice}`);
            if (!isNaN(totalPrice)) {
                subtotal += totalPrice;
            }
        }
    });
    
    console.log(`Subtotal: ${subtotal}`);
    
    // Update subtotal - find the first span after "Tạm tính:"
    const subtotalElement = document.querySelector('.total-section .d-flex.justify-content-between:first-of-type span:last-child');
    if (subtotalElement) {
        subtotalElement.textContent = formatCurrency(subtotal);
        console.log(`Updated subtotal: ${formatCurrency(subtotal)}`);
    } else {
        console.log('Subtotal element not found');
    }
    
    // Update tax (10%)
    const tax = subtotal * 0.1;
    const taxElement = document.querySelector('.total-section .d-flex.justify-content-between:nth-of-type(3) span:last-child');
    if (taxElement) {
        taxElement.textContent = formatCurrency(tax);
        console.log(`Updated tax: ${formatCurrency(tax)}`);
    } else {
        console.log('Tax element not found');
    }
    
    // Update total
    const total = subtotal + tax; // Shipping is free
    const totalElement = document.querySelector('.total-section .text-primary.fs-5');
    if (totalElement) {
        totalElement.textContent = formatCurrency(total);
        console.log(`Updated total: ${formatCurrency(total)}`);
    } else {
        console.log('Total element not found');
        // Try alternative selector
        const altTotalElement = document.querySelector('.total-section strong.text-primary');
        if (altTotalElement) {
            altTotalElement.textContent = formatCurrency(total);
            console.log(`Updated total with alt selector: ${formatCurrency(total)}`);
        } else {
            console.log('Alternative total element also not found');
        }
    }
    
    // Update cart count in header - calculate total quantity
    let totalQuantity = 0;
    cartItems.forEach(item => {
        const quantityInput = item.querySelector('.quantity-input');
        if (quantityInput) {
            totalQuantity += parseInt(quantityInput.value) || 0;
        }
    });
    updateCartCount(totalQuantity);
}

// Remove item from UI
function removeItemFromUI(productId) {
    const cartItem = document.querySelector(`[data-product-id="${productId}"]`).closest('.cart-item');
    if (cartItem) {
        // Add fade out animation
        cartItem.style.transition = 'all 0.3s ease';
        cartItem.style.opacity = '0';
        cartItem.style.transform = 'translateX(-100%)';
        
        setTimeout(() => {
            cartItem.remove();
            updateCartTotals();
            
            // Check if cart is empty
            const remainingItems = document.querySelectorAll('.cart-item');
            if (remainingItems.length === 0) {
                showEmptyCart();
            }
        }, 300);
    }
}

// Clear all items from UI
function clearAllItemsFromUI() {
    const cartItems = document.querySelectorAll('.cart-item');
    cartItems.forEach((item, index) => {
        setTimeout(() => {
            item.style.transition = 'all 0.3s ease';
            item.style.opacity = '0';
            item.style.transform = 'translateX(-100%)';
            
            setTimeout(() => {
                item.remove();
                if (index === cartItems.length - 1) {
                    showEmptyCart();
                }
            }, 100);
        }, index * 100);
    });
    
    // Update cart count
    updateCartCount(0);
}

// Show empty cart message
function showEmptyCart() {
    const cartContainer = document.querySelector('.col-lg-8');
    if (cartContainer) {
        cartContainer.innerHTML = `
            <div class="text-center py-5">
                <i class="fas fa-shopping-cart fa-3x text-muted mb-3"></i>
                <h4 class="text-muted">Giỏ hàng trống</h4>
                <p class="text-muted">Bạn chưa có sản phẩm nào trong giỏ hàng</p>
                <a href="/" class="btn btn-primary">
                    <i class="fas fa-arrow-left me-2"></i>Tiếp tục mua sắm
                </a>
            </div>
        `;
    }
}

// Update cart count in header
function updateCartCount(count) {
    const cartCountElement = document.querySelector('.navbar .nav-link[href="/cart"]');
    if (cartCountElement) {
        if (count > 0) {
            cartCountElement.innerHTML = `<i class="fas fa-shopping-cart me-1"></i>Giỏ hàng (${count})`;
        } else {
            cartCountElement.innerHTML = `<i class="fas fa-shopping-cart me-1"></i>Giỏ hàng`;
        }
    }
}

// Format currency
function formatCurrency(amount) {
    // Simple format for VND
    return new Intl.NumberFormat('vi-VN').format(amount) + ' ₫';
}

// Get CSRF token
function getCSRFToken() {
    const token = document.querySelector('meta[name="_csrf"]');
    return token ? token.getAttribute('content') : '';
}

// Show toast notification
function showToast(message, type = 'info') {
    const toastElement = document.getElementById('cartToast');
    const toastBody = document.getElementById('toastMessage');
    
    if (toastElement && toastBody) {
        toastBody.textContent = message;
        
        // Set toast color based on type
        const toastHeader = toastElement.querySelector('.toast-header');
        if (type === 'error') {
            toastHeader.className = 'toast-header bg-danger text-white';
        } else if (type === 'success') {
            toastHeader.className = 'toast-header bg-success text-white';
        } else {
            toastHeader.className = 'toast-header';
        }
        
        const toast = new bootstrap.Toast(toastElement);
        toast.show();
    }
}

// Add to cart function (for suggested products)
function addToCart(productId) {
    fetch('/cart/add', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            'X-CSRF-TOKEN': getCSRFToken()
        },
        body: `productId=${productId}&quantity=1`
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            showToast(data.message, 'success');
            // Reload page to update cart
            setTimeout(() => location.reload(), 1000);
        } else {
            showToast('Lỗi: ' + data.message, 'error');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        showToast('Có lỗi xảy ra khi thêm vào giỏ hàng', 'error');
    });
}