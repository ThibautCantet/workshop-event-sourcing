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
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ConferenceBookingProcessManager {

    private final OrderRepository orderRepository;
    private final ConferenceRepository conferenceRepository;
    private final AccountRepository accountRepository;

    public ConferenceBookingProcessManager(OrderRepository orderRepository, ConferenceRepository conferenceRepository, AccountRepository accountRepository) {
        this.orderRepository = orderRepository;
        this.conferenceRepository = conferenceRepository;
        this.accountRepository = accountRepository;
    }

    @EventListener
    public void on(OrderRequested orderRequested) {
        log.info("consuming {}", orderRequested.getClass().getSimpleName());
        Conference conference = conferenceRepository.load(orderRequested.getConferenceName());
        Order order = orderRepository.load(orderRequested.getOrderId());
        conference.bookSeat(order.getId());
        conferenceRepository.save(conference);
    }

    @EventListener
    public void on(SeatBooked seatBooked) {
        log.info("consuming {}", seatBooked.getClass().getSimpleName());
        Order order = orderRepository.load(seatBooked.getOrderId());
        Account account = accountRepository.load(order.getAccountId());
        Conference conference = conferenceRepository.load(order.getConferenceName());
        account.requestPayment(conference.getSeatPrice(), order.getId());
        order.assign(seatBooked.getSeat());
        orderRepository.save(order);
        accountRepository.save(account);
    }

    @EventListener
    public void on(SeatBookingRequestRefused seatBookingRequestRefused) {
        log.info("consuming {}", seatBookingRequestRefused.getClass().getSimpleName());
        Order order = orderRepository.load(seatBookingRequestRefused.getOrderId());
        order.failSeatBooking();
        orderRepository.save(order);
    }

    @EventListener
    public void on(PaymentAccepted paymentAccepted) {
        log.info("consuming {}", paymentAccepted.getClass().getSimpleName());
        Order order = orderRepository.load(paymentAccepted.getOrderId());
        order.confirmPayment(paymentAccepted.getPaymentReference());
        orderRepository.save(order);
    }

    @EventListener
    public void on(PaymentRefused paymentRefused) {
        log.info("consuming {}", paymentRefused.getClass().getSimpleName());
        Order order = orderRepository.load(paymentRefused.getOrderId());
        order.refusePayment();
        Conference conference = conferenceRepository.load(order.getConferenceName());
        conference.cancelBooking(order.getSeat());
        orderRepository.save(order);
        conferenceRepository.save(conference);
    }
}
