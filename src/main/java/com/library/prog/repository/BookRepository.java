package com.library.prog.repository;

import com.library.prog.model.Book;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends JpaRepository<Book, UUID> {

  @Query(
      """
      SELECT DISTINCT b FROM Book b
      LEFT JOIN b.bookAuthors ba
      LEFT JOIN ba.author a
      LEFT JOIN b.bookCategories bc
      LEFT JOIN bc.category c
      WHERE (:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%')))
      AND (:author IS NULL OR LOWER(a.firstName) LIKE LOWER(CONCAT('%', :author, '%'))
        OR LOWER(a.lastName) LIKE LOWER(CONCAT('%', :author, '%')))
      AND (:category IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :category, '%')))
      """)
  List<Book> search(
      @Param("title") String title,
      @Param("author") String author,
      @Param("category") String category);

  @Query("SELECT b FROM Book b JOIN b.bookAuthors ba WHERE ba.author.id = :authorId")
  List<Book> findByAuthorId(@Param("authorId") UUID authorId);

  @Query("SELECT b FROM Book b JOIN b.bookCategories bc WHERE bc.category.id = :categoryId")
  List<Book> findByCategoryId(@Param("categoryId") UUID categoryId);
}
