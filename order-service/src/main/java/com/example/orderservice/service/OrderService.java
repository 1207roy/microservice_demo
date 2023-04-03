package com.example.orderservice.service;

import com.example.orderservice.config.WebClientConfig;
import com.example.orderservice.dto.InventoryResponse;
import com.example.orderservice.dto.OrderRequest;
import com.example.orderservice.dto.OrderedItemsDto;
import com.example.orderservice.model.Order;
import com.example.orderservice.model.OrderedItems;
import com.example.orderservice.repository.OrderRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;

    private final WebClient webClient;

    public void placeOrder(OrderRequest orderRequest) {

        List<OrderedItems> orderedItemsList = orderRequest.getOrderedItemsDtoList()
                .stream()
                .map(orderedItemsDto -> mapToOrderedItem(orderedItemsDto))
                .collect(Collectors.toList());

        Order order = Order.builder()
                .orderNumber(UUID.randomUUID().toString())
                .orderedItemsList(orderedItemsList)
                .build();

        List<String> skuCodes = order.getOrderedItemsList().stream()
                .map(OrderedItems::getSkuCode)
                .collect(Collectors.toList());

        List<InventoryResponse> inventoryResponses = webClient.get()
                .uri("http://localhost:8082/api/inventory", uriBuilder -> uriBuilder.queryParam("sku-code", skuCodes).build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<InventoryResponse>>() {})
                .block();

        assert inventoryResponses != null;
        boolean productsInStock = inventoryResponses.stream().allMatch(InventoryResponse::getIsInStock);

        if(productsInStock) {
            orderRepository.save(order);
        } else {
            throw new IllegalArgumentException("not a valid product entered...");
        }
    }

    private OrderedItems mapToOrderedItem(OrderedItemsDto orderedItemsDto) {
        return OrderedItems.builder()
                .skuCode(orderedItemsDto.getSkuCode())
                .price(orderedItemsDto.getPrice())
                .quantity(orderedItemsDto.getQuantity())
                .build();
    }
}
