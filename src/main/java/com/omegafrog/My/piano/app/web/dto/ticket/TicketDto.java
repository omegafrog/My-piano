package com.omegafrog.My.piano.app.web.dto.ticket;

import com.omegafrog.My.piano.app.web.domain.ticket.Ticket;
import com.omegafrog.My.piano.app.web.domain.ticket.TicketStatus;
import com.omegafrog.My.piano.app.web.dto.ReplyDto;
import com.omegafrog.My.piano.app.web.dto.user.UserProfileDto;
import com.omegafrog.My.piano.app.web.enums.TicketType;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.List;

public record TicketDto(Long id, LocalDateTime createdAt, UserProfileDto author,
                        TicketType type, String content, LocalDateTime closedAt, TicketStatus status,
                        List<ReplyDto> reply) {
    public TicketDto(Ticket ticket){
        this(ticket.getId(), ticket.getCreatedAt(),
                new UserProfileDto(
                        ticket.getAuthor().getUserProfile().getUsername(),
                        ticket.getAuthor().getUserProfile().getProfileSrc()),
                ticket.getType(), ticket.getContent(),
                ticket.getClosedAt(), ticket.getStatus(),
                ticket.getReply().stream().map(r-> new ReplyDto(r.getContent(), r.getAuthorName(), r.getCreatedAt())
                ).toList());
    }

}



