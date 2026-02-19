package com.turkcell.etradebackend10.entities.dtos.requests.product;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateProductRequest {

    @NotBlank(message = "Ürün adı boş olamaz.")
    @Size(min = 2, max = 100, message = "Ürün adı 2 ile 100 karakter arasında olmalıdır.")
    private String name;

    @Size(max = 500, message = "Açıklama en fazla 500 karakter olabilir.")
    private String description;

    @NotNull(message = "Birim fiyat boş olamaz.")
    @Min(value = 0, message = "Birim fiyat 0'dan küçük olamaz.")
    private double unitPrice;

    @Min(value = 0, message = "Stok adedi 0'dan küçük olamaz.")
    private int unitsInStock;

    private String imageUrl;

    @NotNull(message = "Kategori ID boş olamaz.")
    private int categoryId;

    public CreateProductRequest() {
    }

    public CreateProductRequest(String name, String description, double unitPrice, int unitsInStock, String imageUrl, int categoryId) {
        this.name = name;
        this.description = description;
        this.unitPrice = unitPrice;
        this.unitsInStock = unitsInStock;
        this.imageUrl = imageUrl;
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public int getUnitsInStock() {
        return unitsInStock;
    }

    public void setUnitsInStock(int unitsInStock) {
        this.unitsInStock = unitsInStock;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }
}
