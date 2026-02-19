package com.turkcell.etradebackend10.api.controllers;

import com.turkcell.etradebackend10.business.abstracts.ProductService;
import com.turkcell.etradebackend10.entities.dtos.requests.product.CreateProductRequest;
import com.turkcell.etradebackend10.entities.dtos.requests.product.UpdateProductRequest;
import com.turkcell.etradebackend10.entities.dtos.responses.product.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductsController {

    private final ProductService productService;

    public ProductsController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<GetAllProductsResponse> getAll() {
        return productService.getAll();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public GetByIdProductResponse getById(@PathVariable int id) {
        return productService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreatedProductResponse add(@RequestBody @Valid CreateProductRequest request) {
        return productService.add(request);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public UpdatedProductResponse update(@RequestBody @Valid UpdateProductRequest request) {
        return productService.update(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public DeletedProductResponse delete(@PathVariable int id) {
        return productService.delete(id);
    }
}
