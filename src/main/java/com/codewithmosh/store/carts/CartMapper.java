package com.codewithmosh.store.carts;

import com.codewithmosh.store.carts.CartDto;
import com.codewithmosh.store.carts.CartItemDto;
import com.codewithmosh.store.carts.Cart;
import com.codewithmosh.store.carts.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CartMapper {
    @Mapping(target = "totalPrice", expression = "java(cart.getTotalPrice())")
    CartDto toDto(Cart cart);

    @Mapping(target = "totalPrice", expression = "java(cartItem.getTotalPrice())")
    CartItemDto toDto(CartItem cartItem);
}
