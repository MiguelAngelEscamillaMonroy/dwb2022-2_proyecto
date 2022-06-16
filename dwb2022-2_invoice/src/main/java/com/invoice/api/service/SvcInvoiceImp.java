package com.invoice.api.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.invoice.api.dto.ApiResponse;
import com.invoice.api.dto.DtoProduct;
import com.invoice.api.entity.Cart;
import com.invoice.api.entity.Invoice;
import com.invoice.api.entity.Item;
import com.invoice.api.repository.RepoCart;
import com.invoice.api.repository.RepoInvoice;
import com.invoice.api.repository.RepoItem;
import com.invoice.configuration.client.ProductClient;
import com.invoice.exception.ApiException;

@Service
public class SvcInvoiceImp implements SvcInvoice {

	@Autowired
	RepoInvoice repo;
	
	@Autowired
	RepoItem repoItem;
	
	@Autowired
	RepoCart repoCart;
	
	@Autowired
	ProductClient productCl;
	
	@Autowired
	SvcCart svcCart;

	@Override
	public List<Invoice> getInvoices(String rfc) {
		return repo.findByRfcAndStatus(rfc, 1);
	}

	@Override
	public List<Item> getInvoiceItems(Integer invoice_id) {
		return repoItem.getInvoiceItems(invoice_id);
	}

	@Override
	public ApiResponse generateInvoice(String rfc) {
		/*
		 * Sprint 3 - Requerimiento 5
		 * Implementar el método para generar una factura 
		 */
		List<Cart> carrito = repoCart.findByRfcAndStatus(rfc, 1);
		if (carrito.isEmpty()) {
			throw new ApiException(HttpStatus.NOT_FOUND, "cart has no items");
		}
		
		Invoice factura = new Invoice();
		factura.setRfc(rfc);
		factura.setSubtotal(0.0);
		factura.setTaxes(0.0);
		factura.setTotal(0.0);
		factura.setCreated_at(LocalDateTime.now());
		factura.setStatus(0);
		factura = repo.save(factura);
		
		Map<String, Integer> vendidos = new HashMap<>();
		Double subtotal = 0.0;
		Double taxes = 0.0;
		Double invoice_total = 0.0;
		
		for (Cart articulo: carrito) {
			DtoProduct producto = getProduct(articulo.getGtin());
			Item item = new Item();
			item.setId_invoice(factura.getInvoice_id());
			item.setGtin(articulo.getGtin());
			item.setUnit_price(producto.getPrice());
			Integer cantidad = articulo.getQuantity();
			Double total = producto.getPrice()*cantidad;
			item.setTotal(total);
			item.setTaxes(total*0.16);
			item.setSubtotal(total-(total*0.16));
			item.setQuantity(cantidad);
			item.setStatus(1);
			repoItem.save(item);
			
			vendidos.put(articulo.getGtin(), cantidad);
			
			subtotal += total-(total*0.16);
			taxes += total*0.16;
			invoice_total += total;
		}
		
		productCl.updateProductStock(vendidos);
		
		factura.setSubtotal(subtotal);
		factura.setTaxes(taxes);
		factura.setTotal(invoice_total);
		factura.setStatus(1);
		
		svcCart.clearCart(rfc);
		
		return new ApiResponse("invoice generated");
	}
	
	private DtoProduct getProduct(String gtin) {
		try {
			ResponseEntity<DtoProduct> response = productCl.getProduct(gtin);
			if (response.getStatusCode() == HttpStatus.OK) {
				return response.getBody();
			}
			
			return null;
		} catch (Exception e) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "unable to retrieve product information");
		}
	}

}
