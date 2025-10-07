/**
 * profile/change-password.html JavaScript
 */


        // Toggle password visibility
        function togglePassword(fieldId, iconId) {
            const passwordField = document.getElementById(fieldId);
            const toggleIcon = document.getElementById(iconId);
            
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
            const newPassword = document.getElementById('newPassword').value;
            const confirmPassword = document.getElementById('confirmPassword').value;
            const messageDiv = document.getElementById('passwordMatchMessage');
            
            if (confirmPassword === '') {
                messageDiv.innerHTML = '';
                checkFormValid();
                return;
            }
            
            if (newPassword === confirmPassword) {
                messageDiv.innerHTML = '<small class="text-success"><i class="fas fa-check me-1"></i>Mật khẩu khớp</small>';
            } else {
                messageDiv.innerHTML = '<small class="text-danger"><i class="fas fa-times me-1"></i>Mật khẩu không khớp</small>';
            }
            
            checkFormValid();
        }
        
        // Check if form is valid
        function checkFormValid() {
            const currentPassword = document.getElementById('currentPassword').value;
            const newPassword = document.getElementById('newPassword').value;
            const confirmPassword = document.getElementById('confirmPassword').value;
            const changeBtn = document.getElementById('changePasswordBtn');
            
            const isValid = currentPassword.length > 0 && 
                           newPassword.length >= 8 && 
                           newPassword === confirmPassword;
            
            changeBtn.disabled = !isValid;
        }
        
        // Add event listeners
        document.getElementById('currentPassword').addEventListener('keyup', checkFormValid);
    