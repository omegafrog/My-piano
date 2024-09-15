package com.omegafrog.My.piano.app.web.infra.sheetPost;

import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostViewCount;
import org.springframework.data.repository.CrudRepository;

public interface RedisSheetPostViewCountRepository extends CrudRepository<SheetPostViewCount, Long> {
}
