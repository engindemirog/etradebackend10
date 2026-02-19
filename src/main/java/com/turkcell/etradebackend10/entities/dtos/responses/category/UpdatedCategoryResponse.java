package com.turkcell.etradebackend10.entities.dtos.responses.category;

import java.time.LocalDateTime;

public class UpdatedCategoryResponse {

    private int id;
    private String name;
    private String description;
    private LocalDateTime updatedDate;

    public UpdatedCategoryResponse() {
    }

    public UpdatedCategoryResponse(int id, String name, String description, LocalDateTime updatedDate) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.updatedDate = updatedDate;
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

    public LocalDateTime getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(LocalDateTime updatedDate) {
        this.updatedDate = updatedDate;
    }
}
