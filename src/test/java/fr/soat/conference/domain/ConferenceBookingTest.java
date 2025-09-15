package fr.soat.conference.domain;

import fr.soat.conference.application.configuration.ConferenceConfiguration;
import fr.soat.conference.domain.booking.Conference;
import fr.soat.conference.domain.booking.ConferenceName;
import fr.soat.conference.domain.order.Order;
import fr.soat.conference.domain.order.OrderId;
import fr.soat.conference.domain.payment.Account;
import fr.soat.conference.domain.payment.AccountId;
import fr.soat.conference.infra.booking.ConferenceRepository;
import fr.soat.conference.infra.order.OrderRepository;
import fr.soat.conference.infra.payment.AccountRepository;
import fr.soat.eventsourcing.api.EventStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import static fr.soat.conference.domain.order.OrderStatus.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(classes = ConferenceConfiguration.class)
public class ConferenceBookingTest {

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    ConferenceRepository conferenceRepository;

    @Autowired
    ConferenceCommandHandler conferenceCommandHandler;

    @Autowired
    EventStore eventStore;

    @BeforeEach
    public void setUp() throws Exception {
        eventStore.clear();
    }

    @Test
    public void should_book_a_seat_successfully() {
        // Given
        AccountId myAccountId = AccountId.next();
        Account myAccount = new Account(myAccountId).credit(100);
        accountRepository.save(myAccount);

        ConferenceName conferenceName = ConferenceName.name("La stratégie de l'echec");
        Conference conference = new Conference(conferenceName).open(10, 15);
        conferenceRepository.save(conference);

        // When
        OrderId orderId = conferenceCommandHandler.requestOrder(conferenceName, myAccountId);

        // Then
        Order order = orderRepository.load(orderId);
        assertThat(order.getStatus()).isEqualTo(PAID);
        assertThat(order.getSeat().getPlaceNumber()).isEqualTo(1);
        assertThat(order.getPaymentReference()).matches(reference -> reference.toString().matches(".*-.*-.*-.*-.*"));

        myAccount = accountRepository.load(myAccountId);
        assertThat(myAccount.getBalance()).isEqualTo(85);

        conference = conferenceRepository.load(conferenceName);
        assertThat(conference.getAvailableSeats().size()).isEqualTo(9);
    }

    @Test
    public void should_cancel_booking_if_funds_are_insufficient() {
        // Given
        AccountId myAccountId = AccountId.next();
        Account myAccount = new Account(myAccountId).credit(3);
        accountRepository.save(myAccount);

        ConferenceName conferenceName = ConferenceName.name("La stratégie de l'echec");
        Conference conference = new Conference(conferenceName).open(10, 15);
        conferenceRepository.save(conference);

        // When
        OrderId orderId = conferenceCommandHandler.requestOrder(conferenceName, myAccountId);

        // Then
        Order order = orderRepository.load(orderId);
        assertThat(order.getStatus()).isEqualTo(PAYMENT_REFUSED);
        assertThat(order.getSeat()).isNull();
        assertThat(order.getPaymentReference()).isNull();

        myAccount = accountRepository.load(myAccountId);
        assertThat(myAccount.getBalance()).isEqualTo(3);

        conference = conferenceRepository.load(conferenceName);
        assertThat(conference.getAvailableSeats().size()).isEqualTo(10);
    }

    @Test
    public void should_cancel_booking_if_the_is_place_no_more() {
        // Given
        AccountId myAccountId = AccountId.next();
        Account myAccount = new Account(myAccountId).credit(100);
        accountRepository.save(myAccount);

        ConferenceName conferenceName = ConferenceName.name("La stratégie de l'echec");
        Conference conference = new Conference(conferenceName).open(1, 15);
        conferenceRepository.save(conference);

        // When
        OrderId orderId1 = conferenceCommandHandler.requestOrder(conferenceName, myAccountId);
        OrderId orderId2 = conferenceCommandHandler.requestOrder(conferenceName, myAccountId);

        // Then
        Order order1 = orderRepository.load(orderId1);
        assertThat(order1.getStatus()).isEqualTo(PAID);
        assertThat(order1.getSeat().getPlaceNumber()).isEqualTo(1);
        assertThat(order1.getPaymentReference()).matches(reference -> reference.toString().matches(".*-.*-.*-.*-.*"));

        Order order2 = orderRepository.load(orderId2);
        assertThat(order2.getStatus()).isEqualTo(SEAT_BOOKING_FAILED);
        assertThat(order2.getSeat()).isNull();
        assertThat(order2.getPaymentReference()).isNull();

        myAccount = accountRepository.load(myAccountId);
        assertThat(myAccount.getBalance()).isEqualTo(100 - 15);

        conference = conferenceRepository.load(conferenceName);
        assertThat(conference.getAvailableSeats().size()).isEqualTo(0);
    }

}
