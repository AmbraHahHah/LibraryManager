package com.library.prog.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.UUID;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class BookCategoryId implements Serializable {

  @Column(name = "book_id", nullable = false)
  private UUID bookId;

  @Column(name = "category_id", nullable = false)
  private UUID categoryId;
}
