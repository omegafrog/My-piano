package com.omegafrog.My.piano.app.ticket.entity;

import com.omegafrog.My.piano.app.ticket.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    Ticket save(Ticket ticket);

    Optional<Ticket> findById(Long id);

    void deleteById(Long id);
}
