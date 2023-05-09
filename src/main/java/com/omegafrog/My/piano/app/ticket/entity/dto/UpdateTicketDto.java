package com.omegafrog.My.piano.app.ticket.entity.dto;

import com.omegafrog.My.piano.app.enums.TicketType;
import lombok.Builder;
import lombok.Data;

@Data
public class UpdateTicketDto {
    private TicketType type;
    private String content;

    @Builder
    public UpdateTicketDto(TicketType type, String content) {
        this.type = type;
        this.content = content;
    }

}
