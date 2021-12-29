package fr.soat.conference.domain;

import fr.soat.conference.domain.booking.Conference;
import fr.soat.conference.domain.booking.SeatBooked;
import fr.soat.conference.domain.booking.SeatBookingRequestRefused;
import fr.soat.conference.domain.order.Order;
import fr.soat.conference.domain.order.OrderRequested;
import fr.soat.conference.domain.payment.Account;
import fr.soat.conference.domain.payment.PaymentAccepted;
import fr.soat.conference.domain.payment.PaymentRefused;
import fr.soat.conference.infra.booking.ConferenceRepository;
import fr.soat.conference.infra.order.OrderRepository;
import fr.soat.conference.infra.payment.AccountRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ConferenceBookingProcessManager {

    private final OrderRepository orderRepository;
    private final ConferenceRepository conferenceRepository;
    private final AccountRepository accountRepository;

    @Autowired
    public ConferenceBookingProcessManager(OrderRepository orderRepository, ConferenceRepository conferenceRepository, AccountRepository accountRepository) {
        this.orderRepository = orderRepository;
        this.conferenceRepository = conferenceRepository;
        this.accountRepository = accountRepository;
    }

    @EventListener
    public void on(OrderRequested orderRequested) {
        log.info("consuming {}", orderRequested.getClass().getSimpleName());
        // expected to bookSeat() on the target conference
        // book a seat
        Conference conference = conferenceRepository.load(orderRequested.getConferenceName());
        conference.bookSeat(orderRequested.getOrderId());
        conferenceRepository.save(conference);
    }

    @EventListener
    public void on(SeatBooked seatBooked) {
        log.info("consuming {}", seatBooked.getClass().getSimpleName());
        // expected to
        // 1. assign the seat to the order
        // 2. request a payment on the customer account
        Order order = orderRepository.load(seatBooked.getOrderId());
        order.assign(seatBooked.getSeat());
        orderRepository.save(order);

        Account account = accountRepository.load(order.getAccountId());
        Conference conference = conferenceRepository.load(order.getConferenceName());
        account.requestPayment(conference.getSeatPrice(), seatBooked.getOrderId());
        accountRepository.save(account);
    }

    @EventListener
    public void on(SeatBookingRequestRefused seatBookingRequestRefused) {
        log.info("consuming {}", seatBookingRequestRefused.getClass().getSimpleName());
        // expected to propagate the seat booking request failure to the order through order.failSeatBooking()
        Order order = orderRepository.load(seatBookingRequestRefused.getOrderId());
        order.failSeatBooking();
        orderRepository.save(order);
    }

    @EventListener
    public void on(PaymentAccepted paymentAccepted) {
        log.info("consuming {}", paymentAccepted.getClass().getSimpleName());
        // expected to confirm the payment to the order through order.confirmPayment()
        Order order = orderRepository.load(paymentAccepted.getOrderId());
        order.confirmPayment(paymentAccepted.getPaymentReference());
        orderRepository.save(order);
    }

    @EventListener
    public void on(PaymentRefused paymentRefused) {
        log.info("consuming {}", paymentRefused.getClass().getSimpleName());
        // expected to:
        // 1. propagate the payment refuse to the order through a order.refusePayment()
        // 2. cancel the booking on the conferenace to release the seat, through conference.cancelBooking()
        Order order = orderRepository.load(paymentRefused.getOrderId());
        order.refusePayment();
        orderRepository.save(order);

        Conference conference = conferenceRepository.load(order.getConferenceName());
        conference.cancelBooking(order.getSeat());
        conferenceRepository.save(conference);
    }
}
