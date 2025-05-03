package com.codewithmosh.store.products;

import com.codewithmosh.store.products.ProductDto;
import com.codewithmosh.store.products.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "categoryId", source = "category.id")
    ProductDto toDto(Product product);

    @Mapping(target = "category.id", source = "categoryId")
    Product toEntity(ProductDto productDto);

    @Mapping(target = "id", ignore = true)
    void update(ProductDto productDto, @MappingTarget Product product);

    List<ProductDto> toDtoList(List<Product> products);


    List<Product> toEntityList(List<ProductDto> productDtos);


}
