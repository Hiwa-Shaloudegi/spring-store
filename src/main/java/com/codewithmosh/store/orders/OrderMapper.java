package com.codewithmosh.store.orders;

import com.codewithmosh.store.orders.OrderDto;
import com.codewithmosh.store.orders.Order;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    OrderDto toDto(Order order);
}
