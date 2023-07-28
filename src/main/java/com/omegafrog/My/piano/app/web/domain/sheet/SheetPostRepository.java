package com.omegafrog.My.piano.app.web.domain.sheet;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface SheetPostRepository  {

    SheetPost save(SheetPost sheetPost);

    Optional<SheetPost> findById(Long id);

    Optional<SheetPost> findBySheetId(Long sheetId);

    Page<SheetPost> findAll(Pageable pageable);

    void deleteById(Long id);

    void deleteAll();

    Long count();

}
