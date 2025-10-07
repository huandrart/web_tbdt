/**
 * cart/checkout.html JavaScript
 */


        // Payment method selection
        function selectPayment(method) {
            document.querySelectorAll('.payment-method').forEach(el => {
                el.classList.remove('selected');
            });
            event.currentTarget.classList.add('selected');
            document.getElementById(method).checked = true;
        }
        
        // Shipping method change handler
        document.querySelectorAll('input[name="shippingMethod"]').forEach(radio => {
            radio.addEventListener('change', function() {
                updateShippingFee();
            });
        });
        
        // Update shipping fee
        function updateShippingFee() {
            const selectedShipping = document.querySelector('input[name="shippingMethod"]:checked').value;
            const shippingFeeElement = document.getElementById('shippingFee');
            let shippingFee = 0;
            
            switch(selectedShipping) {
                case 'standard':
                    shippingFeeElement.innerHTML = '<span class="text-success">Miễn phí</span>';
                    break;
                case 'express':
                    shippingFee = 50000;
                    shippingFeeElement.textContent = '50.000 ₫';
                    break;
                case 'same-day':
                    shippingFee = 100000;
                    shippingFeeElement.textContent = '100.000 ₫';
                    break;
            }
            
            updateTotal(shippingFee);
        }
        
        // Update total amount
        function updateTotal(shippingFee = 0) {
            const subtotal = parseFloat('[[${{subtotal}}]]' || 0);
            const tax = parseFloat('[[${{tax}}]]' || 0);
            const discount = parseFloat(document.getElementById('discountAmount')?.textContent?.replace(/[^\d]/g, '') || 0);
            
            const total = subtotal + tax + shippingFee - discount;
            document.getElementById('totalAmount').textContent = total.toLocaleString('vi-VN') + ' ₫';
        }
        
        // Form validation
        document.getElementById('checkoutForm').addEventListener('submit', function(e) {
            const terms = document.getElementById('terms').checked;
            
            if (!terms) {
                e.preventDefault();
                alert('Vui lòng đồng ý với điều khoản và điều kiện');
                return false;
            }
            
            // Show loading state
            const submitBtn = document.getElementById('checkoutBtn');
            submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>Đang xử lý...';
            submitBtn.disabled = true;
        });
        
        // Location dropdowns
        const locations = {
            'HCM': {
                'Quận 1': ['Phường Bến Nghé', 'Phường Bến Thành', 'Phường Cầu Kho'],
                'Quận 3': ['Phường Võ Thị Sáu', 'Phường 01', 'Phường 02'],
                'Quận 7': ['Phường Tân Thuận Đông', 'Phường Tân Thuận Tây', 'Phường Tân Kiểng']
            },
            'HN': {
                'Quận Hoàn Kiếm': ['Phường Hàng Bài', 'Phường Hàng Bồ', 'Phường Hàng Gai'],
                'Quận Ba Đình': ['Phường Phúc Xá', 'Phường Trúc Bạch', 'Phường Vĩnh Phúc'],
                'Quận Cầu Giấy': ['Phường Dịch Vọng', 'Phường Mai Dịch', 'Phường Nghĩa Đô']
            }
        };
        
        document.getElementById('city').addEventListener('change', function() {
            const city = this.value;
            const districtSelect = document.getElementById('district');
            const wardSelect = document.getElementById('ward');
            
            districtSelect.innerHTML = '<option value="">Chọn quận/huyện</option>';
            wardSelect.innerHTML = '<option value="">Chọn phường/xã</option>';
            
            if (locations[city]) {
                Object.keys(locations[city]).forEach(district => {
                    districtSelect.innerHTML += `<option value="${district}">${district}</option>`;
                });
            }
        });
        
        document.getElementById('district').addEventListener('change', function() {
            const city = document.getElementById('city').value;
            const district = this.value;
            const wardSelect = document.getElementById('ward');
            
            wardSelect.innerHTML = '<option value="">Chọn phường/xã</option>';
            
            if (locations[city] && locations[city][district]) {
                locations[city][district].forEach(ward => {
                    wardSelect.innerHTML += `<option value="${ward}">${ward}</option>`;
                });
            }
        });
        
        // Initialize payment method selection
        document.addEventListener('DOMContentLoaded', function() {
            document.querySelector('.payment-method').click();
        });
    