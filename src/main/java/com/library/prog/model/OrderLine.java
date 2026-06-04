package com.library.prog.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.*;

@Entity
@Table(
    name = "order_line",
    indexes = {
      @Index(name = "idx_order_line_order", columnList = "order_id"),
      @Index(name = "idx_order_line_copy", columnList = "copy_id")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderLine {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "order_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_order_line_order"))
  private Order order;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "copy_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_order_line_copy"))
  private Copy copy;

  @Column(name = "quantity", nullable = false)
  @Builder.Default
  private Integer quantity = 1;

  @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
  @Builder.Default
  private BigDecimal unitPrice = BigDecimal.ZERO;

  @Column(name = "discount", nullable = false, precision = 12, scale = 2)
  @Builder.Default
  private BigDecimal discount = BigDecimal.ZERO;
}
