package com.omegafrog.My.piano.app.web.domain.sheet;

import com.omegafrog.My.piano.app.web.dto.sheetPost.SearchSheetPostFilter;
import com.omegafrog.My.piano.app.web.dto.sheetPost.SheetPostDto;
import com.omegafrog.My.piano.app.web.dto.sheetPost.SheetPostListDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface SheetPostRepository {

    SheetPost save(SheetPost sheetPost);

    Optional<SheetPost> findById(Long id);

    Optional<SheetPost> findBySheetId(Long sheetId);

    Page<SheetPost> findAll(Pageable pageable);

    Page<SheetPostDto> findAll(Pageable pageable, SearchSheetPostFilter filter);


    void deleteById(Long id);

    void deleteAll();

    Long count();

    List<SheetPostListDto> findByIds(List<Long> sheetPostIds, Pageable pageable);

    List<SheetPost> findAllById(List<Long> list);
}
