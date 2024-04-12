package com.omegafrog.My.piano.app.web.dto.ticket;

import com.omegafrog.My.piano.app.web.enums.TicketType;

public record RequestTicketDto(String title, String content, TicketType type) {
}
