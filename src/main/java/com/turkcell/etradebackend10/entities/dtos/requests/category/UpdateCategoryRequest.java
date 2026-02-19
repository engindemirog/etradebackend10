package com.turkcell.etradebackend10.entities.dtos.requests.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UpdateCategoryRequest {

    @NotNull(message = "Kategori ID boş olamaz.")
    private int id;

    @NotBlank(message = "Kategori adı boş olamaz.")
    @Size(min = 2, max = 100, message = "Kategori adı 2 ile 100 karakter arasında olmalıdır.")
    private String name;

    @Size(max = 500, message = "Açıklama en fazla 500 karakter olabilir.")
    private String description;

    public UpdateCategoryRequest() {
    }

    public UpdateCategoryRequest(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
}
