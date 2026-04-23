package com.shyam.product.grpc.repository;

import com.shyam.product.grpc.proto.Product;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class ProductRepository {
    static List<Product> productList = new ArrayList<>();

  static {
    productList.add(Product.newBuilder().setId("111").setName("Product 1").setPrice(1.11).setActive(true).build());
    productList.add(Product.newBuilder().setId("222").setName("Product 2").setPrice(2.22).setActive(true).build());
    productList.add(Product.newBuilder().setId("333").setName("Product 3").setPrice(3.33).setActive(true).build());
    }

    public void addProduct(Product product) {
        productList.add(product);
    }

    public List<Product> getAllProducts() {
        return productList;
    }

    public Product getProductById(String id) {
      return productList.stream().filter(product -> product.getId().equals(id)).findFirst().orElse(null);
    }
}
