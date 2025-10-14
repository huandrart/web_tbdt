package com.electronicstore.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class CheckoutRequest {
    
    @NotBlank(message = "Họ và tên không được để trống")
    @Size(max = 100, message = "Họ và tên không được vượt quá 100 ký tự")
    private String customerName;
    
    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại phải có 10-11 chữ số")
    private String phone;
    
    @NotBlank(message = "Địa chỉ giao hàng không được để trống")
    @Size(max = 500, message = "Địa chỉ không được vượt quá 500 ký tự")
    private String address;
    
    @Size(max = 1000, message = "Ghi chú không được vượt quá 1000 ký tự")
    private String notes;
    
    @NotNull(message = "Phương thức thanh toán không được để trống")
    private String paymentMethod;
    
    // Constructors
    public CheckoutRequest() {}
    
    public CheckoutRequest(String customerName, String phone, String address, String notes, String paymentMethod) {
        this.customerName = customerName;
        this.phone = phone;
        this.address = address;
        this.notes = notes;
        this.paymentMethod = paymentMethod;
    }
    
    // Getters and Setters
    public String getCustomerName() {
        return customerName;
    }
    
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    @Override
    public String toString() {
        return "CheckoutRequest{" +
                "customerName='" + customerName + '\'' +
                ", phone='" + phone + '\'' +
                ", address='" + address + '\'' +
                ", notes='" + notes + '\'' +
                ", paymentMethod='" + paymentMethod + '\'' +
                '}';
    }
}
