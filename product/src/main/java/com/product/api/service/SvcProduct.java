package com.product.api.service;

import java.util.List;
import java.util.Map;

import com.product.api.dto.ApiResponse;
import com.product.api.dto.DtoProductList;
import com.product.api.entity.Category;
import com.product.api.entity.Product;

public interface SvcProduct {

	public List<DtoProductList> getProducts(Integer category_id);
	public Product getProduct(String gtin);
	public ApiResponse createProduct(Product in);
	public ApiResponse updateProduct(Product in, Integer id);
	public ApiResponse deleteProduct(Integer id);
	
	public ApiResponse updateProductCategory(Category category, Integer id);
	public ApiResponse updateProductStock(Map<String, Integer> productos);
	
}
