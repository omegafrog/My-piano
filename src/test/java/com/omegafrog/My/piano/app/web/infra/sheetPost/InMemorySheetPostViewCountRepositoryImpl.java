package com.omegafrog.My.piano.app.web.infra.sheetPost;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import com.omegafrog.My.piano.app.TestResettable;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostViewCount;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostViewCountRepository;

@Repository
@Profile("test")
public class InMemorySheetPostViewCountRepositoryImpl implements SheetPostViewCountRepository, TestResettable {

    private final Map<Long, Integer> counts = new ConcurrentHashMap<>();

    @Override
    public int incrementViewCount(SheetPost sheetPost) {
        return counts.merge(sheetPost.getId(), sheetPost.getViewCount() + 1, (prev, init) -> prev + 1);
    }

    @Override
    public SheetPostViewCount findById(Long id) {
        Integer count = counts.get(id);
        if (count == null) {
            throw new IllegalArgumentException("Cannot find sheet post view count entity : " + id);
        }
        return SheetPostViewCount.builder().id(id).viewCount(count).build();
    }

    @Override
    public boolean exist(Long id) {
        return counts.containsKey(id);
    }

    @Override
    public SheetPostViewCount save(SheetPostViewCount sheetPostViewCount) {
        counts.put(sheetPostViewCount.getId(), sheetPostViewCount.getViewCount());
        return sheetPostViewCount;
    }

    @Override
    public Map<Long, Integer> getViewCountsByIds(List<Long> ids) {
        Map<Long, Integer> viewCounts = new HashMap<>();
        for (Long id : ids) {
            viewCounts.put(id, counts.getOrDefault(id, 0));
        }
        return viewCounts;
    }

    @Override
    public void reset() {
        counts.clear();
    }
}
