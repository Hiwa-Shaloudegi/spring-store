package com.codewithmosh.store.products;

import com.codewithmosh.store.products.Category;
import org.springframework.data.repository.CrudRepository;

public interface CategoryRepository extends CrudRepository<Category, Byte> {
}