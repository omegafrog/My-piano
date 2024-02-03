package com.omegafrog.My.piano.app.web.service.ranking;

import com.omegafrog.My.piano.app.external.elasticsearch.ElasticSearchInstance;
import com.omegafrog.My.piano.app.external.elasticsearch.SheetPostIndex;
import com.omegafrog.My.piano.app.web.domain.ranking.PopularRankingItem;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostRepository;
import com.omegafrog.My.piano.app.web.enums.DateRangeType;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

@Service
public class RankingApiApplicationService {

    @Autowired
    private DateRangeFactory dateRangeFactory;

    @Autowired
    private SheetPostRepository sheetPostRepository;

    @Autowired
    private ElasticSearchInstance elasticInstance;

    public List<PopularRankingItem> getPopularSheetPost(DateRangeType range, String limit) throws IOException, TimeoutException {

        DateRange dateRange = dateRangeFactory.calcDateRange(range);

        List<SheetPostIndex> sheetPostIndices = elasticInstance.searchPopularDateRangeSheetPost(dateRange, limit);

        return sheetPostIndices.stream().map(index -> {
            SheetPost sheetPost = sheetPostRepository.findById(index.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Cannot find Sheet post entity. id : " + index.getId()));
            return new PopularRankingItem(sheetPost);
        }).toList();

    }
}
