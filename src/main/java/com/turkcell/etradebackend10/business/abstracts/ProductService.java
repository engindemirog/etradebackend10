package com.turkcell.etradebackend10.business.abstracts;

import com.turkcell.etradebackend10.entities.dtos.requests.product.CreateProductRequest;
import com.turkcell.etradebackend10.entities.dtos.requests.product.UpdateProductRequest;
import com.turkcell.etradebackend10.entities.dtos.responses.product.*;

import java.util.List;

public interface ProductService {

    List<GetAllProductsResponse> getAll();

    GetByIdProductResponse getById(int id);

    CreatedProductResponse add(CreateProductRequest request);

    UpdatedProductResponse update(UpdateProductRequest request);

    DeletedProductResponse delete(int id);
}
