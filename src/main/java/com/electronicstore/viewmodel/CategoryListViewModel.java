package com.electronicstore.viewmodel;

import com.electronicstore.dto.response.CategoryResponse;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * ViewModel cho trang danh sách categories
 */
public class CategoryListViewModel {
    
    private Page<CategoryResponse> categories;
    private String currentSort;
    private String currentDirection;
    private String reversedDirection;
    private String currentSearch;
    private String currentStatus;
    private int currentPage;
    private int totalPages;
    private long totalItems;
    private int pageSize;
    
    // Pagination info
    private List<Integer> pageNumbers; // Danh sách số trang để hiển thị
    private boolean hasNext;
    private boolean hasPrevious;
    
    // Constructors
    public CategoryListViewModel() {}
    
    public CategoryListViewModel(Page<CategoryResponse> categories) {
        this.categories = categories;
        this.currentPage = categories.getNumber();
        this.totalPages = categories.getTotalPages();
        this.totalItems = categories.getTotalElements();
        this.pageSize = categories.getSize();
        this.hasNext = categories.hasNext();
        this.hasPrevious = categories.hasPrevious();
    }
    
    // Getters and Setters
    public Page<CategoryResponse> getCategories() {
        return categories;
    }
    
    public void setCategories(Page<CategoryResponse> categories) {
        this.categories = categories;
    }
    
    public String getCurrentSort() {
        return currentSort;
    }
    
    public void setCurrentSort(String currentSort) {
        this.currentSort = currentSort;
    }
    
    public String getCurrentDirection() {
        return currentDirection;
    }
    
    public void setCurrentDirection(String currentDirection) {
        this.currentDirection = currentDirection;
        this.reversedDirection = "asc".equals(currentDirection) ? "desc" : "asc";
    }
    
    public String getReversedDirection() {
        return reversedDirection;
    }
    
    public void setReversedDirection(String reversedDirection) {
        this.reversedDirection = reversedDirection;
    }
    
    public String getCurrentSearch() {
        return currentSearch;
    }
    
    public void setCurrentSearch(String currentSearch) {
        this.currentSearch = currentSearch;
    }
    
    public String getCurrentStatus() {
        return currentStatus;
    }
    
    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }
    
    public int getCurrentPage() {
        return currentPage;
    }
    
    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }
    
    public int getTotalPages() {
        return totalPages;
    }
    
    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
    
    public long getTotalItems() {
        return totalItems;
    }
    
    public void setTotalItems(long totalItems) {
        this.totalItems = totalItems;
    }
    
    public int getPageSize() {
        return pageSize;
    }
    
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
    
    public List<Integer> getPageNumbers() {
        return pageNumbers;
    }
    
    public void setPageNumbers(List<Integer> pageNumbers) {
        this.pageNumbers = pageNumbers;
    }
    
    public boolean isHasNext() {
        return hasNext;
    }
    
    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }
    
    public boolean isHasPrevious() {
        return hasPrevious;
    }
    
    public void setHasPrevious(boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }
}