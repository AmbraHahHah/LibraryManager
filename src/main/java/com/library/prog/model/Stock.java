package com.library.prog.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "stock")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stock {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "available_quantity", nullable = false)
  @Builder.Default
  private Integer availableQuantity = 0;

  @Column(name = "reserved_quantity", nullable = false)
  @Builder.Default
  private Integer reservedQuantity = 0;

  @Column(name = "alert_threshold", nullable = false)
  @Builder.Default
  private Integer alertThreshold = 5;

  @UpdateTimestamp
  @Column(name = "last_updated")
  private Instant lastUpdated;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "copy_id",
      nullable = false,
      unique = true,
      foreignKey = @ForeignKey(name = "fk_stock_copy"))
  private Copy copy;

  public int getAvailableStock() {
    return availableQuantity - reservedQuantity;
  }

  public boolean isOutOfStock() {
    return getAvailableStock() <= 0;
  }

  public boolean isLowStock() {
    return getAvailableStock() <= alertThreshold;
  }
}
