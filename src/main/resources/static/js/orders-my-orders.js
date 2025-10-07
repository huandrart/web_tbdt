/**
 * orders/my-orders.html JavaScript
 */

$(document).ready(function() {
    // Auto-submit form when status or size changes
    $('#status, #size').on('change', function() {
        $(this).closest('form').submit();
    });
    
    // Confirm cancel order
    $('form[action*="/cancel"]').on('submit', function(e) {
        if (!confirm('Bạn có chắc muốn hủy đơn hàng này?')) {
            e.preventDefault();
        }
    });
});
