package es.daw.foodexpressapi.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "order_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetail {

//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;

    @EmbeddedId
    private OrderDetailId orderDetailId;     //---- SE REFIERE A LAS DOS CLAVES PRIMARIAS DE LA TABLA ORDER DETAILS, ESTA EN ORDERDETAULID

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("orderId")
    @JoinColumn(name = "order_id")  // HAY UNA FOREING KEY EN LA TABLA ORDER DETAILS
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY) //NUCHOS DETALLE DE PEDIDO ESTAN EN UN PLATO
    @MapsId("dishId")
    @JoinColumn(name = "dish_id") // HAY UNA FOREING KEY EN LA TABLA ORDER DETAILS
    private Dish dish;

    private Integer quantity;

    private BigDecimal subtotal;


}
