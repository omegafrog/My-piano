package com.omegafrog.My.piano.app.web.service;

import com.omegafrog.My.piano.app.security.entity.SecurityUser;
import com.omegafrog.My.piano.app.security.entity.SecurityUserRepository;
import com.omegafrog.My.piano.app.security.entity.authorities.Role;
import com.omegafrog.My.piano.app.utils.AuthenticationUtil;
import com.omegafrog.My.piano.app.web.domain.Reply;
import com.omegafrog.My.piano.app.web.domain.admin.Admin;
import com.omegafrog.My.piano.app.web.domain.ticket.Ticket;
import com.omegafrog.My.piano.app.web.domain.ticket.TicketRepository;
import com.omegafrog.My.piano.app.web.domain.ticket.TicketStatus;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.dto.comment.ReplyDto;
import com.omegafrog.My.piano.app.web.dto.ticket.SearchTicketFilter;
import com.omegafrog.My.piano.app.web.dto.ticket.RequestTicketDto;
import com.omegafrog.My.piano.app.web.dto.ticket.TicketDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final SecurityUserRepository securityUserRepository;
    private final AuthenticationUtil authenticationUtil;


    public Page<TicketDto> getTickets(SearchTicketFilter filter, Pageable pageable) {
        SecurityUser userDetails= authenticationUtil.getLoggedInUser().getSecurityUser();

        if(userDetails.getRole().equals(Role.USER) || userDetails.getRole().equals(Role.CREATOR))
            return getUserTickets(filter, pageable, userDetails);

        else if(userDetails.getRole().equals(Role.ADMIN) || userDetails.getRole().equals(Role.SUPER_ADMIN))
            return getAllTickets(filter, pageable);

        else throw new IllegalArgumentException("Invalid userDetails:" + userDetails.getId());
    }

    private Page<TicketDto> getAllTickets(SearchTicketFilter filter, Pageable pageable) {
        return ticketRepository.findAll(filter, pageable).map(TicketDto::new);
    }

    private Page<TicketDto> getUserTickets(SearchTicketFilter filter, Pageable pageable, SecurityUser userDetails) {
        securityUserRepository.findById(userDetails.getUser().getId());
        return ticketRepository.findByAuthor_IdAndFilter(userDetails.getUser().getId(), filter, pageable)
                .map(TicketDto::new);
    }

    public TicketDto createTicket(RequestTicketDto dto) {
        User loggedInUser = authenticationUtil.getLoggedInUser();
        Ticket ticket = new Ticket(loggedInUser, dto.type(),dto.title(), dto.content());
        return new TicketDto(ticketRepository.save(ticket));
    }

    public ReplyDto replyTo(Long id, RequestTicketDto dto) {
        User loggedInUser = authenticationUtil.getLoggedInUser();
        Ticket ticket = ticketRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Cannot find ticket entity."));

        ticket.changeStatus(TicketStatus.PRODUCING);
        Reply reply= Reply.builder()
                .parent(ticket)
                .content(dto.content())
                .authorRole(loggedInUser.getSecurityUser().getRole())
                .authorId(loggedInUser.getId())
                .authorName(loggedInUser.getSecurityUser().getUsername())
                .build();
        ticket.addReply(reply);
        return new ReplyDto(reply);
    }

    public TicketDto getTicket(Long id){
        SecurityUser loggedInUser = authenticationUtil.getLoggedInUser().getSecurityUser();
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find ticket entity."));
        if(!loggedInUser.getUser().equals(ticket.getAuthor()))
            throw new IllegalArgumentException("Cannot get other user's ticket");
        return new TicketDto(ticket);
    }

    public void closeTicket(Long id){
        User loggedInUser = authenticationUtil.getLoggedInUser();
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find ticket entity."));
        if(!ticket.getAuthor().equals(loggedInUser))
                throw new IllegalArgumentException("Cannot close other user's ticket");

        if(ticket.getStatus().equals(TicketStatus.CLOSED)||ticket.getStatus().equals( TicketStatus.FINISHED))
            throw new IllegalArgumentException("Already closed ticket.");
        ticket.close();
    }

    public Long countTickets() {
        return Long.valueOf(ticketRepository.count());
    }
}
