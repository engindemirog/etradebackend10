package com.turkcell.etradebackend10.business.abstracts;

import com.turkcell.etradebackend10.entities.dtos.requests.category.CreateCategoryRequest;
import com.turkcell.etradebackend10.entities.dtos.requests.category.UpdateCategoryRequest;
import com.turkcell.etradebackend10.entities.dtos.responses.category.*;

import java.util.List;

public interface CategoryService {

    List<GetAllCategoriesResponse> getAll();

    GetByIdCategoryResponse getById(int id);

    CreatedCategoryResponse add(CreateCategoryRequest request);

    UpdatedCategoryResponse update(UpdateCategoryRequest request);

    DeletedCategoryResponse delete(int id);
}
