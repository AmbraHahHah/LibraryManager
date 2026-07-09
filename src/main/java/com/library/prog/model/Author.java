package com.library.prog.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "author")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Author {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "last_name", nullable = false, length = 100)
  private String lastName;

  @Column(name = "first_name", length = 100)
  private String firstName;

  @Column(name = "biography", columnDefinition = "TEXT")
  private String biography;

  @Column(name = "nationality", length = 100)
  private String nationality;

  @Column(name = "birth_date")
  private LocalDate birthDate;

  @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<BookAuthor> bookAuthors = new ArrayList<>();
}
