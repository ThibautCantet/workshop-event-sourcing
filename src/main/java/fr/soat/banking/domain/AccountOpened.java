package fr.soat.banking.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@EqualsAndHashCode
@ToString(callSuper = true)
@Getter
public final class AccountOpened extends AccountEvent {

    private final String owner;
    private final String number;

    public AccountOpened(AccountId id, String owner, String number) {
        super(id);
        this.owner = owner;
        this.number = number;
    }

    @Override
    public void applyOn(Account account) {
        account.apply(this);
    }

}
