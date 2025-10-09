// Order Place Page JavaScript
document.addEventListener('DOMContentLoaded', function() {
    // Initialize form handling
    initOrderForm();
    
    // Initialize payment method selection
    initPaymentMethods();
});

// Initialize order form
function initOrderForm() {
    const orderForm = document.getElementById('orderForm');
    if (orderForm) {
        orderForm.addEventListener('submit', function(e) {
            e.preventDefault();
            handleOrderSubmission();
        });
    }
}

// Initialize payment method selection
function initPaymentMethods() {
    const paymentOptions = document.querySelectorAll('input[name="paymentMethod"]');
    
    paymentOptions.forEach(option => {
        option.addEventListener('change', function() {
            // Remove active class from all payment cards
            document.querySelectorAll('.payment-card').forEach(card => {
                card.classList.remove('active');
            });
            
            // Add active class to selected payment card
            const selectedCard = this.closest('.payment-option').querySelector('.payment-card');
            if (selectedCard) {
                selectedCard.classList.add('active');
            }
            
            // Show specific warnings for certain payment methods
            showPaymentMethodInfo(this.value);
        });
    });
    
    // Initialize first payment method as active
    const firstOption = document.querySelector('input[name="paymentMethod"]:checked');
    if (firstOption) {
        const firstCard = firstOption.closest('.payment-option').querySelector('.payment-card');
        if (firstCard) {
            firstCard.classList.add('active');
        }
        showPaymentMethodInfo(firstOption.value);
    }
}

// Show payment method specific information
function showPaymentMethodInfo(paymentMethod) {
    // Remove any existing info messages
    const existingInfo = document.querySelector('.payment-info-message');
    if (existingInfo) {
        existingInfo.remove();
    }
    
    let message = '';
    let messageType = 'info';
    
    switch (paymentMethod) {
        case 'MOMO':
            message = 'Thanh toán qua ví MoMo sẽ được chuyển hướng đến trang thanh toán MoMo.';
            messageType = 'info';
            break;
        case 'COD':
            message = 'Bạn sẽ thanh toán bằng tiền mặt khi nhận hàng. Phí COD: 0₫';
            messageType = 'success';
            break;
        case 'BANK_TRANSFER':
            message = 'Vui lòng đảm bảo bạn đã thêm thông tin thẻ ngân hàng trong hồ sơ cá nhân.';
            messageType = 'warning';
            break;
    }
    
    if (message) {
        showPaymentInfoMessage(message, messageType);
    }
}

// Show payment info message
function showPaymentInfoMessage(message, type) {
    const paymentMethods = document.querySelector('.payment-methods');
    if (paymentMethods) {
        const infoDiv = document.createElement('div');
        infoDiv.className = `alert alert-${type} payment-info-message mt-3`;
        infoDiv.innerHTML = `
            <i class="fas fa-info-circle me-2"></i>
            ${message}
        `;
        paymentMethods.appendChild(infoDiv);
    }
}

// Handle order submission
function handleOrderSubmission() {
    const submitBtn = document.querySelector('button[type="submit"]');
    const originalText = submitBtn.innerHTML;
    
    // Show loading state
    submitBtn.disabled = true;
    submitBtn.classList.add('btn-loading');
    submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>Đang xử lý...';
    
    // Get selected payment method
    const selectedPayment = document.querySelector('input[name="paymentMethod"]:checked');
    if (!selectedPayment) {
        showToast('Vui lòng chọn phương thức thanh toán!', 'error');
        resetSubmitButton(submitBtn, originalText);
        return;
    }
    
    // Validate payment method specific requirements
    if (!validatePaymentMethod(selectedPayment.value)) {
        resetSubmitButton(submitBtn, originalText);
        return;
    }
    
    // Submit form
    const form = document.getElementById('orderForm');
    const formData = new FormData(form);
    
    fetch('/order/place', {
        method: 'POST',
        headers: {
            'X-CSRF-TOKEN': getCSRFToken()
        },
        body: formData
    })
    .then(response => {
        if (response.redirected) {
            // Handle redirect (success or error)
            window.location.href = response.url;
        } else {
            return response.text();
        }
    })
    .then(data => {
        if (data) {
            // Handle non-redirect response
            showToast('Đặt hàng thành công!', 'success');
            setTimeout(() => {
                window.location.href = '/orders/my-orders';
            }, 2000);
        }
    })
    .catch(error => {
        console.error('Error:', error);
        showToast('Có lỗi xảy ra khi đặt hàng. Vui lòng thử lại!', 'error');
        resetSubmitButton(submitBtn, originalText);
    });
}

// Validate payment method
function validatePaymentMethod(paymentMethod) {
    switch (paymentMethod) {
        case 'BANK_TRANSFER':
            // Check if user has bank card info (this would be a real check in production)
            showToast('Tính năng chuyển khoản ngân hàng đang được phát triển. Vui lòng chọn phương thức khác!', 'warning');
            return false;
        case 'MOMO':
            // MoMo validation would go here
            return true;
        case 'COD':
            // COD validation would go here
            return true;
        default:
            return true;
    }
}

// Reset submit button
function resetSubmitButton(button, originalText) {
    button.disabled = false;
    button.classList.remove('btn-loading');
    button.innerHTML = originalText;
}

// Get CSRF token
function getCSRFToken() {
    const token = document.querySelector('meta[name="_csrf"]');
    return token ? token.getAttribute('content') : '';
}

// Show toast notification
function showToast(message, type = 'info') {
    const toastElement = document.getElementById('orderToast');
    const toastBody = document.getElementById('toastMessage');
    
    if (toastElement && toastBody) {
        toastBody.textContent = message;
        
        // Set toast color based on type
        const toastHeader = toastElement.querySelector('.toast-header');
        if (type === 'error') {
            toastHeader.className = 'toast-header bg-danger text-white';
        } else if (type === 'success') {
            toastHeader.className = 'toast-header bg-success text-white';
        } else if (type === 'warning') {
            toastHeader.className = 'toast-header bg-warning text-dark';
        } else {
            toastHeader.className = 'toast-header';
        }
        
        const toast = new bootstrap.Toast(toastElement);
        toast.show();
    }
}

// Add smooth scrolling for better UX
function smoothScrollTo(element) {
    element.scrollIntoView({
        behavior: 'smooth',
        block: 'start'
    });
}

// Handle payment method change with animation
function animatePaymentChange() {
    const paymentCards = document.querySelectorAll('.payment-card');
    paymentCards.forEach(card => {
        card.addEventListener('click', function() {
            // Remove active class from all cards
            paymentCards.forEach(c => c.classList.remove('active'));
            // Add active class to clicked card
            this.classList.add('active');
            
            // Trigger the radio button
            const radio = this.closest('.payment-option').querySelector('input[type="radio"]');
            if (radio) {
                radio.checked = true;
                radio.dispatchEvent(new Event('change'));
            }
        });
    });
}

// Initialize animations
document.addEventListener('DOMContentLoaded', function() {
    animatePaymentChange();
});

