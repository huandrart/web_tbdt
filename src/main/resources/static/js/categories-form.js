/**
 * categories/form.html JavaScript
 */


  // Preview selected image
  document.getElementById('imageFile').addEventListener('change', function(e) {
    const file = e.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = function(e) {
        // Remove existing preview if any
        const existingPreview = document.getElementById('imagePreview');
        if (existingPreview) {
          existingPreview.remove();
        }
        
        // Create preview
        const preview = document.createElement('div');
        preview.id = 'imagePreview';
        preview.className = 'mt-2';
        preview.innerHTML = `
          <label class="form-label">Xem trước:</label>
          <div>
            <img src="${e.target.result}" 
                 alt="Preview" 
                 style="max-width: 200px; max-height: 200px;" 
                 class="img-thumbnail">
          </div>
        `;
        
        // Insert preview after file input
        e.target.parentNode.insertBefore(preview, e.target.nextSibling);
      };
      reader.readAsDataURL(file);
    }
  });
