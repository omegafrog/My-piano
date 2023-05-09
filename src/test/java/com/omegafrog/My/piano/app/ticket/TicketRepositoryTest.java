package com.omegafrog.My.piano.app.ticket;

import com.omegafrog.My.piano.app.cart.Cart;
import com.omegafrog.My.piano.app.enums.TicketType;
import com.omegafrog.My.piano.app.ticket.entity.Ticket;
import com.omegafrog.My.piano.app.ticket.entity.TicketRepository;
import com.omegafrog.My.piano.app.ticket.entity.dto.UpdateTicketDto;
import com.omegafrog.My.piano.app.user.entity.User;
import com.omegafrog.My.piano.app.user.vo.LoginMethod;
import com.omegafrog.My.piano.app.user.vo.PhoneNum;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;


@DataJpaTest
class TicketRepositoryTest {

    @Autowired
    private TicketRepository ticketRepository;

    @Test
    void saveNFindTest(){
        Ticket ticket = Ticket.builder()
                .author(User.builder()
                        .name("user1")
                        .profileSrc("src1")
                        .phoneNum(PhoneNum.builder()
                                .phoneNum("010-1111-2222")
                                .isAuthorized(false)
                                .build())
                        .loginMethod(LoginMethod.EMAIL)
                        .cart(new Cart())
                        .build())
                .type(TicketType.TYPE_LESSON)
                .content("hihi")
                .build();

        Ticket saved = ticketRepository.save(ticket);
        Optional<Ticket> founded = ticketRepository.findById(saved.getId());
        Assertions.assertThat(founded).isPresent();
        Assertions.assertThat(founded.get()).isEqualTo(saved);
    }

    @Test
    void updateTest(){
        Ticket ticket = Ticket.builder()
                .author(User.builder()
                        .name("user1")
                        .profileSrc("src1")
                        .phoneNum(PhoneNum.builder()
                                .phoneNum("010-1111-2222")
                                .isAuthorized(false)
                                .build())
                        .loginMethod(LoginMethod.EMAIL)
                        .cart(new Cart())
                        .build())
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