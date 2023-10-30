package com.omegafrog.My.piano.app.web.domain.search;

import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.enums.Genre;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
public class ElasticSearchInstance {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${elasticsearch.host}")
    private String esHost;

    @Value("${elasticsearch.port}")
    private String esPort;
    @Value("${elasticsearch.secret}")
    private String esSecret;

    @Async("ThreadPoolTaskExecutor")
    public void invertIndexingSheetPost(SheetPost sheetPost) {
//        curl -XPUT "https://172.18.0.2:9200/sheets/_doc/1" -H "kbn-xsrf: reporting" -H "Content-Type: application/json" -d'
//        {
//            "name":"동해물과 백두산이"
//        }'
        StringBuilder builder = new StringBuilder();
        String url = builder.append("https://").append(esHost).append(":").append(esPort)
                .append("/sheets/_doc/").append(sheetPost.getId())
                .toString();
        log.info("{}",url);
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("title", sheetPost.getTitle());
        params.add("name", sheetPost.getSheet().getTitle());
        List<Genre> all = sheetPost.getSheet().getGenres().getAll();
        params.add("genre", all.stream().map(Genre::toString).toList());
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Basic "+esSecret );
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(params, headers);
        log.info("request:{}", entity);
        restTemplate.put(url, entity);
    }
}
