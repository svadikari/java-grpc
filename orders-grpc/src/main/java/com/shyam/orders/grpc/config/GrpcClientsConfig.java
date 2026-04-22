package com.shyam.orders.grpc.config;

import com.shyam.product.grpc.proto.ProductServiceGrpc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

@Configuration
public class GrpcClientsConfig {
    private static final String PRODUCT_SERVICE_CHANNEL = "product-service";

    @Bean
    public ProductServiceGrpc.ProductServiceBlockingStub productServiceBlockingStub(
            GrpcChannelFactory grpcChannelFactory) {
        return ProductServiceGrpc.newBlockingStub(
                grpcChannelFactory.createChannel(PRODUCT_SERVICE_CHANNEL)
        );
    }
}
