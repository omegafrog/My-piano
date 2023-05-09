package com.omegafrog.My.piano.app.ticket.entity.dto;

import com.omegafrog.My.piano.app.enums.TicketType;
import com.omegafrog.My.piano.app.user.entity.User;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TicketDto {
    private Long id;
    private LocalDateTime createdAt;
    private User author;
    private TicketType type;
    private String content;
    private LocalDateTime closedAt;

    @Builder
    public TicketDto(Long id, LocalDateTime createdAt, User author, TicketType type, String content, LocalDateTime closedAt) {
        this.id = id;
        this.createdAt = createdAt;
        this.author = author;
        this.type = type;
        this.content = content;
        this.closedAt = closedAt;
    }
}
