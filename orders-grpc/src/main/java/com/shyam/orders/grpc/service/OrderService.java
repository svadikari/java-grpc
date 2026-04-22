package com.shyam.orders.grpc.service;

import com.shyam.orders.grpc.proto.Empty;
import com.shyam.orders.grpc.proto.Order;
import com.shyam.orders.grpc.proto.OrderItem;
import com.shyam.orders.grpc.proto.OrderResponse;
import com.shyam.orders.grpc.proto.OrderServiceGrpc;
import com.shyam.orders.grpc.repository.OrderRepository;
import com.shyam.product.grpc.proto.Product;
import com.shyam.product.grpc.proto.ProductIdRequest;
import com.shyam.product.grpc.proto.ProductServiceGrpc;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.grpc.server.service.GrpcService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class OrderService extends OrderServiceGrpc.OrderServiceImplBase {
  private final OrderRepository orderRepository;
  private final ProductServiceGrpc.ProductServiceBlockingStub productServiceStub;

  @Override
  public void createOrder(Order order, StreamObserver<Order> responseObserver) {
    Order.Builder builder = order.toBuilder();
    builder.setId(UUID.randomUUID().toString());
    Order newOrder = builder.build();
    orderRepository.addOrder(newOrder);
    List<OrderItem> orderItems =
        newOrder.getItemsList().stream()
            .map(
                orderItem ->
                    orderItem.toBuilder()
                        .setProductName(getProduct(orderItem.getProductId()).getName())
                        .build())
            .toList();
    newOrder = newOrder.toBuilder().clearItems().addAllItems(orderItems).build();
    responseObserver.onNext(newOrder);
    responseObserver.onCompleted();
  }

  private Product getProduct(String productId) {
    Product product =
        productServiceStub.getProduct(ProductIdRequest.newBuilder().setId(productId).build());
    return Optional.ofNullable(product).orElse(Product.newBuilder().build());
  }

  @Override
  public void getOrders(Empty request, StreamObserver<OrderResponse> responseObserver) {
    List<Order> orders = orderRepository.getOrders();
    orders =
        orders.stream()
            .map(
                order -> {
                  List<OrderItem> orderItems =
                      order.getItemsList().stream()
                          .map(
                              orderItem -> orderItem.toBuilder()
                                  .setProductName(getProduct(orderItem.getProductId()).getName())
                                  .build())
                          .toList();
                  return order.toBuilder().clearItems().addAllItems(orderItems).build();
                })
            .toList();
    responseObserver.onNext(OrderResponse.newBuilder().addAllOrder(orders).build());
    responseObserver.onCompleted();
  }
}
