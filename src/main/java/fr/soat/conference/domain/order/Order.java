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
@ToString(callSuper = true, of = {"status", "conferenceName", "accoundId", "seat" })
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
        OrderRequested event = new OrderRequested(getId(), conferenceName, accountForPayment);
        apply(event);
        return this;
    }

    @EvolutionFunction
    void apply(OrderRequested orderRequested) {
        this.accountId = orderRequested.getAccountId();
        this.conferenceName = orderRequested.getConferenceName();
        recordChange(orderRequested);
    }

    @DecisionFunction
    public Order assign(Seat bookedSeat) {
        apply(new OrderSeatBooked(getId(), bookedSeat));
        return this;
    }

    @EvolutionFunction
    public void apply(OrderSeatBooked orderSeatBooked) {
        this.status = SEAT_BOOKED;
        this.seat = orderSeatBooked.getBookedSeat();
        recordChange(orderSeatBooked);
    }

    @DecisionFunction
    public void failSeatBooking() {
        apply(new OrderSeatBookingFailed(getId()));
    }

    @EvolutionFunction
    void apply(OrderSeatBookingFailed orderSeatBookingFailed) {
        this.status = SEAT_BOOKING_FAILED;
        this.seat = null;
        recordChange(orderSeatBookingFailed);
    }

    @DecisionFunction
    public void confirmPayment(PaymentReference paymentReference) {
        apply(new OrderPaid(getId(), paymentReference));
    }

    @EvolutionFunction
    void apply(OrderPaid orderPaid) {
        this.status = PAID;
        this.paymentReference = orderPaid.getPaymentReference();
        recordChange(orderPaid);
    }

    @DecisionFunction
    public void refusePayment() {
        apply(new OrderPaymentRefused(getId()));
    }

    @EvolutionFunction
    void apply(OrderPaymentRefused orderPaymentRefused) {
        this.status = PAYMENT_REFUSED;
        this.paymentReference = null;
        this.seat = null;
        recordChange(orderPaymentRefused);
    }

}
