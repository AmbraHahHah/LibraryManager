package com.library.prog.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "book_author")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookAuthor {

  @EmbeddedId private BookAuthorId id;

  @Column(name = "role", length = 50)
  @Builder.Default
  private String role = "Author";

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("bookId")
  @JoinColumn(name = "book_id", foreignKey = @ForeignKey(name = "fk_book_author_book"))
  private Book book;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("authorId")
  @JoinColumn(name = "author_id", foreignKey = @ForeignKey(name = "fk_book_author_author"))
  private Author author;
}
