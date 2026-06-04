package com.library.prog.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(
    name = "copy",
    uniqueConstraints = @UniqueConstraint(name = "uk_copy_isbn", columnNames = "isbn"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Copy {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "isbn", nullable = false, length = 20, unique = true)
  private String isbn;

  @Enumerated(EnumType.STRING)
  @Column(name = "format", nullable = false, length = 10)
  private FormatEnum format;

  @Column(name = "price", nullable = false, precision = 10, scale = 2)
  @Builder.Default
  private BigDecimal price = BigDecimal.ZERO;

  @Column(name = "page_count")
  private Integer pageCount;

  @Column(name = "publication_date")
  private LocalDate publicationDate;

  @Column(name = "image_url", columnDefinition = "TEXT")
  private String imageUrl;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private Instant updatedAt;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "book_id", nullable = false, foreignKey = @ForeignKey(name = "fk_copy_book"))
  private Book book;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "publisher_id", foreignKey = @ForeignKey(name = "fk_copy_publisher"))
  private Editor publisher;

  @OneToOne(mappedBy = "copy", cascade = CascadeType.ALL, orphanRemoval = true)
  private Stock stock;

  @OneToMany(mappedBy = "copy", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<StockMovement> stockMovements = new ArrayList<>();

  @OneToMany(mappedBy = "copy", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<OrderLine> orderLines = new ArrayList<>();
}
