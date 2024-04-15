package com.omegafrog.My.piano.app.web.domain.ticket;

import com.omegafrog.My.piano.app.web.dto.SearchTicketFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface TicketRepository {
    Ticket save(Ticket ticket);

    Optional<Ticket> findById(Long id);

    void deleteById(Long id);

    Page<Ticket> findAll(SearchTicketFilter filter, Pageable pageable);

    List<Ticket> findByAuthor_IdAndFilter(Long id, SearchTicketFilter filter,Pageable pageable);

    Long count();
}
