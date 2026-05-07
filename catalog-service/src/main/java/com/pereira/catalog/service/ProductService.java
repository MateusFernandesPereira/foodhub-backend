package com.pereira.catalog.service;

import com.pereira.catalog.dto.request.CreateProductRequest;
import com.pereira.catalog.dto.request.UpdateProductRequest;
import com.pereira.catalog.dto.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {

    ProductResponse create(CreateProductRequest request);

    Page<ProductResponse> findAll(Pageable pageable);

    ProductResponse findById(Long id);

    ProductResponse update(Long id, UpdateProductRequest request);

    void delete(Long id);

    void restoreById(Long id);
}
