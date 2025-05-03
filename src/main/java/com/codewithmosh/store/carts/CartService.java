package com.codewithmosh.store.carts;

import com.codewithmosh.store.carts.CartDto;
import com.codewithmosh.store.carts.CartItemDto;
import com.codewithmosh.store.carts.Cart;
import com.codewithmosh.store.carts.CartItem;
import com.codewithmosh.store.products.Product;
import com.codewithmosh.store.carts.CartNotFoundException;
import com.codewithmosh.store.products.ProductNotFoundException;
import com.codewithmosh.store.carts.CartMapper;
import com.codewithmosh.store.carts.CartRepository;
import com.codewithmosh.store.products.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@AllArgsConstructor
@Service
public class CartService {
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    private final CartMapper cartMapper;

    public CartDto createCart() {
        var cart = new Cart();
        cartRepository.save(cart);
        return cartMapper.toDto(cart);
    }

    public CartDto getCart(UUID cartId) {
        var cart = cartRepository.getCartWithItems(cartId).orElse(null);
        if (cart == null) throw new CartNotFoundException();
        return cartMapper.toDto(cart);
    }

    public CartItemDto addToCart(UUID cartId, Long productId) {
        var cart = cartRepository.getCartWithItems(cartId).orElse(null);
        if (cart == null) throw new CartNotFoundException();

        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) throw new ProductNotFoundException();

        CartItem cartItem = cart.addItem(product);
        cartRepository.save(cart);
        return cartMapper.toDto(cartItem);
    }


    public CartItemDto updateItem(UUID cartId, Long productId, Integer quantity) {

        Cart cart = cartRepository.getCartWithItems(cartId).orElse(null);
        if (cart == null) throw new CartNotFoundException();

        CartItem cartItem = cart.getItem(productId);

        if (cartItem == null) throw new ProductNotFoundException();

        cartItem.setQuantity(quantity);
        cartRepository.save(cart);

        return cartMapper.toDto(cartItem);
    }


    public void removeItem(UUID cartId, Long productId) {
        Cart cart = cartRepository.getCartWithItems(cartId).orElse(null);
        if (cart == null) throw new CartNotFoundException();

        cart.removeItem(productId);

        cartRepository.save(cart);
    }

    public void clearCart(UUID cartId) {
        Cart cart = cartRepository.getCartWithItems(cartId).orElse(null);
        if (cart == null) throw new CartNotFoundException();

        cart.clearCartItems();
        cartRepository.save(cart);

    }
}
