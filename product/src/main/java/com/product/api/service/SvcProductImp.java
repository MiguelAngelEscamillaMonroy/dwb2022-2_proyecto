package com.product.api.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.product.api.dto.ApiResponse;
import com.product.api.dto.DtoProductList;
import com.product.api.entity.Category;
import com.product.api.entity.Product;
import com.product.api.repository.RepoCategory;
import com.product.api.repository.RepoProduct;
import com.product.api.repository.RepoProductList;
import com.product.exception.ApiException;

@Service
public class SvcProductImp implements SvcProduct {

	@Autowired
	RepoProduct repo;
	
	@Autowired
	RepoCategory repoCategory;
	
	@Autowired
	RepoProductList repoProductList;
	
	@Override
	public List<DtoProductList> getProducts(Integer category_id) {
		return repoProductList.findByCategory(category_id);
	}

	@Override
	public Product getProduct(String gtin) {
		Product product = repo.findByGtinAndStatus(gtin,1);
		if (product != null) {
			product.setCategory(repoCategory.findByCategoryId(product.getCategory_id()));
			return product;
		}else
			throw new ApiException(HttpStatus.NOT_FOUND, "product does not exist");
	}

	@Override
	public ApiResponse createProduct(Product in) {
		Product product = repo.findByGtinAndStatus(in.getGtin(),0);
		if(product != null) {
			updateProduct(in,product.getProduct_id());
			return new ApiResponse("product activated");
		}else {
			try {
				in.setStatus(1);
				repo.save(in);
			}catch (DataIntegrityViolationException e) {
				if (e.getLocalizedMessage().contains("gtin"))
					throw new ApiException(HttpStatus.BAD_REQUEST, "product gtin already exist");
				if (e.getLocalizedMessage().contains("product"))
					throw new ApiException(HttpStatus.BAD_REQUEST, "product name already exist");
			}
			return new ApiResponse("product created");
		}
	}

	@Override
	public ApiResponse updateProduct(Product in, Integer id) {
		try {
			repo.updateProduct(id, in.getGtin(), in.getProduct(), in.getDescription(), in.getPrice(), in.getStock());
		}catch (DataIntegrityViolationException e) {
			if (e.getLocalizedMessage().contains("gtin"))
				throw new ApiException(HttpStatus.BAD_REQUEST, "product gtin already exist");
			if (e.getLocalizedMessage().contains("product"))
				throw new ApiException(HttpStatus.BAD_REQUEST, "product name already exist");
		}
		return new ApiResponse("product updated");
	}

	@Override
	public ApiResponse deleteProduct(Integer id) {
		if (repo.deleteProduct(id) > 0)
			return new ApiResponse("product removed");
		else
			throw new ApiException(HttpStatus.BAD_REQUEST, "product cannot be deleted");
	}
	
	@Override
	public ApiResponse updateProductCategory(Category category, Integer id) {
		if (repoCategory.findByCategoryId(category.getCategory_id()) == null) {
			throw new ApiException(HttpStatus.NOT_FOUND, "category not found");
		} else if (repo.updateProductCategory(category.getCategory_id(), id) > 0) {
			return new ApiResponse("product category updated");
		} else {
			throw new ApiException(HttpStatus.BAD_REQUEST, "product category cannot be updated");
		}
		
		/*
		try {
			if (repo.updateProductCategory(category.getCategory_id(), id) > 0) {
				return new ApiResponse("product category updated");
			} else {
				throw new ApiException(HttpStatus.BAD_REQUEST, "product category cannot be updated");
			}
		} catch (DataIntegrityViolationException e) {
			throw new ApiException(HttpStatus.NOT_FOUND, "category not found");
		}
		*/
	}
	
	@Override
	public ApiResponse updateProductStock(Map<String, Integer> productos) {
		for (Map.Entry<String, Integer> producto : productos.entrySet()) {
			if (repo.updateProductStock(producto.getKey(), producto.getValue()) == 0) {
				throw new ApiException(HttpStatus.BAD_REQUEST, 
						String.format("stock for %s cannot be updated", producto.getKey()));
			}			
		}
		
		return new ApiResponse("product stock updated");
	}

}
