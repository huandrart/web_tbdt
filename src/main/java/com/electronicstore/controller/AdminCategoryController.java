package com.electronicstore.controller;

import com.electronicstore.dto.request.CategoryRequest;
import com.electronicstore.dto.response.CategoryResponse;
import com.electronicstore.service.CategoryBusinessService;
import com.electronicstore.viewmodel.CategoryFormViewModel;
import com.electronicstore.viewmodel.CategoryListViewModel;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller quản lý Category theo mô hình MVC rõ ràng
 * 
 * Responsibilities:
 * - Nhận HTTP requests
 * - Validate input
 * - Gọi Business Service
 * - Trả về View với ViewModel
 */
@Controller
@RequestMapping("/admin/categories")
public class AdminCategoryController {
    
    @Autowired
    private CategoryBusinessService categoryBusinessService;
    
    /**
     * [VIEW] Hiển thị danh sách categories
     */
    @GetMapping
    public String listCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            Model model) {
        
        try {
            // Gọi Business Service để lấy dữ liệu
            CategoryListViewModel viewModel = categoryBusinessService.getCategoryList(
                page, size, sort, direction, search, status);
            
            // Thêm ViewModel vào Model để hiển thị ở View
            model.addAttribute("viewModel", viewModel);
            
            return "admin/categories/list";
            
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra khi tải danh sách danh mục: " + e.getMessage());
            return "admin/categories/list";
        }
    }
    
    /**
     * [VIEW] Hiển thị form tạo mới category
     */
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        try {
            // Tạo ViewModel cho form tạo mới
            CategoryFormViewModel viewModel = categoryBusinessService.createNewCategoryForm();
            
            model.addAttribute("viewModel", viewModel);
            model.addAttribute("categoryRequest", viewModel.getCategoryRequest()); // Cho Thymeleaf binding
            
            return "admin/categories/form";
            
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/admin/categories";
        }
    }
    
    /**
     * [VIEW] Hiển thị form chỉnh sửa category
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            // Tạo ViewModel cho form chỉnh sửa
            CategoryFormViewModel viewModel = categoryBusinessService.createEditCategoryForm(id);
            
            model.addAttribute("viewModel", viewModel);
            model.addAttribute("categoryRequest", viewModel.getCategoryRequest()); // Cho Thymeleaf binding
            
            return "admin/categories/form";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/categories";
        }
    }
    
    /**
     * [CONTROLLER] Xử lý lưu category (tạo mới hoặc cập nhật)
     */
    @PostMapping("/save")
    public String saveCategory(
            @Valid @ModelAttribute("categoryRequest") CategoryRequest categoryRequest,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        // Validation failed
        if (bindingResult.hasErrors()) {
            try {
                // Tạo lại ViewModel với dữ liệu lỗi
                CategoryFormViewModel viewModel = categoryRequest.getId() != null 
                    ? new CategoryFormViewModel(categoryRequest, true)
                    : new CategoryFormViewModel(categoryRequest, false);
                
                // Lấy lại parent categories nếu cần
                if (categoryRequest.getId() == null) {
                    viewModel = categoryBusinessService.createNewCategoryForm();
                    viewModel.setCategoryRequest(categoryRequest);
                } else {
                    viewModel = categoryBusinessService.createEditCategoryForm(categoryRequest.getId());
                    viewModel.setCategoryRequest(categoryRequest);
                }
                
                model.addAttribute("viewModel", viewModel);
                return "admin/categories/form";
                
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
                return "redirect:/admin/categories";
            }
        }
        
        try {
            // Gọi Business Service để lưu
            CategoryResponse savedCategory = categoryBusinessService.saveCategory(categoryRequest);
            
            String successMessage = categoryRequest.getId() != null 
                ? "Cập nhật danh mục thành công!"
                : "Thêm danh mục mới thành công!";
                
            redirectAttributes.addFlashAttribute("success", successMessage);
            return "redirect:/admin/categories";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            
            // Quay lại form với dữ liệu đã nhập
            String returnUrl = categoryRequest.getId() != null 
                ? "redirect:/admin/categories/edit/" + categoryRequest.getId()
                : "redirect:/admin/categories/new";
                
            return returnUrl;
        }
    }
    
    /**
     * [CONTROLLER] Xóa category
     */
    @GetMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryBusinessService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("success", "Danh mục đã được xóa thành công!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/admin/categories";
    }
    
    /**
     * [VIEW] Hiển thị chi tiết category
     */
    @GetMapping("/view/{id}")
    public String viewCategory(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            CategoryResponse category = categoryBusinessService.getCategoryDetail(id);
            model.addAttribute("category", category);
            
            return "admin/categories/view";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/categories";
        }
    }
    
    /**
     * [API] Toggle trạng thái active/inactive của category (AJAX)
     */
    @PostMapping("/toggle-status/{id}")
    @ResponseBody
    public String toggleStatus(@PathVariable Long id) {
        try {
            boolean success = categoryBusinessService.toggleCategoryStatus(id);
            return success ? "success" : "error";
            
        } catch (Exception e) {
            return "error";
        }
    }
    
    /**
     * [API] Kiểm tra xem category có thể xóa không (AJAX)
     */
    @GetMapping("/can-delete/{id}")
    @ResponseBody
    public String canDelete(@PathVariable Long id) {
        try {
            boolean canDelete = categoryBusinessService.canDeleteCategory(id);
            return canDelete ? "true" : "false";
            
        } catch (Exception e) {
            return "false";
        }
    }
}