package com.omegafrog.My.piano.app.web.dto.ticket;

import com.omegafrog.My.piano.app.web.enums.TicketType;
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
