package com.omegafrog.My.piano.app.web.infrastructure.ticket;

import com.omegafrog.My.piano.app.DataJpaTestConfig;
import com.omegafrog.My.piano.app.web.domain.cart.Cart;
import com.omegafrog.My.piano.app.web.domain.user.UserRepository;
import com.omegafrog.My.piano.app.web.enums.TicketType;
import com.omegafrog.My.piano.app.web.domain.ticket.Ticket;
import com.omegafrog.My.piano.app.web.domain.ticket.TicketRepository;
import com.omegafrog.My.piano.app.web.dto.ticket.UpdateTicketDto;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.vo.user.LoginMethod;
import com.omegafrog.My.piano.app.web.vo.user.PhoneNum;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.Optional;


@DataJpaTest
@Import(DataJpaTestConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TicketRepositoryTest {

    @Autowired
    private TicketRepository ticketRepository;

    private User user1;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void settings(){
        user1 = userRepository.save(User.builder()
                .name("user1")
                .profileSrc("src1")
                .phoneNum(new PhoneNum("010-1111-2222"))
                .loginMethod(LoginMethod.EMAIL)
                .email("user1@gmail.com")
                .cart(new Cart())
                .build());
    }

    @Test
    void saveNFindTest() {
        //given
        Ticket ticket = Ticket.builder()
                .author(user1)
                .type(TicketType.TYPE_LESSON)
                .content("hihi")
                .build();

        //when
        Ticket saved = ticketRepository.save(ticket);
        //then
        Optional<Ticket> founded = ticketRepository.findById(saved.getId());
        Assertions.assertThat(founded).isPresent();
        Assertions.assertThat(founded).contains(saved);
    }

    @Test
    void updateTest() {
        //given
        Ticket ticket = Ticket.builder()
                .author(user1)
                .type(TicketType.TYPE_LESSON)
                .content("hihi")
                .build();

        Ticket saved = ticketRepository.save(ticket);
        //when
        String changedContent = "changed";
        UpdateTicketDto updated = UpdateTicketDto.builder()
                .type(TicketType.TYPE_SHEET)
                .content(changedContent)
                .build();
        Ticket updatedTicket = saved.update(updated);
        //then
        Optional<Ticket> founded = ticketRepository.findById(saved.getId());
        Assertions.assertThat(founded).isNotEmpty().get().extracting("content").isEqualTo(changedContent);
    }
}