package com.turkcell.etradebackend10.entities.dtos.responses.product;

public class DeletedProductResponse {

    private int id;
    private String name;

    public DeletedProductResponse() {
    }

    public DeletedProductResponse(int id, String name) {
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
