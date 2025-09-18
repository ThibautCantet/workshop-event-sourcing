package fr.soat.conference.domain.payment;

import fr.soat.conference.domain.order.OrderId;
import fr.soat.eventsourcing.api.AggregateRoot;
import fr.soat.eventsourcing.api.DecisionFunction;
import fr.soat.eventsourcing.api.EvolutionFunction;
import lombok.Getter;

@Getter
public class Account extends AggregateRoot<AccountId>  {

    private int balance = 0;

    public Account(AccountId accountId) {
        super(accountId);
    }

    @DecisionFunction
    public Account credit(int amount) {
        //FIXME
        // The expected output event is:
        // - AccountCredited
        throw new RuntimeException("implement me !");
    }

    @EvolutionFunction
    void apply(AccountCredited accountCredited) {
        //FIXME
        // should update the balance !
        throw new RuntimeException("implement me !");
    }

    @DecisionFunction
    public Account requestPayment(int amount, OrderId orderId) {
        //FIXME
        // 1. should always keep trace of request (PaymentRequested event)
        // 2. should then check if funds are sufficient
        // The possible expected output events are:
        // - PaymentAccepted
        // - PaymentRefused
        throw new RuntimeException("implement me !");
    }

    @EvolutionFunction
    void apply(PaymentRequested paymentRequested) {
        recordChange(paymentRequested);
    }

    @EvolutionFunction
    void apply(PaymentAccepted paymentAccepted) {
        //FIXME
        // should update the balance !
        throw new RuntimeException("implement me !");
    }

    @EvolutionFunction
    void apply(PaymentRefused paymentRefused) {
        recordChange(paymentRefused);
    }

}
