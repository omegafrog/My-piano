package com.omegafrog.My.piano.app.web.domain.ticket;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    Ticket save(Ticket ticket);

    Optional<Ticket> findById(Long id);

    void deleteById(Long id);
}
