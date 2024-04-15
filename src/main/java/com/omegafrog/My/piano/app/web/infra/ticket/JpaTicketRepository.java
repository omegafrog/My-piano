package com.omegafrog.My.piano.app.web.infra.ticket;

import com.omegafrog.My.piano.app.web.domain.ticket.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaTicketRepository extends JpaRepository<Ticket, Long> {
}
