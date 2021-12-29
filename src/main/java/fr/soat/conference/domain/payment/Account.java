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
        // The expected output event is:
        // - AccountCredited
        apply(new AccountCredited(this.getId(), amount));
        return this;
    }

    @EvolutionFunction
    public void apply(AccountCredited accountCredited) {
        // should update the balance !
        recordChange(accountCredited);
        this.balance += accountCredited.getAmount();
    }

    @DecisionFunction
    public Account requestPayment(int amount, OrderId orderId) {
        // 1. should always keep trace of request (PaymentRequested event)
        PaymentRequested paymentRequested = new PaymentRequested(getId(), amount);
        apply(paymentRequested);
        // 2. should then check if funds are sufficient
        // The possible expected output events are:
        // - PaymentAccepted
        // - PaymentRefused
        if (this.balance >= amount) {
            PaymentAccepted paymentAccepted = new PaymentAccepted(PaymentReference.genereate(), getId(), amount, orderId);
            apply(paymentAccepted);
        } else {
            PaymentRefused paymentRefused = new PaymentRefused(getId(), amount, orderId);
            apply(paymentRefused);
        }
        return this;
    }

    @EvolutionFunction
    public void apply(PaymentRequested paymentRequested) {
        recordChange(paymentRequested);
    }

    @EvolutionFunction
    public void apply(PaymentAccepted paymentAccepted) {
        // should update the balance !
        recordChange(paymentAccepted);
        balance -= paymentAccepted.getAmount();
    }

    @EvolutionFunction
    public void apply(PaymentRefused paymentRefused) {
        recordChange(paymentRefused);
    }

}
