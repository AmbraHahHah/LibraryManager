package com.library.prog.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "book_category")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookCategory {

  @EmbeddedId private BookCategoryId id;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("bookId")
  @JoinColumn(name = "book_id", foreignKey = @ForeignKey(name = "fk_book_category_book"))
  private Book book;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("categoryId")
  @JoinColumn(name = "category_id", foreignKey = @ForeignKey(name = "fk_book_category_category"))
  private Category category;
}
