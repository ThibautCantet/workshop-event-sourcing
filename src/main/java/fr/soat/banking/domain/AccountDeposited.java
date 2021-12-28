package fr.soat.banking.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@EqualsAndHashCode
@ToString(callSuper = true)
public final class AccountDeposited extends AccountEvent {

    @Getter
    private final Integer amount;

    public AccountDeposited(AccountId accountId, Integer amount) {
        super(accountId);
        this.amount = amount;
    }

    public Integer getAmount() {
        return amount;
    }

    @Override
    public void applyOn(Account account) {
        account.apply(this);
    }
}
