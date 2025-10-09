/**
 * Admin Users View JavaScript
 * Handles user view page interactions
 */

$(document).ready(function() {
    // Password confirmation validation
    $('#newPassword, #confirmPassword').on('input', function() {
        const newPassword = $('#newPassword').val();
        const confirmPassword = $('#confirmPassword').val();
        const confirmBtn = $('#confirmResetBtn');
        
        if (newPassword.length >= 6 && confirmPassword.length >= 6) {
            if (newPassword === confirmPassword) {
                confirmBtn.prop('disabled', false);
                $('#confirmPassword').removeClass('is-invalid').addClass('is-valid');
            } else {
                confirmBtn.prop('disabled', true);
                $('#confirmPassword').removeClass('is-valid').addClass('is-invalid');
            }
        } else {
            confirmBtn.prop('disabled', true);
            $('#confirmPassword').removeClass('is-valid is-invalid');
        }
    });
    
    // Reset form when modal is hidden
    $('#resetPasswordModal').on('hidden.bs.modal', function() {
        $('#newPassword, #confirmPassword').val('').removeClass('is-valid is-invalid');
        $('#confirmResetBtn').prop('disabled', true);
    });
    
    // Form submission confirmation
    $('#resetPasswordModal form').on('submit', function(e) {
        const newPassword = $('#newPassword').val();
        const confirmPassword = $('#confirmPassword').val();
        
        if (newPassword !== confirmPassword) {
            e.preventDefault();
            alert('Mật khẩu xác nhận không khớp!');
            return false;
        }
        
        if (newPassword.length < 6) {
            e.preventDefault();
            alert('Mật khẩu phải có ít nhất 6 ký tự!');
            return false;
        }
        
        return confirm('Bạn có chắc chắn muốn đặt lại mật khẩu cho người dùng này?');
    });
});
