package com.omegafrog.My.piano.app.web.dto.post;

import java.util.List;

public record ReturnPostListDto(Long totalPostCount, List<PostListDto> postListDtos) {
}
