package com.turkcell.etradebackend10.dataAccess.abstracts;

import com.turkcell.etradebackend10.entities.concretes.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Integer> {

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, int id);
}
