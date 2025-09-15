package fr.soat.eventsourcing.impl;

import fr.soat.banking.domain.*;
import fr.soat.eventsourcing.api.Event;
import fr.soat.eventsourcing.api.EventConcurrentUpdateException;
import fr.soat.eventsourcing.api.EventStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;
import java.util.List;

import static com.google.common.collect.Iterables.concat;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.util.Lists.newArrayList;

@SpringBootTest
public class InMemoryEventStoreTest {

    @Autowired
    EventStore eventStore;
    private AccountId accountId = AccountId.next();

    @BeforeEach
    public void setUp() throws Exception {
        eventStore.clear();
    }

    @Test
    public void should_store_and_reload() {
        // Given
        accountId = AccountId.next();
        List<Event> events = asList(
                new AccountOpened(accountId, "toto", "1234-5678-9101"),
                new AccountDeposited(accountId, 100),
                new AccountWithdrawn(accountId, 50),
                new AccountClosed(accountId)
        );

        // When
        eventStore.store(accountId, events);
        List<Event> reloadedEvents = eventStore.loadEvents(accountId);

        // Then
        assertThat(reloadedEvents).isEqualTo(events);
    }

    @Test
    public void should_store_and_reload_empty_event_list() {
        // Given
        accountId = AccountId.next();
        List<Event> events = Collections.emptyList();

        // When
        eventStore.store(accountId, events);
        List<Event> reloadedEvents = eventStore.loadEvents(accountId);

        // Then
        assertThat(reloadedEvents).isEqualTo(events);
    }

    @Test
    public void should_store_be_idempotent() {
        // Given
        accountId = AccountId.next();
        List<Event> events = asList(
                new AccountOpened(accountId, "toto", "1234-5678-9101"),
                new AccountDeposited(accountId, 100),
                new AccountWithdrawn(accountId, 50),
                new AccountClosed(accountId)
        );
        eventStore.store(accountId, events);

        // When
        List<Event> sameEvents = asList(
                new AccountOpened(accountId, "toto", "1234-5678-9101"),
                new AccountDeposited(accountId, 100),
                new AccountWithdrawn(accountId, 50),
                new AccountClosed(accountId)
        );
        eventStore.store(accountId, sameEvents);
        List<Event> reloadedEvents = eventStore.loadEvents(accountId);

        // Then
        assertThat(reloadedEvents).isEqualTo(events);
        assertThat(reloadedEvents).isEqualTo(sameEvents);
    }

    @Test
    public void should_store_a_new_event_on_existing_events() {

        // Given
        accountId = AccountId.next();
        List<Event> events = newArrayList(
                new AccountOpened(accountId, "toto", "1234-5678-9101"),
                new AccountDeposited(accountId, 200)
        );
        eventStore.store(accountId, events);

        // When
        events.add(new AccountClosed(accountId));
        eventStore.store(accountId, events);

        // Then
        List<Event> reloadedEvents = eventStore.loadEvents(accountId);
        assertThat(reloadedEvents).isEqualTo(events);
    }

    @Test
    public void should_store_new_events_on_existing_events() {

        // Given
        accountId = AccountId.next();
        List<Event> events = newArrayList(
                new AccountOpened(accountId, "toto", "1234-5678-9101"),
                new AccountDeposited(accountId, 200)
        );
        eventStore.store(accountId, events);

        // When
        events.add(new AccountDeposited(accountId, 10));
        events.add(new AccountDeposited(accountId, 20));
        events.add(new AccountDeposited(accountId, 30));
        eventStore.store(accountId, events);

        // Then
        List<Event> reloadedEvents = eventStore.loadEvents(accountId);
        assertThat(reloadedEvents).isEqualTo(events);
    }

    @Test
    public void should_fail_to_store_when_one_concurrent_modification_occurs() {
        // Given
        accountId = AccountId.next();
        List<Event> events = asList(
                new AccountOpened(accountId, "toto", "1234-5678-9101"),
                new AccountDeposited(accountId, 100),
                new AccountWithdrawn(accountId, 50)
        );
        eventStore.store(accountId, events);

        // When
        List<Event> newEvents = newArrayList(concat(events, singletonList(new AccountClosed(accountId))));
        eventStore.store(accountId, newEvents);

        // Then
        List<Event> anotherNewEvents = newArrayList(concat(events, singletonList(new AccountWithdrawn(accountId, 10))));
        assertThatThrownBy(() -> eventStore.store(accountId, anotherNewEvents))
                .isInstanceOf(EventConcurrentUpdateException.class)
                .hasMessageContaining("Failed to save events, version mismatch (there was a concurrent update)");
    }

    @Test
    public void should_fail_to_store_when_version_is_very_old() {
        // Given
        accountId = AccountId.next();
        List<Event> events = asList(
                new AccountOpened(accountId, "toto", "1234-5678-9101"),
                new AccountDeposited(accountId, 100),
                new AccountDeposited(accountId, 200),
                new AccountWithdrawn(accountId, 50)
        );
        eventStore.store(accountId, events);

        // When
        List<Event> anotherEventsLateVersion = asList(
                new AccountOpened(accountId, "toto", "1234-5678-9101"),
                new AccountDeposited(accountId, 500)
        );

        // Then
        assertThatThrownBy(() -> eventStore.store(accountId, anotherEventsLateVersion))
                .isInstanceOf(EventConcurrentUpdateException.class)
                .hasMessageContaining("Failed to save events, version mismatch (there was a concurrent update)");
    }
}
