package com.shyam.product.grpc;

import com.shyam.product.grpc.proto.Empty;
import com.shyam.product.grpc.proto.ProductRequest;
import com.shyam.product.grpc.proto.ProductResponse;
import com.shyam.product.grpc.proto.ProductServiceGrpc;
import com.shyam.product.grpc.repository.ProductRepository;
import com.shyam.product.grpc.service.ProductService;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.stub.StreamObserver;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProductsGrpcApplicationTests {

    @Test
    void createProductReturnsAValidProductResponse() {
        ProductRepository repository = new ProductRepository();
        ProductService service = new ProductService(repository);
        CapturingObserver observer = new CapturingObserver();
        int initialSize = repository.getAllProducts().size();

        service.createProduct(ProductRequest.newBuilder()
                .setName("New Product")
                .setPrice(9.99)
                .setActive(true)
                .build(), observer);

        assertThat(observer.error).isNull();
        assertThat(observer.completed).isTrue();
        assertThat(observer.response).isNotNull();
        assertThat(observer.response.getProductsCount()).isEqualTo(1);
        assertThat(observer.response.getProducts(0).getId()).isNotBlank();
        assertThat(observer.response.getProducts(0).getName()).isEqualTo("New Product");
        assertThat(observer.response.getProducts(0).getPrice()).isEqualTo(9.99);
        assertThat(observer.response.getProducts(0).getActive()).isTrue();
        assertThat(repository.getAllProducts()).hasSize(initialSize + 1);
    }

    @Test
    void getProductsReturnsRepositoryContents() {
        ProductRepository repository = new ProductRepository();
        ProductService service = new ProductService(repository);
        CapturingObserver observer = new CapturingObserver();

        service.getProducts(Empty.getDefaultInstance(), observer);

        assertThat(observer.error).isNull();
        assertThat(observer.completed).isTrue();
        assertThat(observer.response).isNotNull();
        assertThat(observer.response.getProductsCount()).isEqualTo(repository.getAllProducts().size());
    }

    @Test
    void grpcRoundTripParsesProductResponseSuccessfully() throws Exception {
        ProductRepository repository = new ProductRepository();
        ProductService service = new ProductService(repository);
        String serverName = InProcessServerBuilder.generateName();
        Server server = InProcessServerBuilder.forName(serverName)
                .directExecutor()
                .addService(service)
                .build()
                .start();
        ManagedChannel channel = InProcessChannelBuilder.forName(serverName)
                .directExecutor()
                .build();

        try {
            ProductResponse response = ProductServiceGrpc.newBlockingStub(channel)
                    .createProduct(ProductRequest.newBuilder()
                            .setName("Wire Test")
                            .setPrice(5.55)
                            .setActive(true)
                            .build());

            assertThat(response.getProductsCount()).isEqualTo(1);
            assertThat(response.getProducts(0).getName()).isEqualTo("Wire Test");
        } finally {
            channel.shutdownNow();
            server.shutdownNow();
        }
    }

    private static final class CapturingObserver implements StreamObserver<ProductResponse> {
        private ProductResponse response;
        private Throwable error;
        private boolean completed;

        @Override
        public void onNext(ProductResponse value) {
            this.response = value;
        }

        @Override
        public void onError(Throwable t) {
            this.error = t;
        }

        @Override
        public void onCompleted() {
            this.completed = true;
        }
    }
}
