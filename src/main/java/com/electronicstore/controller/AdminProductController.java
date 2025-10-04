package com.electronicstore.controller;

import com.electronicstore.entity.Product;
import com.electronicstore.service.ProductService;
import com.electronicstore.service.CategoryService;
import com.electronicstore.viewmodel.ProductFormViewModel;
import com.electronicstore.viewmodel.ProductListViewModel;
import com.electronicstore.service.business.ProductBusinessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/products")
public class AdminProductController {

    @Autowired
    private ProductBusinessService productBusinessService;
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public String listProducts(
            @RequestParam(value = "search",    required = false, defaultValue = "") String search,
            @RequestParam(value = "sort",      required = false, defaultValue = "id") String sort,
            @RequestParam(value = "direction", required = false, defaultValue = "asc") String direction,
            @RequestParam(value = "page",      required = false, defaultValue = "0") int page,
            @RequestParam(value = "size",      required = false, defaultValue = "10") int size,
            @RequestParam(value = "category",  required = false) String category,
            @RequestParam(value = "status",    required = false) String status,
            Model model
    ) {
        try {
            // Convert category to Long if needed
            Long categoryId = null;
            if (category != null && !category.isEmpty() && !category.equals("all")) {
                try {
                    categoryId = Long.parseLong(category);
                } catch (NumberFormatException e) {
                    // Invalid category format, ignore
                }
            }

            // Gọi Business Service theo đúng tham số mà list.html truyền/nhận
            ProductListViewModel vm = productBusinessService.getProductList(
                    search, sort, direction, page, size, status, categoryId
            );

            // Trang đang expect 1 Page<?> tên "products"
            Page<?> productsPage = vm.getProducts();

            // Các biến trang đang dùng để hiển thị / phân trang / sort
            model.addAttribute("products", productsPage);
            model.addAttribute("categories", vm.getCategories()); // combo danh mục
            model.addAttribute("search", search);
            model.addAttribute("category", category);
            model.addAttribute("status", status);

            model.addAttribute("size", size);
            model.addAttribute("sort", sort);
            model.addAttribute("direction", direction);
            model.addAttribute("reversedDirection",
                    "asc".equalsIgnoreCase(direction) ? "desc" : "asc");

            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", productsPage.getTotalPages());
            model.addAttribute("totalItems", productsPage.getTotalElements());

            return "admin/products/list";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "admin/products/list";
        }
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        try {
            ProductFormViewModel viewModel = productBusinessService.createNewProductForm();
            model.addAttribute("viewModel", viewModel);
            return "admin/products/form";
            
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/admin/products";
        }
    }
    
    @GetMapping("/view/{id}")
    public String viewProduct(@PathVariable Long id, Model model) {
        try {
            Product product = productService.findById(id);
            if (product == null) {
                return "redirect:/admin/products?error=Product not found";
            }
            
            model.addAttribute("product", product);
            return "admin/products/view";
            
        } catch (Exception e) {
            return "redirect:/admin/products?error=" + e.getMessage();
        }
    }
    
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
            Product product = productService.findById(id);
            if (product == null) {
                return "redirect:/admin/products?error=Product not found";
            }
            
            ProductFormViewModel viewModel = productBusinessService.createEditProductForm(id);
            model.addAttribute("viewModel", viewModel);
            model.addAttribute("product", product);
            return "admin/products/form";
            
        } catch (Exception e) {
            return "redirect:/admin/products?error=" + e.getMessage();
        }
    }
    
    @PostMapping("/save")
    public String saveProduct(@ModelAttribute Product product, 
                             @RequestParam(required = false) Long categoryId,
                             RedirectAttributes redirectAttributes) {
        try {
            if (categoryId != null) {
                var category = categoryService.findById(categoryId);
                product.setCategory(category);
            }
            
            productService.save(product);
            redirectAttributes.addFlashAttribute("success", "Sản phẩm đã được lưu thành công!");
            return "redirect:/admin/products";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi lưu sản phẩm: " + e.getMessage());
            return "redirect:/admin/products/new";
        }
    }
    
    @PostMapping("/update/{id}")
    public String updateProduct(@PathVariable Long id, 
                               @ModelAttribute Product product,
                               @RequestParam(required = false) Long categoryId,
                               RedirectAttributes redirectAttributes) {
        try {
            Product existingProduct = productService.findById(id);
            if (existingProduct == null) {
                redirectAttributes.addFlashAttribute("error", "Sản phẩm không tồn tại!");
                return "redirect:/admin/products";
            }
            
            // Update fields
            existingProduct.setName(product.getName());
            existingProduct.setDescription(product.getDescription());
            existingProduct.setPrice(product.getPrice());
            existingProduct.setSalePrice(product.getSalePrice());
            existingProduct.setStockQuantity(product.getStockQuantity());
            existingProduct.setSku(product.getSku());
            existingProduct.setBrand(product.getBrand());
            existingProduct.setModel(product.getModel());
            existingProduct.setWarrantyPeriod(product.getWarrantyPeriod());
            existingProduct.setIsFeatured(product.getIsFeatured());
            existingProduct.setIsActive(product.getIsActive());
            
            if (categoryId != null) {
                var category = categoryService.findById(categoryId);
                existingProduct.setCategory(category);
            }
            
            productService.save(existingProduct);
            redirectAttributes.addFlashAttribute("success", "Sản phẩm đã được cập nhật thành công!");
            return "redirect:/admin/products";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật sản phẩm: " + e.getMessage());
            return "redirect:/admin/products/edit/" + id;
        }
    }
    
    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Product product = productService.findById(id);
            if (product == null) {
                redirectAttributes.addFlashAttribute("error", "Sản phẩm không tồn tại!");
                return "redirect:/admin/products";
            }
            
            productService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Sản phẩm đã được xóa thành công!");
            return "redirect:/admin/products";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa sản phẩm: " + e.getMessage());
            return "redirect:/admin/products";
        }
    }
    
    @PostMapping("/toggle-status/{id}")
    public String toggleProductStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Product product = productService.findById(id);
            if (product == null) {
                redirectAttributes.addFlashAttribute("error", "Sản phẩm không tồn tại!");
                return "redirect:/admin/products";
            }
            
            product.setIsActive(!product.getIsActive());
            productService.save(product);
            
            String status = product.getIsActive() ? "kích hoạt" : "tạm dừng";
            redirectAttributes.addFlashAttribute("success", "Sản phẩm đã được " + status + " thành công!");
            return "redirect:/admin/products";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi thay đổi trạng thái sản phẩm: " + e.getMessage());
            return "redirect:/admin/products";
        }
    }
}