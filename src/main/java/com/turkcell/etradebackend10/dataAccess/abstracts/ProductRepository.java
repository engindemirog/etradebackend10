package com.turkcell.etradebackend10.dataAccess.abstracts;

import com.turkcell.etradebackend10.entities.concretes.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, int id);
}
