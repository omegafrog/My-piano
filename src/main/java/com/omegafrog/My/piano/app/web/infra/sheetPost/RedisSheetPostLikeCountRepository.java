package com.omegafrog.My.piano.app.web.infra.sheetPost;


import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostLikeCount;
import org.springframework.data.repository.CrudRepository;

public interface RedisSheetPostLikeCountRepository extends CrudRepository<SheetPostLikeCount, Long> {
}
