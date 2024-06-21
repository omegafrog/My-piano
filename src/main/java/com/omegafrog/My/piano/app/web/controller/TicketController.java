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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
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

    @ExceptionHandler(IllegalArgumentException.class)
    public JsonAPISuccessResponse<Void> handleIllegalArgumentException(IllegalArgumentException ex) {
        return new APIBadRequestSuccessResponse(ex.getMessage());
    }

    @GetMapping("")
    public JsonAPISuccessResponse<Map<String, Object>> getTickets(
            @RequestParam @Nullable Long id,
            @RequestParam @Nullable TicketType type,
            @RequestParam @Nullable TicketStatus status,
            @RequestParam @Nullable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @Nullable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate ,
            Pageable pageable) throws JsonProcessingException {
        SearchTicketFilter filter = new SearchTicketFilter(id, type, status, startDate, endDate);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        SecurityUser userDetails = (SecurityUser) auth.getPrincipal();
        Map<String, Object> data = new HashMap<>();
        List<TicketDto> tickets = ticketService.getTickets(userDetails, filter, pageable);
        data.put("count", tickets.size());
        data.put("data", tickets);
        return new ApiSuccessResponse<>("Get tickets success.", data);

    }

    @PutMapping("")
    public JsonAPISuccessResponse<TicketDto> createTicket(@RequestBody RequestTicketDto dto ) throws JsonProcessingException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        TicketDto data = ticketService.createTicket(dto,  loggedInUser);
        return new ApiSuccessResponse<>("Create ticket success.", data);
    }

    @DeleteMapping("{id}")
    public JsonAPISuccessResponse<Void> closeTicket(@PathVariable Long id){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userdetails = (UserDetails) authentication.getPrincipal();
        ticketService.closeTicket(id, userdetails);
        return new ApiSuccessResponse<>("Close ticket success.");
    }

    @PutMapping("{id}")
    public JsonAPISuccessResponse<ReplyDto> replyTo(@PathVariable Long id, @RequestBody RequestTicketDto dto) throws JsonProcessingException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetails principal =  (UserDetails) auth.getPrincipal();
        ReplyDto data = ticketService.replyTo(id, dto, principal);
        return new ApiSuccessResponse<>("Reply to ticket success.", data);
    }

    @GetMapping("{id}")
    public JsonAPISuccessResponse<TicketDto> getTicket(@PathVariable Long id) throws JsonProcessingException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetails principal = (UserDetails) auth.getPrincipal();
        TicketDto data = ticketService.getTicket(id,principal);
        return new ApiSuccessResponse<>("Get ticket success.", data);
    }
}
