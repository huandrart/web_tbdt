// Templates Products JavaScript

document.addEventListener('DOMContentLoaded', function() {

    console.log('Templates Products JS loaded - Version Fix Filter');



    // ==========================================

    // 1. CÁC CHỨC NĂNG CƠ BẢN (GIỎ HÀNG, NOTI...)

    // ==========================================

    

    updateCartCount();

    

    window.addToCart = function(productId) {

        // ... (Giữ nguyên code phần Add to Cart của bạn ở đây nếu cần, hoặc copy từ bài trước) ...

        // Để ngắn gọn tôi tập trung vào phần sửa lỗi bên dưới

        console.log('Adding product to cart:', productId);

        const button = document.querySelector(`button[onclick="addToCart(${productId})"]`);

        let originalContent = '';

        if (button) {

            originalContent = button.innerHTML;

            button.innerHTML = '<i class="fas fa-spinner fa-spin"></i>';

            button.disabled = true;

        }

        const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');

        

        $.ajax({

            url: '/cart/add',

            method: 'POST',

            headers: { 'X-CSRF-TOKEN': csrfToken },

            data: { productId: productId, quantity: 1 },

            success: function(response) {

                const result = typeof response === 'string' ? JSON.parse(response) : response;

                if (result.success) {

                    showNotification(result.message || 'Đã thêm vào giỏ!', 'success');

                    updateCartCount();

                } else {

                    showNotification(result.message || 'Lỗi', 'error');

                }

                if (button) { button.innerHTML = originalContent; button.disabled = false; }

            },

            error: function() {

                showNotification('Lỗi kết nối', 'error');

                if (button) { button.innerHTML = originalContent; button.disabled = false; }

            }

        });

    };



    function updateCartCount() {

        $.ajax({

            url: '/cart/count', method: 'GET',

            success: function(data) {

                const el = document.getElementById('cartCount');

                if (el) el.textContent = data.count || 0;

            }

        });

    }



    function showNotification(message, type) {

        // ... (Giữ nguyên code notification của bạn) ...

        const div = document.createElement('div');

        div.className = `alert alert-${type === 'success' ? 'success' : 'danger'} cart-notification`;

        div.style.cssText = 'position: fixed; top: 20px; right: 20px; z-index: 9999;';

        div.innerHTML = `<span>${message}</span>`;

        document.body.appendChild(div);

        setTimeout(() => div.remove(), 3000);

    }



    // ==========================================

    // 2. PHẦN SỬA LỖI QUAN TRỌNG NHẤT

    // ==========================================



    // Tìm tất cả radio button của danh mục

    const categoryRadios = document.querySelectorAll('input[name="categoryId"]');

    

    // Tìm Form bộ lọc (Form chứa nút Áp dụng và các Radio này)

    // Cách tìm chắc chắn nhất: Tìm form cha của radio button đầu tiên

    let filterForm = null;

    if(categoryRadios.length > 0) {

        filterForm = categoryRadios[0].closest('form');

    }



    // A. Xử lý khi bấm chọn Radio Button

    categoryRadios.forEach(radio => {

        radio.addEventListener('change', function() {

            console.log("Radio thay đổi, đang tìm input ẩn để xóa...");

            

            // Tìm input keyword nằm CHÍNH TRONG FORM NÀY (tránh nhầm với thanh tìm kiếm ở header)

            const currentForm = this.closest('form');

            if (currentForm) {

                const hiddenKeyword = currentForm.querySelector('input[name="keyword"]');

                if (hiddenKeyword) {

                    console.log("Đã tìm thấy input ẩn, giá trị cũ:", hiddenKeyword.value);

                    hiddenKeyword.value = ""; // XÓA GIÁ TRỊ

                    console.log("Đã xóa giá trị input ẩn.");

                }

            }

        });

    });



    // B. Xử lý dự phòng: Khi bấm nút Submit (Nút "Áp dụng")

    if (filterForm) {

        filterForm.addEventListener('submit', function(e) {

            // Kiểm tra xem có radio danh mục nào đang được chọn không (khác Tất cả)

            // Logic: Nếu người dùng đang lọc theo danh mục cụ thể, ta nên ưu tiên danh mục và bỏ từ khóa cũ

            

            const checkedCategory = filterForm.querySelector('input[name="categoryId"]:checked');

            const hiddenKeyword = filterForm.querySelector('input[name="keyword"]');



            // Nếu có input keyword trong form VÀ người dùng đã chọn một danh mục (value khác rỗng/0)

            if (hiddenKeyword && checkedCategory && checkedCategory.value && checkedCategory.value !== "") {

                // Bạn có thể tùy chỉnh logic: Chỉ xóa nếu người dùng chọn danh mục khác

                // Nhưng ở đây ta xóa luôn để đảm bảo nút lọc hoạt động đúng ý bạn

                console.log("Submit Form: Xóa keyword để ưu tiên bộ lọc danh mục");

                hiddenKeyword.value = ""; 

            }

        });

    }

    

    // Giữ lại chức năng sort

    const sortSelect = document.getElementById('sortBy');

    if (sortSelect) {

        sortSelect.addEventListener('change', function() {

            const url = new URL(window.location);

            url.searchParams.set('sortBy', this.value);

            window.location.href = url.toString();

        });

    }

});

