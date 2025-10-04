package com.electronicstore.viewmodel;

import com.electronicstore.dto.response.ProductResponse;
import com.electronicstore.entity.Category;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * ViewModel cho trang danh sách products
 */
public class ProductListViewModel {
    
    private Page<ProductResponse> products;
    private String currentSort;
    private String currentDirection;
    private String reversedDirection;
    private String currentSearch;
    private String currentStatus;
    private Long currentCategoryId;
    private int currentPage;
    private int totalPages;
    private long totalItems;
    private int pageSize;
    
    // Pagination info
    private List<Integer> pageNumbers;
    private boolean hasNext;
    private boolean hasPrevious;
    
    // Filter options
    private List<String> availableBrands;
    private List<String> stockStatuses;
    private List<Category> categories;
    
    // Constructors
    public ProductListViewModel() {}
    
    public ProductListViewModel(Page<ProductResponse> products) {
        this.products = products;
        this.currentPage = products.getNumber();
        this.totalPages = products.getTotalPages();
        this.totalItems = products.getTotalElements();
        this.pageSize = products.getSize();
        this.hasNext = products.hasNext();
        this.hasPrevious = products.hasPrevious();
    }
    
    // Getters and Setters
    public Page<ProductResponse> getProducts() {
        return products;
    }
    
    public void setProducts(Page<ProductResponse> products) {
        this.products = products;
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
    
    public Long getCurrentCategoryId() {
        return currentCategoryId;
    }
    
    public void setCurrentCategoryId(Long currentCategoryId) {
        this.currentCategoryId = currentCategoryId;
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
    
    public List<String> getAvailableBrands() {
        return availableBrands;
    }
    
    public void setAvailableBrands(List<String> availableBrands) {
        this.availableBrands = availableBrands;
    }
    
    public List<String> getStockStatuses() {
        return stockStatuses;
    }
    
    public void setStockStatuses(List<String> stockStatuses) {
        this.stockStatuses = stockStatuses;
    }
    
    public List<Category> getCategories() {
        return categories;
    }
    
    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }
}