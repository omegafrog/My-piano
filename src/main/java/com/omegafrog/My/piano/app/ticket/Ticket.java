package com.omegafrog.My.piano.app.ticket;

import com.omegafrog.My.piano.app.enums.TicketType;
import com.omegafrog.My.piano.app.user.entity.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Long id;

    private LocalDateTime createdAt;

    @OneToOne(cascade = {CascadeType.MERGE,CascadeType.PERSIST})
    @JoinColumn(name = "USER_ID")
    private User author;
    private TicketType type;
    private String content;
    private LocalDateTime closedAt;

    @Builder
    public Ticket(User author, TicketType type, String content) {
        this.createdAt = LocalDateTime.now();
        this.author = author;
        this.type = type;
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Ticket ticket = (Ticket) o;

        return id.equals(ticket.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
