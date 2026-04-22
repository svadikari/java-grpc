package com.shyam.product.grpc.service;

import com.shyam.product.grpc.proto.Empty;
import com.shyam.product.grpc.proto.Product;
import com.shyam.product.grpc.proto.ProductIdRequest;
import com.shyam.product.grpc.proto.ProductRequest;
import com.shyam.product.grpc.proto.ProductResponse;
import com.shyam.product.grpc.proto.ProductServiceGrpc;
import com.shyam.product.grpc.repository.ProductRepository;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.grpc.server.service.GrpcService;

import java.util.UUID;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class ProductService extends ProductServiceGrpc.ProductServiceImplBase {
    private final ProductRepository productRepository;
    @Override
    public void createProduct(ProductRequest request, StreamObserver<ProductResponse> responseObserver) {
        Product product = Product.newBuilder()
                .setId(String.valueOf(UUID.randomUUID()))
                .setActive(request.getActive())
                .setName(request.getName())
                .setPrice(request.getPrice())
                .build();

        productRepository.addProduct(product);
        responseObserver.onNext(ProductResponse.newBuilder().addProducts(product).build());
        responseObserver.onCompleted();
    }

    @Override
    public void getProducts(Empty request, StreamObserver<ProductResponse> responseObserver) {
        responseObserver.onNext(ProductResponse.newBuilder().addAllProducts(productRepository.getAllProducts()).build());
        responseObserver.onCompleted();
    }

    @Override
    public void getProduct(ProductIdRequest request, StreamObserver<Product> responseObserver) {
        log.info("Get product by id {}", request.getId());
        responseObserver.onNext(productRepository.getProductById(request.getId()));
        responseObserver.onCompleted();
    }
}
