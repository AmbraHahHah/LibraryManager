package com.library.prog.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class BookAuthorId implements Serializable {

    @Column(name = "book_id", nullable = false)
    private UUID bookId;

    @Column(name = "author_id", nullable = false)
    private UUID authorId;
}