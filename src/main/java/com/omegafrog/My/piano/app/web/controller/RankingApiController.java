package com.omegafrog.My.piano.app.web.controller;

import com.omegafrog.My.piano.app.utils.response.APISuccessResponse;
import com.omegafrog.My.piano.app.utils.response.JsonAPIResponse;
import com.omegafrog.My.piano.app.utils.response.ResponseUtil;
import com.omegafrog.My.piano.app.web.domain.ranking.PopularRankingItem;
import com.omegafrog.My.piano.app.web.enums.DateRangeType;
import com.omegafrog.My.piano.app.web.service.ranking.RankingApiApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@RestController
@RequestMapping("/api/v1")
public class RankingApiController {

    @Autowired
    private RankingApiApplicationService rankingService;

    @GetMapping("popular")
    public JsonAPIResponse getPopularSheetPost(@RequestParam DateRangeType range, @RequestParam String limit)
            throws IOException, TimeoutException {
        List<PopularRankingItem> popularSheetPost = rankingService.getPopularSheetPost(range, limit);
        Map<String, Object> data = ResponseUtil.getStringObjectMap("popular", popularSheetPost);
        return new APISuccessResponse("Get popular sheet post success.", data);
    }

}
