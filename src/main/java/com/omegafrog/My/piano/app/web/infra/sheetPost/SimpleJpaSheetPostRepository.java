package com.omegafrog.My.piano.app.web.infra.sheetPost;

import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SimpleJpaSheetPostRepository extends JpaRepository<SheetPost, Long> {

    Optional<SheetPost> findBySheet_id(Long sheetId);
}