package com.omegafrog.My.piano.app.web.service;

import com.omegafrog.My.piano.app.security.entity.SecurityUser;
import com.omegafrog.My.piano.app.security.entity.SecurityUserRepository;
import com.omegafrog.My.piano.app.security.entity.authorities.Role;
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


    public List<TicketDto> getTickets(SecurityUser userDetails,

                                      SearchTicketFilter filter, Pageable pageable) {
        if(userDetails.getRole().equals(Role.USER) || userDetails.getRole().equals(Role.CREATOR)){
            securityUserRepository.findById(userDetails.getUser().getId());
            List<Ticket> byAuthorId = ticketRepository.findByAuthor_IdAndFilter(userDetails.getUser().getId(), filter, pageable);
            return byAuthorId.stream().map(TicketDto::new).toList();
        }else if(userDetails.getRole().equals(Role.ADMIN) || userDetails.getRole().equals(Role.SUPER_ADMIN)){
            Page<Ticket> all = ticketRepository.findAll(filter, pageable);
            return all.getContent().stream().map(TicketDto::new).toList();
        } else throw new IllegalArgumentException("Invalid userDetails:" + userDetails.getId());
    }

    public TicketDto createTicket(RequestTicketDto dto,  User loggedInUser) {
        Ticket ticket = new Ticket(loggedInUser, dto.type(),dto.title(), dto.content());
        return new TicketDto(ticketRepository.save(ticket));
    }

    public ReplyDto replyTo(Long id, RequestTicketDto dto, UserDetails principal) {
        Ticket ticket = ticketRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Cannot find ticket entity."));
        ticket.changeStatus(TicketStatus.PRODUCING);
        Reply.ReplyBuilder builder = Reply.builder()
                .parent(ticket)
                .content(dto.content());
        if (principal instanceof SecurityUser user) {
            builder.authorRole(Role.USER);
            builder.authorId(user.getUser().getId());
        } else if (principal instanceof Admin admin) {
            builder.authorRole(Role.ADMIN);
            builder.authorId(admin.getId());
        } else throw new IllegalArgumentException("Invalid principal type.");
        builder.authorName(principal.getUsername());
        Reply reply = builder.build();
        ticket.addReply(reply);
        return new ReplyDto(reply);
    }

    public TicketDto getTicket(Long id, UserDetails principal){
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find ticket entity."));
        if( principal instanceof SecurityUser securityUser
                && (!securityUser.getUser().equals(ticket.getAuthor())))
            throw new IllegalArgumentException("Cannot get other user's ticket");
        return new TicketDto(ticket);
    }

    public void closeTicket(Long id, UserDetails userDetails){
        Ticket ticket = ticketRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Cannot find ticket entity."));
        if (userDetails instanceof SecurityUser securityUser){
            if(!ticket.getAuthor().equals(securityUser.getUser()))
                throw new IllegalArgumentException("Cannot close other user's ticket");
        }

        if(ticket.getStatus().equals(TicketStatus.CLOSED)||ticket.getStatus().equals( TicketStatus.FINISHED))
            throw new IllegalArgumentException("Already closed ticket.");
        ticket.close();
    }

    public Long countTickets() {
        return Long.valueOf(ticketRepository.count());
    }
}
