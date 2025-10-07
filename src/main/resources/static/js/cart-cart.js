/**
 * cart/cart.html JavaScript
 */


        // Update quantity
        function updateQuantity(productId, change) {
            const quantityInput = document.getElementById('quantity-' + productId);
            let currentQuantity = parseInt(quantityInput.value);
            let newQuantity = currentQuantity + change;
            
            if (newQuantity < 1) newQuantity = 1;
            if (newQuantity > 99) newQuantity = 99;
            
            quantityInput.value = newQuantity;
            updateCartItem(productId, newQuantity);
        }
        
        // Update cart item via AJAX
        function updateCartItem(productId, quantity) {
            $.ajax({
                url: '/cart/update',
                method: 'POST',
                data: {
                    productId: productId,
                    quantity: quantity
                },
                success: function(response) {
                    if (response.success) {
                        location.reload();
                    } else {
                        showToast('Có lỗi xảy ra: ' + response.message, 'error');
                    }
                },
                error: function() {
                    showToast('Có lỗi xảy ra khi cập nhật giỏ hàng', 'error');
                }
            });
        }
        
        // Remove from cart
        function removeFromCart(productId) {
            if (confirm('Bạn có chắc muốn xóa sản phẩm này khỏi giỏ hàng?')) {
                $.ajax({
                    url: '/cart/remove',
                    method: 'POST',
                    data: { productId: productId },
                    success: function(response) {
                        if (response.success) {
                            location.reload();
                        } else {
                            showToast('Có lỗi xảy ra: ' + response.message, 'error');
                        }
                    },
                    error: function() {
                        showToast('Có lỗi xảy ra khi xóa sản phẩm', 'error');
                    }
                });
            }
        }
        
        // Clear cart
        function clearCart() {
            if (confirm('Bạn có chắc muốn xóa tất cả sản phẩm khỏi giỏ hàng?')) {
                $.ajax({
                    url: '/cart/clear',
                    method: 'POST',
                    success: function(response) {
                        if (response.success) {
                            location.reload();
                        } else {
                            showToast('Có lỗi xảy ra: ' + response.message, 'error');
                        }
                    },
                    error: function() {
                        showToast('Có lỗi xảy ra khi xóa giỏ hàng', 'error');
                    }
                });
            }
        }
        
        // Add to cart (for suggested products)
        function addToCart(productId) {
            $.ajax({
                url: '/cart/add',
                method: 'POST',
                data: { productId: productId, quantity: 1 },
                success: function(response) {
                    if (response.success) {
                        showToast('Đã thêm sản phẩm vào giỏ hàng', 'success');
                        setTimeout(() => location.reload(), 1000);
                    } else {
                        showToast('Có lỗi xảy ra: ' + response.message, 'error');
                    }
                },
                error: function() {
                    showToast('Có lỗi xảy ra khi thêm sản phẩm', 'error');
                }
            });
        }
        
        // Apply discount code
        function applyDiscount() {
            const discountCode = document.getElementById('discountCode').value;
            if (!discountCode.trim()) {
                showToast('Vui lòng nhập mã giảm giá', 'error');
                return;
            }
            
            $.ajax({
                url: '/cart/apply-discount',
                method: 'POST',
                data: { discountCode: discountCode },
                success: function(response) {
                    if (response.success) {
                        showToast('Áp dụng mã giảm giá thành công', 'success');
                        setTimeout(() => location.reload(), 1000);
                    } else {
                        showToast('Mã giảm giá không hợp lệ', 'error');
                    }
                },
                error: function() {
                    showToast('Có lỗi xảy ra khi áp dụng mã giảm giá', 'error');
                }
            });
        }
        
        // Show toast notification
        function showToast(message, type = 'info') {
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
        
        // Handle quantity input change
        document.querySelectorAll('.quantity-input').forEach(input => {
            input.addEventListener('change', function() {
                const productId = this.id.replace('quantity-', '');
                const quantity = parseInt(this.value);
                if (quantity > 0 && quantity <= 99) {
                    updateCartItem(productId, quantity);
                } else {
                    this.value = 1;
                }
            });
        });
    