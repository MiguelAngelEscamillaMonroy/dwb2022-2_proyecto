package com.invoice.configuration.client;

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.invoice.api.dto.ApiResponse;
import com.invoice.api.dto.DtoProduct;

/*
 * Sprint 1 - Requerimiento 2
 * Actualizar método getProduct para obtener la información necesaria de un producto
 */

/*
 * Sprint 3 - Requerimiento 5
 * Agregar método updateProductStock para actualizar el stock de productos
 */
@FeignClient(name = "product-service")
public interface ProductClient {
	
	@GetMapping("product/{gtin}")
	public ResponseEntity<DtoProduct> getProduct(@PathVariable("gtin") String gtin);
	
	@PutMapping("product/stock")
	public ResponseEntity<ApiResponse> updateProductStock(@RequestBody Map<String, Integer> productos);
}
