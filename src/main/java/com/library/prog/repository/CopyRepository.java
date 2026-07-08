package com.library.prog.repository;

import com.library.prog.model.Copy;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CopyRepository extends JpaRepository<Copy, UUID> {
  List<Copy> findByBookId(UUID bookId);
}
