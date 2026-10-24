package com.example.B2C.modules.wishlist.service;

import com.example.B2C.common.exception.ResourceNotFoundException;
import com.example.B2C.modules.catalog.entity.Product;
import com.example.B2C.modules.catalog.repository.ProductRepository;
import com.example.B2C.modules.user.entity.User;
import com.example.B2C.modules.user.repository.UserRepository;
import com.example.B2C.modules.wishlist.dto.WishlistItemDto;
import com.example.B2C.modules.wishlist.dto.WishlistToggleResponse;
import com.example.B2C.modules.wishlist.entity.Wishlist;
import com.example.B2C.modules.wishlist.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public Page<WishlistItemDto> list(String email, Pageable pageable) {
        User user = getUser(email);
        return wishlistRepository.findByUserOrderByCreatedAtDesc(user, pageable)
            .map(WishlistItemDto::from);
    }

    public WishlistToggleResponse toggle(String email, Long productId) {
        User user = getUser(email);
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
        var existing = wishlistRepository.findByUserAndProduct_Id(user, productId);
        boolean wishlisted;
        if (existing.isPresent()) {
            wishlistRepository.delete(existing.get());
            wishlistRepository.flush();
            wishlisted = false;
        } else {
            Wishlist w = Wishlist.builder().user(user).product(product).build();
            wishlistRepository.save(w);
            wishlisted = true;
        }
        return WishlistToggleResponse.builder()
            .wishlisted(wishlisted)
            .productId(productId)
            .build();
    }

    public void remove(String email, Long productId) {
        User user = getUser(email);
        wishlistRepository.deleteByUserAndProductId(user, productId);
    }

    @Transactional(readOnly = true)
    public boolean isWishlisted(String email, Long productId) {
        User user = getUser(email);
        return wishlistRepository.existsByUserAndProduct_Id(user, productId);
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User", email));
    }
}
