package com.omegafrog.My.piano.app.web.domain.ticket;

import com.omegafrog.My.piano.app.web.domain.Reply;
import com.omegafrog.My.piano.app.web.enums.TicketType;
import com.omegafrog.My.piano.app.web.dto.ticket.UpdateTicketDto;
import com.omegafrog.My.piano.app.web.domain.user.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@Getter
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime createdAt;

    @OneToOne
    @JoinColumn(name = "USER_ID")
    private User author;

    private TicketType type;
    private String content;

    private TicketStatus status;
    private LocalDateTime closedAt;

    @OneToMany(cascade = {CascadeType.PERSIST}, mappedBy = "parent", fetch = FetchType.LAZY)
    private List<Reply> reply = new ArrayList<>();

    @Builder
    public Ticket(User author, TicketType type, String content) {
        this.createdAt = LocalDateTime.now();
        this.author = author;
        this.type = type;
        this.content = content;
        this.status = TicketStatus.CREATED;
    }

    public Ticket update(UpdateTicketDto dto){
        this.type = dto.getType();
        this.content = dto.getContent();
        return this;
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

    public void close(){
        this.status = TicketStatus.CLOSED;
        this.closedAt = LocalDateTime.now();
    }

    public void addReply(Reply reply){
        this.reply.add(reply);
    }

    public void changeStatus(TicketStatus status){
        this.status = status;
    }

}
