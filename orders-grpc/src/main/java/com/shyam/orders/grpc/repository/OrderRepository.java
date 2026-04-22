package com.shyam.orders.grpc.repository;

import com.shyam.orders.grpc.proto.Order;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
@Repository
public class OrderRepository {
    static List<Order> orders = new ArrayList<>();

    public void  addOrder(Order order) {
        orders.add(order);
    }
    public List<Order> getOrders() {
        return orders;
    }

    public Order getOrderById(String orderId) {
        return orders.stream().filter(order -> order.getId().equals(orderId)).findFirst().orElse(null);
    }

    public void addAllOrders(List<Order> ordersList) {
        orders.addAll(ordersList);
    }
}
