/**
 * auth/forgot-password.html JavaScript
 */


        let currentStep = 1;
        let countdownTimer;
        
        // Email form submission
        document.getElementById('emailForm').addEventListener('submit', function(e) {
            e.preventDefault();
            const email = document.getElementById('email').value;
            
            // Simulate API call
            setTimeout(() => {
                document.getElementById('emailDisplay').textContent = email;
                showStep(2);
                startCountdown();
            }, 1000);
        });
        
        // Verification form submission
        document.getElementById('verificationForm').addEventListener('submit', function(e) {
            e.preventDefault();
            
            // Get verification code
            const codes = document.querySelectorAll('#step2 input[type="text"]');
            const verificationCode = Array.from(codes).map(input => input.value).join('');
            
            if (verificationCode.length !== 6) {
                alert('Vui lòng nhập đầy đủ mã xác minh');
                return;
            }
            
            // Simulate verification
            setTimeout(() => {
                showStep(3);
            }, 1000);
        });
        
        // Reset password form submission
        document.getElementById('resetPasswordForm').addEventListener('submit', function(e) {
            e.preventDefault();
            
            const newPassword = document.getElementById('newPassword').value;
            const confirmPassword = document.getElementById('confirmNewPassword').value;
            
            if (newPassword !== confirmPassword) {
                document.getElementById('confirmNewPassword').classList.add('is-invalid');
                document.getElementById('confirmPasswordError').textContent = 'Mật khẩu xác nhận không khớp';
                return;
            }
            
            // Simulate password reset
            setTimeout(() => {
                showStep('success');
            }, 1000);
        });
        
        // Show step function
        function showStep(step) {
            document.querySelectorAll('.step').forEach(s => s.classList.remove('active'));
            document.querySelectorAll('.step-item').forEach(s => s.classList.remove('active'));
            
            if (step === 'success') {
                document.getElementById('success').classList.add('active');
            } else {
                document.getElementById(`step${step}`).classList.add('active');
                document.getElementById(`step${step}-indicator`).classList.add('active');
            }
            
            currentStep = step;
        }
        
        // Verification code input handling
        document.querySelectorAll('#step2 input[type="text"]').forEach((input, index, inputs) => {
            input.addEventListener('input', function() {
                if (this.value.length === 1 && index < inputs.length - 1) {
                    inputs[index + 1].focus();
                }
            });
            
            input.addEventListener('keydown', function(e) {
                if (e.key === 'Backspace' && this.value === '' && index > 0) {
                    inputs[index - 1].focus();
                }
            });
        });
        
        // Countdown timer
        function startCountdown() {
            let seconds = 60;
            const countdownElement = document.getElementById('countdown');
            const resendButton = document.getElementById('resendCode');
            
            resendButton.disabled = true;
            
            countdownTimer = setInterval(() => {
                seconds--;
                countdownElement.textContent = seconds;
                
                if (seconds <= 0) {
                    clearInterval(countdownTimer);
                    resendButton.disabled = false;
                }
            }, 1000);
        }
        
        // Resend code
        document.getElementById('resendCode').addEventListener('click', function() {
            startCountdown();
        });
        
        // Password visibility toggle
        document.getElementById('toggleNewPassword').addEventListener('click', function() {
            const password = document.getElementById('newPassword');
            const type = password.getAttribute('type') === 'password' ? 'text' : 'password';
            password.setAttribute('type', type);
            this.querySelector('i').classList.toggle('fa-eye');
            this.querySelector('i').classList.toggle('fa-eye-slash');
        });
        
        // Password strength indicator
        function checkPasswordStrength(password) {
            let strength = 0;
            if (password.length >= 6) strength += 1;
            if (password.length >= 8) strength += 1;
            if (/[A-Z]/.test(password)) strength += 1;
            if (/[0-9]/.test(password)) strength += 1;
            if (/[^A-Za-z0-9]/.test(password)) strength += 1;
            return strength;
        }
        
        document.getElementById('newPassword').addEventListener('input', function() {
            const password = this.value;
            const strength = checkPasswordStrength(password);
            const strengthBar = document.getElementById('strengthBar');
            const strengthText = document.getElementById('strengthText');
            
            const colors = ['#dc3545', '#fd7e14', '#ffc107', '#28a745', '#20c997'];
            const texts = ['Yếu', 'Trung bình', 'Khá', 'Mạnh', 'Rất mạnh'];
            
            if (password.length === 0) {
                strengthBar.style.width = '0%';
                strengthBar.style.backgroundColor = '#e9ecef';
                strengthText.textContent = 'Mật khẩu ít nhất 6 ký tự';
            } else {
                strengthBar.style.width = ((strength + 1) * 20) + '%';
                strengthBar.style.backgroundColor = colors[strength];
                strengthText.textContent = texts[strength] || 'Yếu';
            }
        });
        
        // Confirm password validation
        document.getElementById('confirmNewPassword').addEventListener('input', function() {
            const password = document.getElementById('newPassword').value;
            const confirmPassword = this.value;
            const errorDiv = document.getElementById('confirmPasswordError');
            
            if (confirmPassword && password !== confirmPassword) {
                this.classList.add('is-invalid');
                errorDiv.textContent = 'Mật khẩu xác nhận không khớp';
            } else {
                this.classList.remove('is-invalid');
                errorDiv.textContent = '';
            }
        });
    