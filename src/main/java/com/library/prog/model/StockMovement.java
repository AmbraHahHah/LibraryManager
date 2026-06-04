package com.library.prog.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Index;

@Entity
@Table(
    name = "stock_movement",
    indexes = {
      @Index(name = "idx_stock_movement_copy", columnList = "copy_id"),
      @Index(name = "idx_stock_movement_order", columnList = "order_id")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockMovement {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "quantity", nullable = false)
  private Integer quantity;

  @Enumerated(EnumType.STRING)
  @Column(name = "movement_type", nullable = false, length = 20)
  private MovementTypeEnum movementType;

  @Column(name = "reason", columnDefinition = "TEXT")
  private String reason;

  @CreationTimestamp
  @Column(name = "movement_date", nullable = false, updatable = false)
  private Instant movementDate;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "copy_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_stock_movement_copy"))
  private Copy copy;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", foreignKey = @ForeignKey(name = "fk_stock_movement_order"))
  private Order order;
}
