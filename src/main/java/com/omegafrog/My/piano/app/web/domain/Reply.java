package com.omegafrog.My.piano.app.web.domain;

import com.omegafrog.My.piano.app.web.domain.ticket.Ticket;
import com.omegafrog.My.piano.app.web.domain.user.authorities.Role;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class Reply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Ticket parent;

    private String content;
    private Long authorId;
    private String authorName;
    private Role authorRole;
    private LocalDateTime createdAt;

    @Builder
    public Reply(Ticket parent, String content, Long authorId, String authorName, Role authorRole) {
        this.parent = parent;
        this.content = content;
        this.authorId = authorId;
        this.authorRole = authorRole;
        this.authorName = authorName;
        this.createdAt = LocalDateTime.now();
    }

}
