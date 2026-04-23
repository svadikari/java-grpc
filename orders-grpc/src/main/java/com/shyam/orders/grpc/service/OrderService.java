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
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.grpc.server.service.GrpcService;

import java.util.ArrayList;
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

  @Override
  public StreamObserver<Order> createBulkOrders(StreamObserver<OrderResponse> responseObserver) {
    List<Order> orders = new ArrayList<>();
    return new StreamObserver<>() {
      @Override
      public void onNext(Order order) {
        log.info("Order received from: {} for {} items", order.getCreatedBy(), order.getItemsList().size());
        order = order.toBuilder().setId(UUID.randomUUID().toString()).build();
        orders.add(order);
      }

      @Override
      public void onError(Throwable throwable) {
        responseObserver.onError(Status.INTERNAL.withDescription(throwable.getMessage()).asRuntimeException());
      }

      @Override
      public void onCompleted() {
        orderRepository.addAllOrders(orders);
        log.info("Orders created...");
        responseObserver.onNext(OrderResponse.newBuilder().addAllOrder(orders).build());
        responseObserver.onCompleted();
      }
    };

  }

  @Override
  public StreamObserver<Order> createOrdersUsingStream(StreamObserver<Order> responseObserver) {
    return new StreamObserver<>() {
      @Override
      public void onNext(Order order) {
        log.info("Order received from: {} for {} items", order.getCreatedBy(), order.getItemsList().size());
        order = order.toBuilder().setId(UUID.randomUUID().toString()).build();
        orderRepository.addOrder(order);
        responseObserver.onNext(order);
      }

      @Override
      public void onError(Throwable throwable) {
        responseObserver.onError(Status.INTERNAL.withDescription(throwable.getMessage()).asRuntimeException());
      }

      @Override
      public void onCompleted() {
        log.info("Orders creation completed...");
        responseObserver.onCompleted();
      }
    };
  }

  @Override
  public void getOrders(Empty request, StreamObserver<Order> responseObserver) {
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
    orders.forEach(responseObserver::onNext);
    responseObserver.onCompleted();
  }

  @Override
  public void getOrder(Order request, StreamObserver<Order> responseObserver) {
    responseObserver.onNext(orderRepository.getOrderById(request.getId()));
    responseObserver.onCompleted();
  }

  private Product getProduct(String productId) {
    try {
      Product product =
              productServiceStub.getProduct(ProductIdRequest.newBuilder().setId(productId).build());
      return Optional.ofNullable(product).orElse(Product.newBuilder().build());
    }catch (Exception ex){
      log.error(ex.getMessage());
      ex.printStackTrace();
      return Product.newBuilder().build();
    }
  }
}
