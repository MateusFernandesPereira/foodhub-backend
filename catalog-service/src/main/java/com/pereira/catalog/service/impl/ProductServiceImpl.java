package com.pereira.catalog.service.impl;

import com.pereira.catalog.dto.request.CreateProductRequest;
import com.pereira.catalog.dto.request.UpdateProductRequest;
import com.pereira.catalog.dto.response.ProductResponse;
import com.pereira.catalog.entity.ProductEntity;
import com.pereira.catalog.repository.ProductRepository;
import com.pereira.catalog.service.ProductService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    @Transactional
    public ProductResponse create(CreateProductRequest request) {
        ProductEntity entity = ProductEntity.builder()
            .name(request.name())
            .description(request.description())
            .price(request.price())
            .category(request.category())
            .imageUrl(request.imageUrl())
            .available(request.available())
            .stockQuantity(request.stockQuantity())
            .build();

        return ProductResponse.from(productRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> findAll() {
        return productRepository.findAll()
            .stream()
            .map(ProductResponse::from)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        return productRepository.findById(id)
            .map(ProductResponse::from)
            .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));
    }

    @Override
    @Transactional
    public ProductResponse update(Long id, UpdateProductRequest request) {
        ProductEntity entity = productRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));

        if (request.name() != null)          entity.setName(request.name());
        if (request.description() != null)   entity.setDescription(request.description());
        if (request.price() != null)         entity.setPrice(request.price());
        if (request.category() != null)      entity.setCategory(request.category());
        if (request.imageUrl() != null)      entity.setImageUrl(request.imageUrl());
        if (request.available() != null)     entity.setAvailable(request.available());
        if (request.stockQuantity() != null) entity.setStockQuantity(request.stockQuantity());

        return ProductResponse.from(productRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new EntityNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

}
