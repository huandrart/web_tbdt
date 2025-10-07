/**
 * users/list.html JavaScript
 */


        $(document).ready(function() {
            // Checkbox selection logic
            $('#selectAllCheckbox').change(function() {
                $('.user-checkbox').prop('checked', $(this).prop('checked'));
                updateBulkActionsButton();
            });
            
            $('.user-checkbox').change(function() {
                const totalCheckboxes = $('.user-checkbox').length;
                const checkedCheckboxes = $('.user-checkbox:checked').length;
                
                $('#selectAllCheckbox').prop('checked', checkedCheckboxes === totalCheckboxes);
                $('#selectAllCheckbox').prop('indeterminate', checkedCheckboxes > 0 && checkedCheckboxes < totalCheckboxes);
                
                updateBulkActionsButton();
            });
            
            function updateBulkActionsButton() {
                const checkedCount = $('.user-checkbox:checked').length;
                $('#bulkActionsBtn').prop('disabled', checkedCount === 0);
                
                if (checkedCount > 0) {
                    $('#selectAllBtn').html('<i class="fas fa-times me-1"></i> Bỏ chọn tất cả');
                } else {
                    $('#selectAllBtn').html('<i class="fas fa-check-square me-1"></i> Chọn tất cả');
                }
            }
        });
        
        function selectAll() {
            const isAllSelected = $('#selectAllCheckbox').prop('checked');
            $('#selectAllCheckbox').prop('checked', !isAllSelected);
            $('.user-checkbox').prop('checked', !isAllSelected);
            updateBulkActionsButton();
        }
        
        function bulkUpdateStatus(isActive) {
            const selectedIds = $('.user-checkbox:checked').map(function() {
                return $(this).val();
            }).get();
            
            if (selectedIds.length === 0) {
                alert('Vui lòng chọn ít nhất một người dùng.');
                return;
            }
            
            const action = isActive ? 'kích hoạt' : 'khóa';
            
            if (confirm(`Bạn có chắc muốn ${action} ${selectedIds.length} tài khoản đã chọn?`)) {
                // Create form and submit
                const form = $('<form>', {
                    method: 'POST',
                    action: '/admin/users/bulk-update-status'
                });
                
                form.append($('<input>', {
                    type: 'hidden',
                    name: 'active',
                    value: isActive
                }));
                
                selectedIds.forEach(id => {
                    form.append($('<input>', {
                        type: 'hidden',
                        name: 'userIds',
                        value: id
                    }));
                });
                
                // Add CSRF token if needed
                form.append($('<input>', {
                    type: 'hidden',
                    name: '_token',
                    value: $('meta[name="csrf-token"]').attr('content')
                }));
                
                $('body').append(form);
                form.submit();
            }
        }
        
        function bulkExport() {
            const selectedIds = $('.user-checkbox:checked').map(function() {
                return $(this).val();
            }).get();
            
            if (selectedIds.length === 0) {
                alert('Vui lòng chọn ít nhất một người dùng.');
                return;
            }
            
            const params = selectedIds.map(id => `userIds=${id}`).join('&');
            window.open(`/admin/users/export?${params}`, '_blank');
        }
        
        function exportUsers(format = 'excel') {
            const params = new URLSearchParams(window.location.search);
            params.set('format', format);
            window.open(`/admin/users/export?${params.toString()}`, '_blank');
        }

        function changeUserRole(userId, newRole, fullName) {
            const currentRole = newRole === 'ADMIN' ? 'USER' : 'ADMIN';
            const actionText = newRole === 'ADMIN' ? 'thăng chức thành Admin' : 'hạ cấp về User';
            
            if (confirm(`Bạn có chắc chắn muốn ${actionText} cho ${fullName}?`)) {
                const form = $('<form>', {
                    'method': 'POST',
                    'action': `/admin/users/${userId}/change-role`,
                    'style': 'display: none;'
                });
                
                // Add CSRF token
                const token = $('meta[name="_csrf"]').attr('content');
                const header = $('meta[name="_csrf_header"]').attr('content');
                
                form.append($('<input>', {
                    'type': 'hidden',
                    'name': '_token',
                    'value': token
                }));
                
                form.append($('<input>', {
                    'type': 'hidden',
                    'name': 'role',
                    'value': newRole
                }));
                
                $('body').append(form);
                form.submit();
            }
        }
    