package com.turkcell.etradebackend10.api.controllers;

import com.turkcell.etradebackend10.business.abstracts.CategoryService;
import com.turkcell.etradebackend10.entities.dtos.requests.category.CreateCategoryRequest;
import com.turkcell.etradebackend10.entities.dtos.requests.category.UpdateCategoryRequest;
import com.turkcell.etradebackend10.entities.dtos.responses.category.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoriesController {

    private final CategoryService categoryService;

    public CategoriesController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<GetAllCategoriesResponse> getAll() {
        return categoryService.getAll();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public GetByIdCategoryResponse getById(@PathVariable int id) {
        return categoryService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreatedCategoryResponse add(@RequestBody @Valid CreateCategoryRequest request) {
        return categoryService.add(request);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public UpdatedCategoryResponse update(@RequestBody @Valid UpdateCategoryRequest request) {
        return categoryService.update(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public DeletedCategoryResponse delete(@PathVariable int id) {
        return categoryService.delete(id);
    }
}
