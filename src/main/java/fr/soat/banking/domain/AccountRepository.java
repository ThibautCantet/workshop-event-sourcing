package fr.soat.banking.domain;

import fr.soat.eventsourcing.api.Event;
import fr.soat.eventsourcing.api.EventStore;

import java.util.List;

import static java.util.stream.Collectors.toList;


public class AccountRepository {

    private final EventStore eventStore;

    public AccountRepository(EventStore eventStore) {
        this.eventStore = eventStore;
    }

    public void save(Account account) {
        // 1. retrieve all the pending changes recorded from the account aggregate
        // 2. invoke eventStore to save these changes (events)
        eventStore.store(account.getId(), account.getChanges());
    }

    public Account load(AccountId accountId) {
        // 1. load from eventStore all the past events for the given account
        List<AccountEvent> events = eventStore.loadEvents(accountId).stream().map(event -> (AccountEvent) event).toList();
        // 2. hydrate Account to retrieve the current state
        return hydrate(accountId, events);
    }

    private static Account hydrate(AccountId accountId, List<AccountEvent> events) {
        // apply all events on a new Account to retrieve the current state
        Account account = new Account(accountId);
        for (AccountEvent accountEvent : events) {
            switch (accountEvent) {
                case AccountOpened e -> account.apply(e);
                case AccountClosed e -> account.apply(e);
                case AccountDeposited e -> account.apply(e);
                case AccountWithdrawn e -> account.apply(e);
                default -> throw new IllegalStateException("Unexpected value: " + accountEvent);
            }
        }
        return account;
    }

    private List<AccountEvent> asAccountEvents(List<Event> events) {
        return events.stream().map(event -> (AccountEvent) event).collect(toList());
    }
}
