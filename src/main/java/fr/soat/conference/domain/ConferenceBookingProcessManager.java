package fr.soat.conference.domain;

import fr.soat.conference.domain.booking.SeatBooked;
import fr.soat.conference.domain.booking.SeatBookingRequestRefused;
import fr.soat.conference.domain.order.OrderRequested;
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
        //FIXME
        // expected to bookSeat() on the target conference
        throw new RuntimeException("implement me !");
    }

    @EventListener
    public void on(SeatBooked seatBooked) {
        log.info("consuming {}", seatBooked.getClass().getSimpleName());
        //FIXME
        // expected to
        // 1. assign the seat to the order
        // 2. request a payment on the customer account
        throw new RuntimeException("implement me !");
    }

    @EventListener
    public void on(SeatBookingRequestRefused seatBookingRequestRefused) {
        log.info("consuming {}", seatBookingRequestRefused.getClass().getSimpleName());
        //FIXME
        // expected to propagate the seat booking request failure to the order through order.failSeatBooking()
        throw new RuntimeException("implement me !");
    }

    @EventListener
    public void on(PaymentAccepted paymentAccepted) {
        log.info("consuming {}", paymentAccepted.getClass().getSimpleName());
        //FIXME
        // expected to confirm the payment to the order through order.confirmPayment()
        throw new RuntimeException("implement me !");
    }

    @EventListener
    public void on(PaymentRefused paymentRefused) {
        log.info("consuming {}", paymentRefused.getClass().getSimpleName());
        //FIXME
        // expected to:
        // 1. propagate the payment refuse to the order through a order.refusePayment()
        // 2. cancel the booking on the conferenace to release the seat, through conference.cancelBooking()
        throw new RuntimeException("implement me !");
    }
}
