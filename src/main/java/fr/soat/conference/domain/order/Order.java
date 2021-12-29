package fr.soat.conference.domain.order;


import fr.soat.conference.domain.booking.ConferenceName;
import fr.soat.conference.domain.booking.Seat;
import fr.soat.conference.domain.payment.AccountId;
import fr.soat.conference.domain.payment.PaymentReference;
import fr.soat.eventsourcing.api.AggregateRoot;
import fr.soat.eventsourcing.api.DecisionFunction;
import fr.soat.eventsourcing.api.EvolutionFunction;
import lombok.Getter;
import lombok.ToString;

import static fr.soat.conference.domain.order.OrderStatus.*;

@Getter
@ToString(callSuper = true, of = { "status", "conferenceName", "accoundId", "seat" })
public class Order extends AggregateRoot<OrderId> {

    private OrderStatus status;
    private ConferenceName conferenceName;
    private AccountId accountId;

    private Seat seat;
    private PaymentReference paymentReference;

    public Order(OrderId orderId) {
        super(orderId);
        this.status = NEW;
    }

    @DecisionFunction
    public Order requestBooking(ConferenceName conferenceName, AccountId accountForPayment) {
        apply(new OrderRequested(getId(), conferenceName, accountForPayment));
        return this;
    }

    @EvolutionFunction
    void apply(OrderRequested orderRequested) {
        // should init the state of order (accountId, conferenceName)
        recordChange(orderRequested);
        this.accountId = orderRequested.getAccountId();
        this.conferenceName = orderRequested.getConferenceName();
    }

    @DecisionFunction
    public Order assign(Seat bookedSeat) {
        //  expected output event is:
        // - OrderSeatBooked
        apply(new OrderSeatBooked(getId(), bookedSeat));
        return this;
    }

    @EvolutionFunction
    public void apply(OrderSeatBooked orderSeatBooked) {
        // should update state (order status and assigned seat)
        recordChange(orderSeatBooked);
        status = SEAT_BOOKED;
        seat = orderSeatBooked.getBookedSeat();
    }

    @DecisionFunction
    public void failSeatBooking() {
        //  expected output event is:
        // - OrderSeatBookingFailed
        apply(new OrderSeatBookingFailed(getId()));
    }

    @EvolutionFunction
    void apply(OrderSeatBookingFailed orderSeatBookingFailed) {
        // should update state:
        // - order status
        // - (no) assigned seat
        recordChange(orderSeatBookingFailed);
        status = SEAT_BOOKING_FAILED;
        seat = null;
    }

    @DecisionFunction
    public void confirmPayment(PaymentReference paymentReference) {
        //  expected output event is:
        // - OrderPaid
        apply(new OrderPaid(getId(), paymentReference));
    }

    @EvolutionFunction
    void apply(OrderPaid orderPaid) {
        // should update state:
        // - order status
        // - the payment reference
        recordChange(orderPaid);
        status = PAID;
        paymentReference = orderPaid.getPaymentReference();
    }

    @DecisionFunction
    public void refusePayment() {
        //  expected output event is:
        // - OrderPaymentRefused
        apply(new OrderPaymentRefused(getId()));
    }

    @EvolutionFunction
    void apply(OrderPaymentRefused orderPaymentRefused) {
        // should update state:
        // - order status
        // - (no) payment reference
        // - but also the fact the is NO more assigned seat !
        recordChange(orderPaymentRefused);
        status = PAYMENT_REFUSED;
        paymentReference = null;
        seat = null;
    }

}
