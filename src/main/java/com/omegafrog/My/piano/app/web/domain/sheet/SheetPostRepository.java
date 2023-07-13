package com.omegafrog.My.piano.app.web.domain.sheet;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SheetPostRepository  {

    SheetPost save(SheetPost sheetPost);

    Optional<SheetPost> findById(Long id);

    Optional<SheetPost> findBySheetId(Long sheetId);

    void deleteById(Long id);

}
