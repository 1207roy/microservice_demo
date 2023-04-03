package com.example.inventoryservice.controller;

import com.example.inventoryservice.dto.InventoryResponse;
import com.example.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public List<InventoryResponse> isInStock(@RequestParam(value = "sku-code", required = false) List<String> skuCodes) {
        return inventoryService.isInStock(skuCodes);
    }

//    @GetMapping()
//    @ResponseStatus(HttpStatus.OK)
//    public List<InventoryResponse> isInStock() {
//
//        String[] list = new String[2];
//        list[0] = "Samsung_M31S";
//        list[1] = "IPhone_13X";
//
//        return inventoryService.isInStock(Arrays.asList(list));
//    }
}
