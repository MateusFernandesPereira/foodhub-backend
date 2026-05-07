package com.pereira.catalog.service;

import com.pereira.catalog.dto.request.CreateProductRequest;
import com.pereira.catalog.dto.request.UpdateProductRequest;
import com.pereira.catalog.dto.response.ProductResponse;

import java.util.List;

public interface ProductService {

    ProductResponse create(CreateProductRequest request);

    List<ProductResponse> findAll();

    ProductResponse findById(Long id);

    ProductResponse update(Long id, UpdateProductRequest request);

    void delete(Long id);

    void restoreById(Long id);
}
