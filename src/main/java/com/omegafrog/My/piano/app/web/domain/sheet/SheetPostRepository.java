package com.omegafrog.My.piano.app.web.domain.sheet;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SheetPostRepository extends JpaRepository<SheetPost, Long> {

    SheetPost save(SheetPost sheetPost);

    Optional<SheetPost> findById(Long id);

    void deleteById(Long id);

}
