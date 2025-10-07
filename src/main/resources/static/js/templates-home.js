/**
 * templates/home.html JavaScript
 */


        // Add to cart function
        function addToCart(productId) {
            $.ajax({
                url: '/cart/add',
                method: 'POST',
                data: {
                    productId: productId,
                    quantity: 1
                },
                success: function(response) {
                    if (response.success) {
                        showToast('Đã thêm sản phẩm vào giỏ hàng!');
                        updateCartCount();
                    } else {
                        showToast('Có lỗi xảy ra: ' + response.message, 'error');
                    }
                },
                error: function() {
                    showToast('Có lỗi xảy ra khi thêm sản phẩm', 'error');
                }
            });
        }
        
        // Show toast notification
        function showToast(message, type = 'success') {
            const toastElement = document.getElementById('cartToast');
            const toastMessage = document.getElementById('toastMessage');
            
            toastMessage.textContent = message;
            
            // Change toast color based on type
            toastElement.className = 'toast';
            if (type === 'success') {
                toastElement.classList.add('bg-success', 'text-white');
            } else if (type === 'error') {
                toastElement.classList.add('bg-danger', 'text-white');
            }
            
            const toast = new bootstrap.Toast(toastElement);
            toast.show();
        }
        
        // Update cart count
        function updateCartCount() {
            $.get('/cart/count', function(count) {
                document.getElementById('cartCount').textContent = count;
            }).fail(function() {
                document.getElementById('cartCount').textContent = '0';
            });
        }
        
        // Load cart count on page load
        document.addEventListener('DOMContentLoaded', function() {
            updateCartCount();
        });
        
        // Smooth scrolling for anchor links
        document.querySelectorAll('a[href^="#"]').forEach(anchor => {
            anchor.addEventListener('click', function (e) {
                e.preventDefault();
                const target = document.querySelector(this.getAttribute('href'));
                if (target) {
                    target.scrollIntoView({
                        behavior: 'smooth',
                        block: 'start'
                    });
                }
            });
        });
    