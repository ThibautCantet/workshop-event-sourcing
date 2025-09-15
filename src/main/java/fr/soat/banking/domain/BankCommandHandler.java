package fr.soat.banking.domain;

import fr.soat.eventsourcing.api.Command;

public class BankCommandHandler {

    private AccountRepository repository;

    public BankCommandHandler(AccountRepository repository) {
        this.repository = repository;
    }

    @Command
    public AccountId openAccount(String owner) {
        final Account account = Account
                        .create()
                        .register(owner);

        repository.save(account);
        return account.getId();
    }

    @Command
    public Account loadAccount(AccountId id) {
        return repository.load(id);
    }

    @Command
    public void deposit(AccountId id, int amount) {
        // 1. load the account aggregate using the repository
        Account account = repository.load(id);
        // 2. invoke the decision function deposit() on the aggregate to apply the business logic
        account.deposit(amount);
        // 3. save the mutated aggregate with the repository
        repository.save(account);
    }

    @Command
    public void withdraw(AccountId id, int amount) {
        // 1. load the account aggregate using the repository
        Account account = repository.load(id);
        // 2. invoke the decision function withdraw() on the aggregate to apply the business logic
        account.withdraw(amount);
        // 3. save the mutated aggregate with the repository
        repository.save(account);
    }

    @Command
    public void closeAccount(AccountId id) {
        final Account account = repository.load(id);
        account.close();
        repository.save(account);
    }
}
