package com.omegafrog.My.piano.app.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.omegafrog.My.piano.app.utils.AuthenticationUtil;
import com.omegafrog.My.piano.app.utils.response.APIBadRequestResponse;
import com.omegafrog.My.piano.app.utils.response.APISuccessResponse;
import com.omegafrog.My.piano.app.utils.response.JsonAPIResponse;
import com.omegafrog.My.piano.app.web.domain.admin.Admin;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.dto.ticket.RequestTicketDto;
import com.omegafrog.My.piano.app.web.dto.ticket.TicketDto;
import com.omegafrog.My.piano.app.web.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
public class TicketController {


    private final TicketService ticketService;

    @ExceptionHandler(IllegalArgumentException.class)
    public JsonAPIResponse<Void> handleIllegalArgumentException(IllegalArgumentException ex) {
        return new APIBadRequestResponse(ex.getMessage());
    }

    @GetMapping("")
    public JsonAPIResponse<List<TicketDto>> getTickets( Pageable pageable) throws JsonProcessingException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        List<TicketDto> data = ticketService.getTickets(userDetails, pageable);
        return new APISuccessResponse<>("Get tickets success.", data);

    }

    @PutMapping("")
    public JsonAPIResponse<TicketDto> createTicket(@RequestBody RequestTicketDto dto ) throws JsonProcessingException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        TicketDto data = ticketService.createTicket(dto,  loggedInUser);
        return new APISuccessResponse<>("Create ticket success.", data);
    }

    @DeleteMapping("{id}")
    public JsonAPIResponse<Void> closeTicket(@PathVariable Long id){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userdetails = (UserDetails) authentication.getPrincipal();
        ticketService.closeTicket(id, userdetails);
        return new APISuccessResponse<>("Close ticket success.");
    }

    @PutMapping("{id}")
    public JsonAPIResponse<TicketDto> replyTo(@PathVariable Long id, @RequestBody RequestTicketDto dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetails principal =  (UserDetails) auth.getPrincipal();
        ticketService.replyTo(id, dto, principal);
        return new APISuccessResponse<>("Reply to ticket success.");
    }

    @GetMapping("{id}")
    public JsonAPIResponse<TicketDto> getTicket(@PathVariable Long id) throws JsonProcessingException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetails principal = (UserDetails) auth.getPrincipal();
        TicketDto data = ticketService.getTicket(id,principal);
        return new APISuccessResponse<>("Get ticket success.", data);
    }
}
