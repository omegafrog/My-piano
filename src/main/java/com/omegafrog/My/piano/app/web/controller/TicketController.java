package com.omegafrog.My.piano.app.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.omegafrog.My.piano.app.security.entity.SecurityUser;
import com.omegafrog.My.piano.app.utils.AuthenticationUtil;
import com.omegafrog.My.piano.app.web.response.APIBadRequestSuccessResponse;
import com.omegafrog.My.piano.app.web.response.success.ApiSuccessResponse;
import com.omegafrog.My.piano.app.web.response.success.JsonAPISuccessResponse;
import com.omegafrog.My.piano.app.web.domain.ticket.TicketStatus;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.dto.comment.ReplyDto;
import com.omegafrog.My.piano.app.web.dto.ticket.SearchTicketFilter;
import com.omegafrog.My.piano.app.web.dto.ticket.RequestTicketDto;
import com.omegafrog.My.piano.app.web.dto.ticket.TicketDto;
import com.omegafrog.My.piano.app.web.enums.TicketType;
import com.omegafrog.My.piano.app.web.service.TicketService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class TicketController {


    private final TicketService ticketService;

    @GetMapping("")
    public JsonAPISuccessResponse getTickets(
            @Valid @RequestParam @Nullable Long id,
            @Valid @RequestParam @Nullable TicketType type,
            @Valid @RequestParam @Nullable TicketStatus status,
            @Valid @RequestParam @Nullable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Valid @RequestParam @Nullable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate ,
            @PageableDefault(page = 0,size = 30) Pageable pageable) {
        SearchTicketFilter filter = new SearchTicketFilter(id, type, status, startDate, endDate);
        Page<TicketDto> tickets = ticketService.getTickets( filter, pageable);
        return new ApiSuccessResponse<>("Get tickets success.", tickets);

    }

    @PutMapping("")
    public JsonAPISuccessResponse<TicketDto> createTicket(
            @Valid @NotNull @RequestBody RequestTicketDto dto ) {
        TicketDto data = ticketService.createTicket(dto);
        return new ApiSuccessResponse<>("Create ticket success.", data);
    }

    @DeleteMapping("{id}")
    public JsonAPISuccessResponse<Void> closeTicket(
            @Valid @NotNull@PathVariable Long id){
        ticketService.closeTicket(id);
        return new ApiSuccessResponse<>("Close ticket success.");
    }

    @PutMapping("{id}")
    public JsonAPISuccessResponse<ReplyDto> replyTo(
            @Valid @NotNull @PathVariable Long id,
            @Valid @NotNull @RequestBody RequestTicketDto dto) {
        ReplyDto data = ticketService.replyTo(id, dto);
        return new ApiSuccessResponse<>("Reply to ticket success.", data);
    }

    @GetMapping("{id}")
    public JsonAPISuccessResponse<TicketDto> getTicket(
            @Valid @NotNull @PathVariable Long id) {
        TicketDto data = ticketService.getTicket(id);
        return new ApiSuccessResponse<>("Get ticket success.", data);
    }
}
