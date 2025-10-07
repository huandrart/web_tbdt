/**
 * Category Products Page JavaScript
 */

$(document).ready(function() {
    // Initialize cart count
    updateCartCount();
    
    // Sort functionality
    $('#sortBy').on('change', function() {
        const sortValue = $(this).val();
        if (sortValue) {
            const currentUrl = new URL(window.location);
            currentUrl.searchParams.set('sort', sortValue);
            window.location.href = currentUrl.toString();
        }
    });
    
    // Add to cart functionality
    $('.btn-primary').on('click', function(e) {
        e.preventDefault();
        const productId = $(this).data('product-id');
        if (productId) {
            addToCart(productId);
        }
    });
    
    // Filter form submission
    $('.filter-sidebar form').on('submit', function(e) {
        e.preventDefault();
        const formData = new FormData(this);
        const params = new URLSearchParams();
        
        for (let [key, value] of formData.entries()) {
            if (value) {
                params.set(key, value);
            }
        }
        
        const currentUrl = new URL(window.location);
        currentUrl.search = params.toString();
        window.location.href = currentUrl.toString();
    });
});

/**
 * Add product to cart
 */
function addToCart(productId) {
    $.ajax({
        url: '/cart/add',
        method: 'POST',
        data: {
            productId: productId,
            quantity: 1,
            _token: $('meta[name="csrf-token"]').attr('content')
        },
        success: function(response) {
            if (response.success) {
                updateCartCount();
                showToast('Đã thêm sản phẩm vào giỏ hàng!', 'success');
            } else {
                showToast('Có lỗi xảy ra: ' + response.message, 'error');
            }
        },
        error: function() {
            showToast('Có lỗi xảy ra khi thêm sản phẩm vào giỏ hàng', 'error');
        }
    });
}

/**
 * Update cart count in header
 */
function updateCartCount() {
    $.ajax({
        url: '/cart/count',
        method: 'GET',
        success: function(count) {
            $('#cartCount').text(count);
        },
        error: function() {
            $('#cartCount').text('0');
        }
    });
}

/**
 * Show toast notification
 */
function showToast(message, type = 'info') {
    const toastHtml = `
        <div class="toast align-items-center text-white bg-${type === 'success' ? 'success' : 'danger'} border-0" role="alert">
            <div class="d-flex">
                <div class="toast-body">
                    <i class="fas fa-${type === 'success' ? 'check-circle' : 'exclamation-triangle'} me-2"></i>
                    ${message}
                </div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
            </div>
        </div>
    `;
    
    $('.toast-container').append(toastHtml);
    $('.toast').last().toast('show');
    
    // Auto remove after 3 seconds
    setTimeout(function() {
        $('.toast').last().remove();
    }, 3000);
}

/**
 * Initialize tooltips
 */
$(function () {
    $('[data-bs-toggle="tooltip"]').tooltip();
});

/**
 * Smooth scroll for anchor links
 */
$('a[href^="#"]').on('click', function(event) {
    var target = $(this.getAttribute('href'));
    if (target.length) {
        event.preventDefault();
        $('html, body').stop().animate({
            scrollTop: target.offset().top - 100
        }, 1000);
    }
});
