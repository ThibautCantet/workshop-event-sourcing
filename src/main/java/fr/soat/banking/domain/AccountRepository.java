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
        List<Event> changes = account.getChanges();
        // 2. invoke eventStore to save these changes (events)
        eventStore.store(account.getId(), changes);
    }

    public Account load(AccountId accountId) {
        // 1. load from eventStore all the past events for the given account
        List<AccountEvent> events = asAccountEvents(eventStore.loadEvents(accountId));
        // 2. hydrate Account to retrieve the current state
        return hydrate(accountId, events);
    }

    private static Account hydrate(AccountId accountId, List<AccountEvent> events) {
        Account account = new Account(accountId);
        events.forEach(e -> {
                    e.applyOn(account);
                });

        return account;
    }

    private List<AccountEvent> asAccountEvents(List<Event> events) {
        return events.stream().map(event -> (AccountEvent) event).collect(toList());
    }
}
