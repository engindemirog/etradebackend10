package com.turkcell.etradebackend10.business.rules;

import com.turkcell.etradebackend10.dataAccess.abstracts.CategoryRepository;
import org.springframework.stereotype.Service;

@Service
public class CategoryBusinessRules {

    private final CategoryRepository categoryRepository;

    public CategoryBusinessRules(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public void checkIfCategoryExistsById(int id) {
        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("Kategori bulunamadı. ID: " + id);
        }
    }

    public void checkIfCategoryNameAlreadyExists(String name) {
        if (categoryRepository.existsByName(name)) {
            throw new RuntimeException("Bu kategori adı zaten mevcut: " + name);
        }
    }

    public void checkIfCategoryNameAlreadyExistsForUpdate(int id, String name) {
        if (categoryRepository.existsByNameAndIdNot(name, id)) {
            throw new RuntimeException("Bu kategori adı zaten başka bir kategoriye ait: " + name);
        }
    }
}
