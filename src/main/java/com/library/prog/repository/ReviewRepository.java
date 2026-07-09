package com.library.prog.repository;

import com.library.prog.model.Review;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

  List<Review> findByBookId(UUID bookId);

  List<Review> findByClientId(UUID clientId);
}
