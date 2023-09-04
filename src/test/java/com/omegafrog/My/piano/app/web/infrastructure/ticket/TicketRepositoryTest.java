package com.omegafrog.My.piano.app.web.infrastructure.ticket;

import com.omegafrog.My.piano.app.web.domain.cart.Cart;
import com.omegafrog.My.piano.app.web.domain.user.UserRepository;
import com.omegafrog.My.piano.app.web.enums.TicketType;
import com.omegafrog.My.piano.app.web.domain.ticket.Ticket;
import com.omegafrog.My.piano.app.web.domain.ticket.TicketRepository;
import com.omegafrog.My.piano.app.web.dto.ticket.UpdateTicketDto;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.infra.user.JpaUserRepositoryImpl;
import com.omegafrog.My.piano.app.web.infra.user.SimpleJpaUserRepository;
import com.omegafrog.My.piano.app.web.vo.user.LoginMethod;
import com.omegafrog.My.piano.app.web.vo.user.PhoneNum;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;


@DataJpaTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TicketRepositoryTest {

    @Autowired
    private TicketRepository ticketRepository;

    private User user1;
    @Autowired
    private SimpleJpaUserRepository jpaUserRepository;

    private UserRepository userRepository;

    @BeforeAll
    void settings(){
    userRepository = new JpaUserRepositoryImpl(jpaUserRepository);
        user1 = userRepository.save(User.builder()
                .name("user1")
                .profileSrc("src1")
                .phoneNum(PhoneNum.builder()
                        .phoneNum("010-1111-2222")
                        .build())
                .loginMethod(LoginMethod.EMAIL)
                .email("user1@gmail.com")
                .cart(new Cart())
                .build());
    }

    @AfterEach
    void clearRepository(){
        ticketRepository.deleteAll();
    }

    @AfterAll
    void clearAllRepository(){
        userRepository.deleteAll();
    }

    @Test
    void saveNFindTest() {
        Ticket ticket = Ticket.builder()
                .author(user1)
                .type(TicketType.TYPE_LESSON)
                .content("hihi")
                .build();

        Ticket saved = ticketRepository.save(ticket);
        Optional<Ticket> founded = ticketRepository.findById(saved.getId());
        Assertions.assertThat(founded).isPresent();
        Assertions.assertThat(founded.get()).isEqualTo(saved);
    }

    @Test
    void updateTest() {
        Ticket ticket = Ticket.builder()
                .author(user1)
                .type(TicketType.TYPE_LESSON)
                .content("hihi")
                .build();

        Ticket saved = ticketRepository.save(ticket);
        String changedContent = "changed";
        UpdateTicketDto updated = UpdateTicketDto.builder()
                .type(TicketType.TYPE_SHEET)
                .content(changedContent)
                .build();
        Ticket updatedTicket = saved.update(updated);
        Ticket updatedTicketEntity = ticketRepository.save(updatedTicket);
        Assertions.assertThat(updatedTicketEntity).isEqualTo(saved);
        Assertions.assertThat(updatedTicketEntity.toDto().getContent())
                .isEqualTo(changedContent);
    }
}