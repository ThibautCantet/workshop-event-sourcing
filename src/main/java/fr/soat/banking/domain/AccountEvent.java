package fr.soat.banking.domain;

import fr.soat.eventsourcing.api.AggregateId;
import fr.soat.eventsourcing.api.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString(of = "accountId")
public abstract sealed class AccountEvent implements Event permits AccountClosed, AccountDeposited, AccountOpened, AccountWithdrawn {

    private final AccountId accountId;

    public AggregateId getAggregateId() {
        return accountId;
    }

    abstract void applyOn(Account account);
}
