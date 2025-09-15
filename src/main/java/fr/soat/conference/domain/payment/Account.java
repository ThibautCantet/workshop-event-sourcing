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
        apply(new AccountCredited(getId(), amount));
        return this;
    }

    @EvolutionFunction
    public void apply(AccountCredited accountCredited) {
        this.balance += accountCredited.getAmount();
        recordChange(accountCredited);
    }

    @DecisionFunction
    public Account requestPayment(int amount, OrderId orderId) {
        apply(new PaymentRequested(getId(), amount));
        if (balance >= amount) {
            apply(new PaymentAccepted(PaymentReference.genereate(), getId(), amount, orderId));
        } else {
            apply(new PaymentRefused(getId(), amount, orderId));
        }
        return this;
    }

    @EvolutionFunction
    public void apply(PaymentRequested paymentRequested) {
        recordChange(paymentRequested);
    }

    @EvolutionFunction
    public void apply(PaymentAccepted paymentAccepted) {
        this.balance -= paymentAccepted.getAmount();
        recordChange(paymentAccepted);
    }

    @EvolutionFunction
    public void apply(PaymentRefused paymentRefused) {
        recordChange(paymentRefused);
    }

}
