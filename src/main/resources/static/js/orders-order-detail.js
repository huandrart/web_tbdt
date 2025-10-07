/**
 * orders/order-detail.html JavaScript
 */

$(document).ready(function() {
    // Confirm cancel order
    $('form[action*="/cancel"]').on('submit', function(e) {
        if (!confirm('Bạn có chắc muốn hủy đơn hàng này?')) {
            e.preventDefault();
        }
    });
});
