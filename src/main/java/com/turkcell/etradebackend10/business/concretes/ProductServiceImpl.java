package com.turkcell.etradebackend10.business.concretes;

import com.turkcell.etradebackend10.business.abstracts.ProductService;
import com.turkcell.etradebackend10.business.rules.ProductBusinessRules;
import com.turkcell.etradebackend10.dataAccess.abstracts.CategoryRepository;
import com.turkcell.etradebackend10.dataAccess.abstracts.ProductRepository;
import com.turkcell.etradebackend10.entities.concretes.Category;
import com.turkcell.etradebackend10.entities.concretes.Product;
import com.turkcell.etradebackend10.entities.dtos.requests.product.CreateProductRequest;
import com.turkcell.etradebackend10.entities.dtos.requests.product.UpdateProductRequest;
import com.turkcell.etradebackend10.entities.dtos.responses.product.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductBusinessRules productBusinessRules;

    public ProductServiceImpl(ProductRepository productRepository, CategoryRepository categoryRepository, ProductBusinessRules productBusinessRules) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productBusinessRules = productBusinessRules;
    }

    @Override
    public List<GetAllProductsResponse> getAll() {
        List<Product> products = productRepository.findAll();

        return products.stream().map(product -> {
            GetAllProductsResponse response = new GetAllProductsResponse();
            response.setId(product.getId());
            response.setName(product.getName());
            response.setUnitPrice(product.getUnitPrice());
            response.setUnitsInStock(product.getUnitsInStock());
            response.setImageUrl(product.getImageUrl());
            if (product.getCategory() != null) {
                response.setCategoryId(product.getCategory().getId());
                response.setCategoryName(product.getCategory().getName());
            }
            return response;
        }).toList();
    }

    @Override
    public GetByIdProductResponse getById(int id) {
        productBusinessRules.checkIfProductExistsById(id);

        Product product = productRepository.findById(id).orElseThrow();

        GetByIdProductResponse response = new GetByIdProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setUnitPrice(product.getUnitPrice());
        response.setUnitsInStock(product.getUnitsInStock());
        response.setImageUrl(product.getImageUrl());
        if (product.getCategory() != null) {
            response.setCategoryId(product.getCategory().getId());
            response.setCategoryName(product.getCategory().getName());
        }
        response.setCreatedDate(product.getCreatedDate());
        response.setUpdatedDate(product.getUpdatedDate());
        response.setActive(product.isActive());
        return response;
    }

    @Override
    public CreatedProductResponse add(CreateProductRequest request) {
        productBusinessRules.checkIfProductNameAlreadyExists(request.getName());
        productBusinessRules.checkIfUnitPriceValid(request.getUnitPrice());

        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setUnitPrice(request.getUnitPrice());
        product.setUnitsInStock(request.getUnitsInStock());
        product.setImageUrl(request.getImageUrl());

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Kategori bulunamadı. ID: " + request.getCategoryId()));
        product.setCategory(category);

        Product savedProduct = productRepository.save(product);

        CreatedProductResponse response = new CreatedProductResponse();
        response.setId(savedProduct.getId());
        response.setName(savedProduct.getName());
        response.setDescription(savedProduct.getDescription());
        response.setUnitPrice(savedProduct.getUnitPrice());
        response.setUnitsInStock(savedProduct.getUnitsInStock());
        response.setImageUrl(savedProduct.getImageUrl());
        response.setCategoryId(savedProduct.getCategory().getId());
        response.setCategoryName(savedProduct.getCategory().getName());
        response.setCreatedDate(savedProduct.getCreatedDate());
        return response;
    }

    @Override
    public UpdatedProductResponse update(UpdateProductRequest request) {
        productBusinessRules.checkIfProductExistsById(request.getId());
        productBusinessRules.checkIfProductNameAlreadyExistsForUpdate(request.getId(), request.getName());
        productBusinessRules.checkIfUnitPriceValid(request.getUnitPrice());

        Product product = productRepository.findById(request.getId()).orElseThrow();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setUnitPrice(request.getUnitPrice());
        product.setUnitsInStock(request.getUnitsInStock());
        product.setImageUrl(request.getImageUrl());

        Category updateCategory = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Kategori bulunamadı. ID: " + request.getCategoryId()));
        product.setCategory(updateCategory);

        Product updatedProduct = productRepository.save(product);

        UpdatedProductResponse response = new UpdatedProductResponse();
        response.setId(updatedProduct.getId());
        response.setName(updatedProduct.getName());
        response.setDescription(updatedProduct.getDescription());
        response.setUnitPrice(updatedProduct.getUnitPrice());
        response.setUnitsInStock(updatedProduct.getUnitsInStock());
        response.setImageUrl(updatedProduct.getImageUrl());
        response.setCategoryId(updatedProduct.getCategory().getId());
        response.setCategoryName(updatedProduct.getCategory().getName());
        response.setUpdatedDate(updatedProduct.getUpdatedDate());
        return response;
    }

    @Override
    public DeletedProductResponse delete(int id) {
        productBusinessRules.checkIfProductExistsById(id);

        Product product = productRepository.findById(id).orElseThrow();
        productRepository.delete(product);

        DeletedProductResponse response = new DeletedProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        return response;
    }
}
