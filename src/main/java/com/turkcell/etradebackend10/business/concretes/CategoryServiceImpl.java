package com.turkcell.etradebackend10.business.concretes;

import com.turkcell.etradebackend10.business.abstracts.CategoryService;
import com.turkcell.etradebackend10.business.rules.CategoryBusinessRules;
import com.turkcell.etradebackend10.dataAccess.abstracts.CategoryRepository;
import com.turkcell.etradebackend10.entities.concretes.Category;
import com.turkcell.etradebackend10.entities.dtos.requests.category.CreateCategoryRequest;
import com.turkcell.etradebackend10.entities.dtos.requests.category.UpdateCategoryRequest;
import com.turkcell.etradebackend10.entities.dtos.responses.category.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryBusinessRules categoryBusinessRules;

    public CategoryServiceImpl(CategoryRepository categoryRepository, CategoryBusinessRules categoryBusinessRules) {
        this.categoryRepository = categoryRepository;
        this.categoryBusinessRules = categoryBusinessRules;
    }

    @Override
    public List<GetAllCategoriesResponse> getAll() {
        List<Category> categories = categoryRepository.findAll();

        return categories.stream().map(category -> {
            GetAllCategoriesResponse response = new GetAllCategoriesResponse();
            response.setId(category.getId());
            response.setName(category.getName());
            return response;
        }).toList();
    }

    @Override
    public GetByIdCategoryResponse getById(int id) {
        categoryBusinessRules.checkIfCategoryExistsById(id);

        Category category = categoryRepository.findById(id).orElseThrow();

        GetByIdCategoryResponse response = new GetByIdCategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());
        response.setCreatedDate(category.getCreatedDate());
        response.setUpdatedDate(category.getUpdatedDate());
        response.setActive(category.isActive());
        return response;
    }

    @Override
    public CreatedCategoryResponse add(CreateCategoryRequest request) {
        categoryBusinessRules.checkIfCategoryNameAlreadyExists(request.getName());

        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());

        Category savedCategory = categoryRepository.save(category);

        CreatedCategoryResponse response = new CreatedCategoryResponse();
        response.setId(savedCategory.getId());
        response.setName(savedCategory.getName());
        response.setDescription(savedCategory.getDescription());
        response.setCreatedDate(savedCategory.getCreatedDate());
        return response;
    }

    @Override
    public UpdatedCategoryResponse update(UpdateCategoryRequest request) {
        categoryBusinessRules.checkIfCategoryExistsById(request.getId());
        categoryBusinessRules.checkIfCategoryNameAlreadyExistsForUpdate(request.getId(), request.getName());

        Category category = categoryRepository.findById(request.getId()).orElseThrow();
        category.setName(request.getName());
        category.setDescription(request.getDescription());

        Category updatedCategory = categoryRepository.save(category);

        UpdatedCategoryResponse response = new UpdatedCategoryResponse();
        response.setId(updatedCategory.getId());
        response.setName(updatedCategory.getName());
        response.setDescription(updatedCategory.getDescription());
        response.setUpdatedDate(updatedCategory.getUpdatedDate());
        return response;
    }

    @Override
    public DeletedCategoryResponse delete(int id) {
        categoryBusinessRules.checkIfCategoryExistsById(id);

        Category category = categoryRepository.findById(id).orElseThrow();
        categoryRepository.delete(category);

        DeletedCategoryResponse response = new DeletedCategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        return response;
    }
}
