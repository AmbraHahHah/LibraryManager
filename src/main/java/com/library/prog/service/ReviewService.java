package com.library.prog.service;

import com.library.prog.dto.request.ReviewRequest;
import com.library.prog.dto.response.ReviewResponse;
import com.library.prog.model.Review;
import com.library.prog.repository.BookRepository;
import com.library.prog.repository.ClientRepository;
import com.library.prog.repository.ReviewRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

  private final ReviewRepository reviewRepository;
  private final BookRepository bookRepository;
  private final ClientRepository clientRepository;

  @Transactional(readOnly = true)
  public List<ReviewResponse> findAll() {
    return reviewRepository.findAll().stream().map(this::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public ReviewResponse findById(UUID id) {
    return reviewRepository
        .findById(id)
        .map(this::toResponse)
        .orElseThrow(() -> new EntityNotFoundException("Review not found: " + id));
  }

  @Transactional
  public ReviewResponse create(ReviewRequest request) {
    var book =
        bookRepository
            .findById(request.bookId())
            .orElseThrow(() -> new EntityNotFoundException("Book not found: " + request.bookId()));
    var client =
        clientRepository
            .findById(request.clientId())
            .orElseThrow(
                () -> new EntityNotFoundException("Client not found: " + request.clientId()));

    var review =
        Review.builder()
            .book(book)
            .client(client)
            .rating(request.rating())
            .comment(request.comment())
            .build();
    return toResponse(reviewRepository.save(review));
  }

  @Transactional
  public ReviewResponse update(UUID id, ReviewRequest request) {
    var review =
        reviewRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Review not found: " + id));

    var book =
        bookRepository
            .findById(request.bookId())
            .orElseThrow(() -> new EntityNotFoundException("Book not found: " + request.bookId()));
    var client =
        clientRepository
            .findById(request.clientId())
            .orElseThrow(
                () -> new EntityNotFoundException("Client not found: " + request.clientId()));

    review.setBook(book);
    review.setClient(client);
    review.setRating(request.rating());
    review.setComment(request.comment());
    return toResponse(reviewRepository.save(review));
  }

  @Transactional
  public void delete(UUID id) {
    if (!reviewRepository.existsById(id)) {
      throw new EntityNotFoundException("Review not found: " + id);
    }
    reviewRepository.deleteById(id);
  }

  private ReviewResponse toResponse(Review review) {
    return ReviewResponse.builder()
        .id(review.getId())
        .bookId(review.getBook().getId())
        .clientId(review.getClient().getId())
        .rating(review.getRating())
        .comment(review.getComment())
        .valid(review.getValid())
        .createdAt(review.getCreatedAt())
        .build();
  }
}
