/**
 * Admin Products List JavaScript
 * Handles product list page interactions
 */

$(document).ready(function() {
    // Page size change
    $('#pageSize').change(function() {
        var size = $(this).val();
        var currentUrl = new URL(window.location);
        currentUrl.searchParams.set('size', size);
        currentUrl.searchParams.set('page', 0);
        window.location = currentUrl;
    });
    
    // Toggle status
    $('.toggle-status').click(function() {
        var button = $(this);
        var productId = button.data('id');
        
        $.ajax({
            url: '/admin/products/toggle-status/' + productId,
            type: 'POST',
            success: function(response) {
                if (response === 'success') {
                    location.reload();
                } else {
                    alert('Có lỗi xảy ra khi thay đổi trạng thái!');
                }
            },
            error: function() {
                alert('Có lỗi xảy ra khi thay đổi trạng thái!');
            }
        });
    });
    
    // Delete confirmation
    $('.delete-btn').click(function(e) {
        e.preventDefault();
        if (confirm('Bạn có chắc chắn muốn xóa sản phẩm này?')) {
            window.location = $(this).attr('href');
        }
    });
    
    // Auto-submit search form when filters change
    $('select[name="category"], select[name="status"]').change(function() {
        $(this).closest('form').submit();
    });
});
