package fr.soat.conference.domain.order;

import fr.soat.conference.domain.booking.ConferenceName;
import fr.soat.conference.domain.booking.Seat;
import fr.soat.conference.domain.payment.*;
import fr.soat.conference.infra.order.OrderRepository;
import fr.soat.eventsourcing.impl.InMemoryEventStore;
import fr.soat.eventsourcing.impl.NOOPEventPublisher;
import org.junit.jupiter.api.Test;

import static fr.soat.conference.domain.order.OrderStatus.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class OrderTest {

    ConferenceName conferenceName = ConferenceName.name("picasso dans tous ses etats");
    AccountId myAccountId = AccountId.next();
    OrderRepository repository = new OrderRepository(new InMemoryEventStore(new NOOPEventPublisher()));
    private Seat seatOne = new Seat(1);
    private PaymentReference  paymentReference = PaymentReference.genereate();

    @Test
    public void should_create_then_book_then_pay_order() {
        Order order = OrderFactory.create();
        order.requestBooking(conferenceName, myAccountId)
                .assign(seatOne)
                .confirmPayment(paymentReference);
        repository.save(order);

        order = repository.load(order.getId());
        assertThat(order.getStatus()).isEqualTo(PAID);
        assertThat(order.getSeat()).isEqualTo(seatOne);
        assertThat(order.getPaymentReference()).isEqualTo(paymentReference);
        assertThat(order.getChanges())
                .extracting(event -> tuple(event.getClass()))
                .containsExactly(
                        tuple(OrderRequested.class),
                        tuple(OrderSeatBooked.class),
                        tuple(OrderPaid.class)
                );
    }

    @Test
    public void should_create_then_book_then_fail() {
        Order order = OrderFactory.create();
        order.requestBooking(conferenceName, myAccountId)
                .failSeatBooking();
        repository.save(order);

        order = repository.load(order.getId());
        assertThat(order.getStatus()).isEqualTo(SEAT_BOOKING_FAILED);
        assertThat(order.getSeat()).isNull();
        assertThat(order.getPaymentReference()).isNull();
        assertThat(order.getChanges())
                .extracting(event -> tuple(event.getClass()))
                .containsExactly(
                        tuple(OrderRequested.class),
                        tuple(OrderSeatBookingFailed.class)
                );
    }

    @Test
    public void should_create_then_book_then_pay_then_fail() {
        Order order = OrderFactory.create();
        order.requestBooking(conferenceName, myAccountId)
                .assign(seatOne)
                .refusePayment();
        repository.save(order);

        order = repository.load(order.getId());
        assertThat(order.getStatus()).isEqualTo(PAYMENT_REFUSED);
        assertThat(order.getSeat()).isNull();
        assertThat(order.getPaymentReference()).isNull();
        assertThat(order.getChanges())
                .extracting(event -> tuple(event.getClass()))
                .containsExactly(
                        tuple(OrderRequested.class),
                        tuple(OrderSeatBooked.class),
                        tuple(OrderPaymentRefused.class)
                );
    }

}
