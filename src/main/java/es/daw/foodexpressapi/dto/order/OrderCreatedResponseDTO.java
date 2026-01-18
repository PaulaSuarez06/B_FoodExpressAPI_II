package es.daw.foodexpressapi.dto.order;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderCreatedResponseDTO(
        Long orderId,
        Long userId,
        Long restaurantId,
        String status,              // tú estás devolviendo order.getStatus().name()
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime orderDate,
        BigDecimal total,
        List<OrderLineResponseDTO> lines
) {}