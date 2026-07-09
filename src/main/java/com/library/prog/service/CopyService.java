package com.library.prog.service;

import com.library.prog.dto.request.CopyRequest;
import com.library.prog.dto.response.CopyResponse;
import com.library.prog.model.Copy;
import com.library.prog.repository.BookRepository;
import com.library.prog.repository.CopyRepository;
import com.library.prog.repository.EditorRepository;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CopyService {

  private final CopyRepository copyRepository;
  private final BookRepository bookRepository;
  private final EditorRepository editorRepository;

  public List<CopyResponse> findAll() {
    return copyRepository.findAll().stream().map(this::toResponse).toList();
  }

  public List<CopyResponse> findByBookId(UUID bookId) {
    return copyRepository.findByBookId(bookId).stream().map(this::toResponse).toList();
  }

  public CopyResponse findById(UUID id) {
    return copyRepository
        .findById(id)
        .map(this::toResponse)
        .orElseThrow(() -> new EntityNotFoundException("Copy not found: " + id));
  }

  public CopyResponse create(CopyRequest request) {
    var book =
        bookRepository
            .findById(request.bookId())
            .orElseThrow(() -> new EntityNotFoundException("Book not found: " + request.bookId()));

    var publisher =
        request.publisherId() != null
            ? editorRepository
                .findById(request.publisherId())
                .orElseThrow(
                    () -> new EntityNotFoundException("Editor not found: " + request.publisherId()))
            : null;

    var copy =
        Copy.builder()
            .isbn(request.isbn())
            .format(request.format())
            .price(request.price() != null ? request.price() : BigDecimal.ZERO)
            .pageCount(request.pageCount())
            .publicationDate(request.publicationDate())
            .imageUrl(request.imageUrl())
            .book(book)
            .publisher(publisher)
            .build();
    return toResponse(copyRepository.save(copy));
  }

  public CopyResponse update(UUID id, CopyRequest request) {
    var copy =
        copyRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Copy not found: " + id));

    var book =
        bookRepository
            .findById(request.bookId())
            .orElseThrow(() -> new EntityNotFoundException("Book not found: " + request.bookId()));

    var publisher =
        request.publisherId() != null
            ? editorRepository
                .findById(request.publisherId())
                .orElseThrow(
                    () -> new EntityNotFoundException("Editor not found: " + request.publisherId()))
            : null;

    copy.setIsbn(request.isbn());
    copy.setFormat(request.format());
    copy.setPrice(request.price() != null ? request.price() : BigDecimal.ZERO);
    copy.setPageCount(request.pageCount());
    copy.setPublicationDate(request.publicationDate());
    copy.setImageUrl(request.imageUrl());
    copy.setBook(book);
    copy.setPublisher(publisher);
    return toResponse(copyRepository.save(copy));
  }

  public void delete(UUID id) {
    if (!copyRepository.existsById(id)) {
      throw new EntityNotFoundException("Copy not found: " + id);
    }
    copyRepository.deleteById(id);
  }

  private CopyResponse toResponse(Copy copy) {
    return CopyResponse.builder()
        .id(copy.getId())
        .isbn(copy.getIsbn())
        .format(copy.getFormat())
        .price(copy.getPrice())
        .pageCount(copy.getPageCount())
        .publicationDate(copy.getPublicationDate())
        .imageUrl(copy.getImageUrl())
        .updatedAt(copy.getUpdatedAt())
        .bookId(copy.getBook().getId())
        .publisherId(copy.getPublisher() != null ? copy.getPublisher().getId() : null)
        .build();
  }
}
