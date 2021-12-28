package fr.soat.banking.domain;

import fr.soat.eventsourcing.api.Event;
import fr.soat.eventsourcing.api.EventStore;
import org.springframework.stereotype.Repository;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Repository
public class AccountRepository {

    private final EventStore eventStore;

    public AccountRepository(EventStore eventStore) {
        this.eventStore = eventStore;
    }

    public void save(Account account) {
        AccountId aggregateId = account.getId();
        eventStore.store(aggregateId, account.getChanges());
    }

    public Account load(AccountId accountId) {
        List<AccountEvent> events = asAccountEvents(eventStore.loadEvents(accountId));
        return hydrate(accountId, events);
    }

    private static Account hydrate(AccountId accountId, List<AccountEvent> events) {
        Account account = new Account(accountId);
        events.forEach(event -> event.applyOn(account));
        return account;
    }

    private List<AccountEvent> asAccountEvents(List<Event> events) {
        return events.stream().map(AccountEvent.class::cast).collect(toList());
    }
}
