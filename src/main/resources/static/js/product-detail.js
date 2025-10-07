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
            const csrfToken = document.querySelector('meta[name="csrf-token"]').getAttribute('content');
            
            $.ajax({
                url: '/cart/add',
                method: 'POST',
                headers: {
                    'X-CSRF-TOKEN': csrfToken
                },
                data: {
                    productId: productId,
                    quantity: quantity
                },
                success: function(response) {
                    const result = typeof response === 'string' ? JSON.parse(response) : response;
                    if (result.success) {
                        showToast(result.message || 'Đã thêm sản phẩm vào giỏ hàng!', 'success');
                        updateCartCount();
                    } else {
                        showToast(result.message || 'Có lỗi xảy ra', 'error');
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
            const csrfToken = document.querySelector('meta[name="csrf-token"]').getAttribute('content');
            
            // Add to cart first, then redirect to checkout
            $.ajax({
                url: '/cart/add',
                method: 'POST',
                headers: {
                    'X-CSRF-TOKEN': csrfToken
                },
                data: {
                    productId: productId,
                    quantity: quantity
                },
                success: function(response) {
                    const result = typeof response === 'string' ? JSON.parse(response) : response;
                    if (result.success) {
                        window.location.href = '/checkout';
                    } else {
                        showToast(result.message || 'Có lỗi xảy ra', 'error');
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
        
        // Review rating stars
        function initRatingStars() {
            const stars = document.querySelectorAll('.rating-input i');
            const ratingInput = document.getElementById('rating');
            
            stars.forEach((star, index) => {
                star.addEventListener('click', function() {
                    const rating = parseInt(this.getAttribute('data-rating'));
                    ratingInput.value = rating;
                    
                    // Update star display
                    stars.forEach((s, i) => {
                        if (i < rating) {
                            s.className = 'fas fa-star';
                        } else {
                            s.className = 'far fa-star';
                        }
                    });
                });
                
                star.addEventListener('mouseenter', function() {
                    const rating = parseInt(this.getAttribute('data-rating'));
                    stars.forEach((s, i) => {
                        if (i < rating) {
                            s.className = 'fas fa-star';
                        } else {
                            s.className = 'far fa-star';
                        }
                    });
                });
            });
            
            // Reset stars on mouse leave
            document.querySelector('.rating-input').addEventListener('mouseleave', function() {
                const currentRating = parseInt(ratingInput.value);
                stars.forEach((s, i) => {
                    if (i < currentRating) {
                        s.className = 'fas fa-star';
                    } else {
                        s.className = 'far fa-star';
                    }
                });
            });
        }
        
        // Submit review form
        function initReviewForm() {
            const reviewForm = document.getElementById('reviewForm');
            if (reviewForm) {
                reviewForm.addEventListener('submit', function(e) {
                    e.preventDefault();
                    
                    const productId = window.location.pathname.split('/').pop();
                    const rating = document.getElementById('rating').value;
                    const comment = document.getElementById('comment').value;
                    const csrfToken = document.querySelector('meta[name="csrf-token"]').getAttribute('content');
                    
                    if (!comment.trim()) {
                        showToast('Vui lòng nhập nhận xét!', 'error');
                        return;
                    }
                    
                    $.ajax({
                        url: '/reviews/add',
                        method: 'POST',
                        headers: {
                            'X-CSRF-TOKEN': csrfToken
                        },
                        data: {
                            productId: productId,
                            rating: rating,
                            comment: comment
                        },
                        success: function(response) {
                            const result = typeof response === 'string' ? JSON.parse(response) : response;
                            if (result.success) {
                                showToast(result.message, 'success');
                                // Reset form
                                document.getElementById('comment').value = '';
                                document.getElementById('rating').value = '5';
                                // Reset stars
                                document.querySelectorAll('.rating-input i').forEach((s, i) => {
                                    if (i < 5) {
                                        s.className = 'fas fa-star';
                                    } else {
                                        s.className = 'far fa-star';
                                    }
                                });
                                // Reload page after 2 seconds to show new review
                                setTimeout(() => {
                                    window.location.reload();
                                }, 2000);
                            } else {
                                showToast(result.message, 'error');
                            }
                        },
                        error: function() {
                            showToast('Có lỗi xảy ra khi gửi đánh giá', 'error');
                        }
                    });
                });
            }
        }
        
        // Load cart count on page load
        document.addEventListener('DOMContentLoaded', function() {
            updateCartCount();
            initRatingStars();
            initReviewForm();
        });
    