package es.daw.foodexpressapi.controller;

import es.daw.foodexpressapi.dto.*;
import es.daw.foodexpressapi.dto.order.CreateOrderDTO;
import es.daw.foodexpressapi.dto.order.OrderCreatedResponseDTO;
import es.daw.foodexpressapi.dto.order.OrderResponseDTO;
import es.daw.foodexpressapi.dto.order.OrderSummaryDTO;
import es.daw.foodexpressapi.service.OrderService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
//@Validated //?????????????
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<Page<OrderResponseDTO>> filterOrders(
            @RequestParam(required = false) String status,
            @Min(1) @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long restaurantId,
            @PageableDefault(size = 10, sort = "orderDate", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {

            return ResponseEntity.ok(orderService.filterOrders(status, userId, restaurantId, pageable));

    }

    @GetMapping("/summary")
    public ResponseEntity<List<OrderSummaryDTO>> getOrderSummaries() {
        return ResponseEntity.ok(orderService.getAllOrderSummaries());
    }

    // PENDIENTE!!!!! CREAR PEDIDO
    @PostMapping
    public ResponseEntity<OrderCreatedResponseDTO> crearPedido(@RequestBody CreateOrderDTO dto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(orderService.createOrder(dto));
    }


    // -- JUAN --- MEJOR LLEVARLOS A UN ReportController....
    @GetMapping("/totals-by-customer")
    public ResponseEntity<List<CustomerTotalDTO>> getCustomerTotalsByCustomer(){
        return ResponseEntity.ok(orderService.getAllCustomerTotals());
    }

    @GetMapping("/best-restaurants")
    public ResponseEntity<List<RestaurantOrderCountDTO>> getBestRestaurant(){
        return ResponseEntity.ok(orderService.getAllRestaurantOrderCounts());
    }

    @GetMapping("/best-dishes")
    public ResponseEntity<List<DishesOrderCountDTO>> getBestDishes(){
        return ResponseEntity.ok(orderService.getAllDishesOrderCounts());
    }
}
