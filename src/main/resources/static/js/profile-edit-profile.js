/**
 * profile/edit-profile.html JavaScript
 */


        // Auto sync full name with first name and last name
        document.getElementById('firstName').addEventListener('input', syncNames);
        document.getElementById('lastName').addEventListener('input', syncNames);
        
        function syncNames() {
            const firstName = document.getElementById('firstName').value;
            const lastName = document.getElementById('lastName').value;
            const fullNameField = document.getElementById('fullName');
            
            if (firstName || lastName) {
                fullNameField.value = `${lastName} ${firstName}`.trim();
            }
        }
    