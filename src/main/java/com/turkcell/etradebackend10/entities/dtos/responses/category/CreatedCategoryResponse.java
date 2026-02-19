package com.turkcell.etradebackend10.entities.dtos.responses.category;

import java.time.LocalDateTime;

public class CreatedCategoryResponse {

    private int id;
    private String name;
    private String description;
    private LocalDateTime createdDate;

    public CreatedCategoryResponse() {
    }

    public CreatedCategoryResponse(int id, String name, String description, LocalDateTime createdDate) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdDate = createdDate;
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

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
}
