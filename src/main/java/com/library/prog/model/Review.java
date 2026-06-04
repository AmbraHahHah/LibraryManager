package com.library.prog.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(
    name = "review",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_review_book_client",
            columnNames = {"book_id", "client_id"}),
    indexes = {
      @Index(name = "idx_review_book", columnList = "book_id"),
      @Index(name = "idx_review_client", columnList = "client_id")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "book_id", nullable = false, foreignKey = @ForeignKey(name = "fk_review_book"))
  private Book book;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "client_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_review_client"))
  private Client client;

  @Column(name = "rating", nullable = false)
  private Integer rating;

  @Column(name = "comment", columnDefinition = "TEXT")
  private String comment;

  @Column(name = "valid", nullable = false)
  @Builder.Default
  private Boolean valid = false;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;
}
