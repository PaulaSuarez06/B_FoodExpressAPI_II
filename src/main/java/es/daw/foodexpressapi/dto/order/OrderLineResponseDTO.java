package es.daw.foodexpressapi.dto.order;

import java.math.BigDecimal;

public record OrderLineResponseDTO(
        Long dishId,
        String dishName,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal subtotal
) {
}
