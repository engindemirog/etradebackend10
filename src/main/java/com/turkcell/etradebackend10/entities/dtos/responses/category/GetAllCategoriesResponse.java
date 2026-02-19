package com.turkcell.etradebackend10.entities.dtos.responses.category;

public class GetAllCategoriesResponse {

    private int id;
    private String name;

    public GetAllCategoriesResponse() {
    }

    public GetAllCategoriesResponse(int id, String name) {
        this.id = id;
        this.name = name;
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
}
