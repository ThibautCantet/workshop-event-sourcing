package fr.soat.banking.domain;

import org.junit.jupiter.api.Test;

import static fr.soat.banking.domain.AccountStatus.*;
import static org.assertj.core.api.Assertions.*;

public class AccountTest {

    @Test
    public void should_succeed_a_classic_scenario() {
        // Given
        Account account = Account.create();

        // When
        account.open("toto")
                .deposit(100)
                .deposit(100)
                .withdraw(200)
                .close();

        // Then
        assertThat(account.getOwner()).isEqualTo("toto");
        assertThat(account.getBalance()).isEqualTo(0);
        assertThat(account.getVersion()).isEqualTo(5);
        assertThat(account.getStatus()).isEqualTo(CLOSED);
        assertThat(account.getChanges())
                .extracting(event -> tuple(event.getClass()))
                .containsExactly(
                        tuple(AccountOpened.class),
                        tuple(AccountDeposited.class),
                        tuple(AccountDeposited.class),
                        tuple(AccountWithdrawn.class),
                        tuple(AccountClosed.class)
                );

    }

    @Test
    public void should_fail_to_get_negative_balance() {
        // Given
        Account account = Account.create();

        // When
        assertThatThrownBy(() -> account.open("toto")
                .deposit(100)
                .withdraw(200))
                .isInstanceOf(InsufficientFundsException.class)
                .hasMessage("Withdrawal of 200 can not be applied with balance of 100");
        assertThat(account.getBalance()).isEqualTo(100);
        assertThat(account.getVersion()).isEqualTo(2);
    }

    @Test
    public void should_fail_with_invalid_decisions_on_new_account() {
        // Given
        Account account = Account.create();
        assertThat(account.getStatus()).isEqualTo(NEW);

        // When
        assertThatThrownBy(() -> account.withdraw(100))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> account.deposit(100))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> account.close())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    public void should_fail_with_invalid_decisions_on_open_account() {
        // Given
        Account account = Account.create();
        account.open("alice");
        assertThat(account.getStatus()).isEqualTo(OPEN);

        // When
        assertThatThrownBy(() -> account.open("bob"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    public void should_fail_with_invalid_decisions_on_closed_account() {
        // Given
        Account account = Account.create();
        account.open("alice")
                .close();
        assertThat(account.getStatus()).isEqualTo(CLOSED);

        // When
        assertThatThrownBy(() -> account.open("bob"))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> account.withdraw(100))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> account.deposit(100))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> account.close())
                .isInstanceOf(UnsupportedOperationException.class);
    }

}
