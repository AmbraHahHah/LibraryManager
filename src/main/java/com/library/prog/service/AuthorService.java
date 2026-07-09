package com.library.prog.service;

import com.library.prog.dto.request.AuthorRequest;
import com.library.prog.dto.response.AuthorResponse;
import com.library.prog.model.Author;
import com.library.prog.repository.AuthorRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthorService {

  private final AuthorRepository authorRepository;

  @Transactional(readOnly = true)
  public List<AuthorResponse> findAll() {
    return authorRepository.findAll().stream().map(this::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public AuthorResponse findById(UUID id) {
    return authorRepository
        .findById(id)
        .map(this::toResponse)
        .orElseThrow(() -> new EntityNotFoundException("Author not found: " + id));
  }

  @Transactional
  public AuthorResponse create(AuthorRequest request) {
    var author =
        Author.builder()
            .lastName(request.lastName())
            .firstName(request.firstName())
            .biography(request.biography())
            .nationality(request.nationality())
            .birthDate(request.birthDate())
            .build();
    return toResponse(authorRepository.save(author));
  }

  @Transactional
  public AuthorResponse update(UUID id, AuthorRequest request) {
    var author =
        authorRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Author not found: " + id));
    author.setLastName(request.lastName());
    author.setFirstName(request.firstName());
    author.setBiography(request.biography());
    author.setNationality(request.nationality());
    author.setBirthDate(request.birthDate());
    return toResponse(authorRepository.save(author));
  }

  @Transactional
  public void delete(UUID id) {
    if (!authorRepository.existsById(id)) {
      throw new EntityNotFoundException("Author not found: " + id);
    }
    authorRepository.deleteById(id);
  }

  private AuthorResponse toResponse(Author author) {
    return AuthorResponse.builder()
        .id(author.getId())
        .lastName(author.getLastName())
        .firstName(author.getFirstName())
        .biography(author.getBiography())
        .nationality(author.getNationality())
        .birthDate(author.getBirthDate())
        .build();
  }
}
