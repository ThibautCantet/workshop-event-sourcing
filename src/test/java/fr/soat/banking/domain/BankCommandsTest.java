package fr.soat.banking.domain;

import fr.soat.eventsourcing.impl.InMemoryEventStore;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static fr.soat.banking.domain.AccountStatus.CLOSED;

public class BankCommandsTest {

    private BankCommandHandler bankCommandHandler = new BankCommandHandler(new AccountRepository(new InMemoryEventStore()));

    @Test
    public void should_register_a_new_account_then_use_then_close() {
        AccountId accountId = bankCommandHandler.openAccount("toto");

        // When
        bankCommandHandler.deposit(accountId, 100);
        bankCommandHandler.deposit(accountId, 200);
        bankCommandHandler.withdraw(accountId, 300);
        bankCommandHandler.closeAccount(accountId);

        // Then
        Account reloadedAccount = bankCommandHandler.loadAccount(accountId);
        Assertions.assertThat(reloadedAccount.getStatus()).isEqualTo(CLOSED);
        Assertions.assertThat(reloadedAccount.getBalance()).isEqualTo(0);
    }
}
