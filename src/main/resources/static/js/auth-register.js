/**
 * auth/register.html JavaScript
 */


        // Toggle password visibility
        function togglePassword() {
            const passwordField = document.getElementById('password');
            const toggleIcon = document.getElementById('togglePasswordIcon');
            
            if (passwordField.type === 'password') {
                passwordField.type = 'text';
                toggleIcon.classList.remove('fa-eye');
                toggleIcon.classList.add('fa-eye-slash');
            } else {
                passwordField.type = 'password';
                toggleIcon.classList.remove('fa-eye-slash');
                toggleIcon.classList.add('fa-eye');
            }
        }
        
        // Check password strength
        function checkPasswordStrength(password) {
            const strengthFill = document.getElementById('strengthFill');
            const strengthText = document.getElementById('strengthText');
            
            let score = 0;
            let feedback = '';
            
            if (password.length >= 8) score++;
            if (password.match(/[a-z]/)) score++;
            if (password.match(/[A-Z]/)) score++;
            if (password.match(/[0-9]/)) score++;
            if (password.match(/[^A-Za-z0-9]/)) score++;
            
            switch (score) {
                case 0:
                case 1:
                case 2:
                    strengthFill.className = 'strength-fill strength-weak';
                    strengthFill.style.width = '33%';
                    feedback = 'Mật khẩu yếu';
                    break;
                case 3:
                case 4:
                    strengthFill.className = 'strength-fill strength-medium';
                    strengthFill.style.width = '66%';
                    feedback = 'Mật khẩu trung bình';
                    break;
                case 5:
                    strengthFill.className = 'strength-fill strength-strong';
                    strengthFill.style.width = '100%';
                    feedback = 'Mật khẩu mạnh';
                    break;
            }
            
            strengthText.textContent = feedback;
            checkFormValid();
        }
        
        // Check password match
        function checkPasswordMatch() {
            const password = document.getElementById('password').value;
            const confirmPassword = document.getElementById('confirmPassword').value;
            const messageDiv = document.getElementById('passwordMatchMessage');
            
            if (confirmPassword === '') {
                messageDiv.innerHTML = '';
                checkFormValid();
                return;
            }
            
            if (password === confirmPassword) {
                messageDiv.innerHTML = '<small class="text-success"><i class="fas fa-check me-1"></i>Mật khẩu khớp</small>';
            } else {
                messageDiv.innerHTML = '<small class="text-danger"><i class="fas fa-times me-1"></i>Mật khẩu không khớp</small>';
            }
            
            checkFormValid();
        }
        
        // Check if form is valid
        function checkFormValid() {
            const password = document.getElementById('password').value;
            const confirmPassword = document.getElementById('confirmPassword').value;
            const agreeTerms = document.getElementById('agreeTerms').checked;
            const registerBtn = document.getElementById('registerBtn');
            
            const isValid = password.length >= 8 && 
                           password === confirmPassword && 
                           agreeTerms;
            
            registerBtn.disabled = !isValid;
        }
        
        // Add event listeners
        document.getElementById('agreeTerms').addEventListener('change', checkFormValid);
        
        // Add floating label animation
        document.querySelectorAll('.form-floating .form-control').forEach(input => {
            input.addEventListener('focus', function() {
                this.parentElement.classList.add('focused');
            });
            
            input.addEventListener('blur', function() {
                if (this.value === '') {
                    this.parentElement.classList.remove('focused');
                }
            });
        });
        
        // Add form submission loading effect
        document.querySelector('form').addEventListener('submit', function(e) {
            const password = document.getElementById('password').value;
            const confirmPassword = document.getElementById('confirmPassword').value;
            
            if (password !== confirmPassword) {
                e.preventDefault();
                alert('Mật khẩu xác nhận không khớp!');
                return;
            }
            
            const submitBtn = this.querySelector('.btn-register');
            const originalText = submitBtn.innerHTML;
            
            submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>Đang tạo tài khoản...';
            submitBtn.disabled = true;
            
            // Re-enable after 5 seconds (fallback)
            setTimeout(() => {
                submitBtn.innerHTML = originalText;
                submitBtn.disabled = false;
            }, 5000);
        });
    