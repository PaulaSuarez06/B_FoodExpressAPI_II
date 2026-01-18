package es.daw.foodexpressapi.service;


import es.daw.foodexpressapi.dto.*;
import es.daw.foodexpressapi.dto.order.*;
import es.daw.foodexpressapi.dto.order.OrderCreatedResponseDTO;
import es.daw.foodexpressapi.dto.report.CustomerSpendDTO;
import es.daw.foodexpressapi.entity.*;
import es.daw.foodexpressapi.enums.OrderStatus;
import es.daw.foodexpressapi.exception.InvalidStatusException;
import es.daw.foodexpressapi.exception.RestaurantNotFoundException;
import es.daw.foodexpressapi.exception.UserNotFoundException;
import es.daw.foodexpressapi.mapper.OrderMapper;
import es.daw.foodexpressapi.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final OrderMapper orderMapper;
    private final OrderDetailRepository orderDetailRepository;
    private final DishRepository dishRepository;

    public Page<OrderResponseDTO> filterOrders(String status, Long userId, Long restaurantId, Pageable pageable) {


        OrderStatus orderStatus = null;
        if (status != null) {
            if (!OrderStatus.isValid(status))
                throw new InvalidStatusException(status);
            orderStatus = OrderStatus.valueOf(status);
        }


        if (status != null && !OrderStatus.isValid(status)) {
            throw new InvalidStatusException(status);
        }

        if (userId != null && !userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }

        if (restaurantId != null && !restaurantRepository.existsById(restaurantId)) {
            throw new RestaurantNotFoundException(restaurantId);
        }


        Page<Order> orders = orderRepository.findByFilters(orderStatus, userId, restaurantId, pageable);

        return orders.map(orderMapper::toResponse);
    }





    public List<OrderSummaryDTO> getAllOrderSummaries() {
        return orderRepository.findAllOrderSummaries();
    }

    public List<CustomerTotalDTO> getAllCustomerTotals() {
        return orderRepository.findAllCustomerTotals();
    }

    public List<RestaurantOrderCountDTO> getAllRestaurantOrderCounts() {
        return orderRepository.findAllRestaurantOrderCounts();
    }

    public List<DishesOrderCountDTO> getAllDishesOrderCounts() {
        return orderRepository.findAllDishesOrderCounts();
    }

    @Transactional
    public OrderCreatedResponseDTO createOrder(CreateOrderDTO dto) {

        // Cargar agregados principales
        User user = userRepository.findById(dto.userId())
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + dto.userId()));

        Restaurant restaurant = restaurantRepository.findById(dto.restaurantId())
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found: " + dto.restaurantId()));

        // Crear Order --------ESTO ES LA ENTIDAD
        Order order = new Order();
        order.setUser(user);
        order.setRestaurant(restaurant);
        order.setStatus(OrderStatus.CREADO);
        order.setOrderDate(LocalDateTime.now());

        orderRepository.save(order);

        // Crear detalles
        BigDecimal total = BigDecimal.ZERO;
        List<OrderLineResponseDTO> responseLines = new ArrayList<>();

        for (OrderItemDTO item : dto.items()) {

            Dish dish = dishRepository.findById(item.dishId())
                    .orElseThrow(() -> new EntityNotFoundException("Dish not found: " + item.dishId()));

            // Validar que el plato pertenece al restaurante del pedido
            if (dish.getRestaurant() == null || dish.getRestaurant().getId() == null ||
                    !dish.getRestaurant().getId().equals(dto.restaurantId())) {
                throw new IllegalArgumentException("Dish " + dish.getId() + " does not belong to restaurant " + dto.restaurantId());
            }

            BigDecimal unitPrice = dish.getPrice(); // o dish.getBasePrice() / price final según tu modelo
            if (unitPrice == null) {
                throw new IllegalStateException("Dish price is null for dishId=" + dish.getId());
            }

            int qty = item.quantity();
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(qty));

            OrderDetail detail = new OrderDetail();
            detail.setOrder(order);
            detail.setDish(dish);
            detail.setQuantity(qty);
            detail.setSubtotal(subtotal);

            // CLAVE: inicializar el EmbeddedId
            detail.setOrderDetailId(new OrderDetailId(order.getId(), dish.getId()));

            orderDetailRepository.save(detail);

            total = total.add(subtotal);

            responseLines.add(new OrderLineResponseDTO(
                    dish.getId(),
                    dish.getName(),
                    unitPrice,
                    qty,
                    subtotal
            ));
        }


        // Respuesta
        return new OrderCreatedResponseDTO(
                order.getId(),
                user.getId(),
                restaurant.getId(),
                //order.getStatus(),
                order.getStatus().name(),
                order.getOrderDate(),
                total,
                responseLines
        );
    }

}
