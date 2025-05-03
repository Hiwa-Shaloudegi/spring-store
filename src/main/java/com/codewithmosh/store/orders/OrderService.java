package com.codewithmosh.store.orders;

import com.codewithmosh.store.auth.AuthService;
import com.codewithmosh.store.users.User;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class OrderService {
    private final AuthService authService;
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;


    public List<OrderDto> getAllOrders() {
        User currentUser = authService.getCurrentUser();

        List<Order> orders = orderRepository.getOrdersByCustomer(currentUser);
        return orders.stream().map(orderMapper::toDto).toList();

    }

    public OrderDto getOrder(Long id) {
        Order order = orderRepository.getOrderWithItems(id).orElseThrow(OrderNotFoundException::new);

        User currentUser = authService.getCurrentUser();
        if (!order.isPlacedBy(currentUser)) {
            throw new AccessDeniedException("You don't have access to access this order");
        }

        return orderMapper.toDto(order);
    }
}
