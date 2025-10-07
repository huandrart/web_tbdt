/**
 * admin/layout.html JavaScript
 */


        // Mobile sidebar toggle
        function toggleSidebar() {
            document.querySelector('.sidebar').classList.toggle('show');
        }
        
        // Auto-hide sidebar on mobile when clicking outside
        document.addEventListener('click', function(e) {
            if (window.innerWidth <= 768) {
                const sidebar = document.querySelector('.sidebar');
                const toggleBtn = document.querySelector('.sidebar-toggle');
                
                if (!sidebar.contains(e.target) && !toggleBtn?.contains(e.target)) {
                    sidebar.classList.remove('show');
                }
            }
        });
        
        // Handle window resize
        window.addEventListener('resize', function() {
            if (window.innerWidth > 768) {
                document.querySelector('.sidebar').classList.remove('show');
            }
        });
        
        // Auto-submit form on filter change
        document.addEventListener('DOMContentLoaded', function() {
            const filterSelects = document.querySelectorAll('select[name="category"], select[name="status"], select[name="sort"], select[name="direction"]');
            filterSelects.forEach(select => {
                select.addEventListener('change', function() {
                    this.form.submit();
                });
            });
        });
    