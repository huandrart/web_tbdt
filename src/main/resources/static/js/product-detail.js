/**
 * product/detail.html JavaScript
 */


        // Change main image
        function changeMainImage(src) {
            document.getElementById('mainImage').src = src;
            
            // Update active thumbnail
            document.querySelectorAll('.thumbnail').forEach(thumb => {
                thumb.classList.remove('active');
            });
            event.target.classList.add('active');
        }
        
        // Image zoom functionality
        document.getElementById('mainImage').addEventListener('click', function() {
            const overlay = document.createElement('div');
            overlay.className = 'zoom-overlay';
            overlay.innerHTML = `<img src="${this.src}" class="zoom-image" alt="Zoomed Image">`;
            document.body.appendChild(overlay);
            
            overlay.addEventListener('click', function() {
                document.body.removeChild(overlay);
            });
        });
        
        // Quantity controls
        function changeQuantity(change) {
            const quantityInput = document.getElementById('quantity');
            let currentQuantity = parseInt(quantityInput.value);
            let newQuantity = currentQuantity + change;
            
            if (newQuantity < 1) newQuantity = 1;
            if (newQuantity > parseInt(quantityInput.max)) newQuantity = parseInt(quantityInput.max);
            
            quantityInput.value = newQuantity;
        }
        
        // Add to cart
        function addToCart(productId) {
            const quantity = document.getElementById('quantity').value;
            
            $.ajax({
                url: '/cart/add',
                method: 'POST',
                data: {
                    productId: productId,
                    quantity: quantity
                },
                success: function(response) {
                    if (response.success) {
                        showToast('Đã thêm sản phẩm vào giỏ hàng!', 'success');
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
        
        // Buy now
        function buyNow(productId) {
            const quantity = document.getElementById('quantity').value;
            
            // Add to cart first, then redirect to checkout
            $.ajax({
                url: '/cart/add',
                method: 'POST',
                data: {
                    productId: productId,
                    quantity: quantity
                },
                success: function(response) {
                    if (response.success) {
                        window.location.href = '/checkout';
                    } else {
                        showToast('Có lỗi xảy ra: ' + response.message, 'error');
                    }
                },
                error: function() {
                    showToast('Có lỗi xảy ra khi mua hàng', 'error');
                }
            });
        }
        
        // Add to wishlist
        function addToWishlist() {
            showToast('Đã thêm vào danh sách yêu thích!', 'success');
        }
        
        // Compare product
        function compareProduct() {
            showToast('Đã thêm vào danh sách so sánh!', 'info');
        }
        
        // Share product
        function shareProduct() {
            if (navigator.share) {
                navigator.share({
                    title: document.title,
                    url: window.location.href
                });
            } else {
                // Fallback - copy to clipboard
                navigator.clipboard.writeText(window.location.href);
                showToast('Đã sao chép đường link!', 'info');
            }
        }
        
        // Show toast notification
        function showToast(message, type = 'info') {
            const toastElement = document.getElementById('productToast');
            const toastMessage = document.getElementById('toastMessage');
            
            toastMessage.textContent = message;
            
            // Change toast color based on type
            toastElement.className = 'toast';
            if (type === 'success') {
                toastElement.classList.add('bg-success', 'text-white');
            } else if (type === 'error') {
                toastElement.classList.add('bg-danger', 'text-white');
            } else if (type === 'info') {
                toastElement.classList.add('bg-info', 'text-white');
            }
            
            const toast = new bootstrap.Toast(toastElement);
            toast.show();
        }
        
        // Update cart count
        function updateCartCount() {
            $.get('/cart/count', function(count) {
                document.getElementById('cartCount').textContent = count;
            });
        }
        
        // Load cart count on page load
        document.addEventListener('DOMContentLoaded', function() {
            updateCartCount();
        });
    