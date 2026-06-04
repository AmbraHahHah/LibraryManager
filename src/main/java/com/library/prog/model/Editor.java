package com.library.prog.model;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "editor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Editor {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "country", length = 100)
    private String country;


    @OneToMany(mappedBy = "publisher", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Copy> copies = new ArrayList<>();
}