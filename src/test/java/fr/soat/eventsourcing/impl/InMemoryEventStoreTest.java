package fr.soat.eventsourcing.impl;

import fr.soat.conference.application.configuration.ConferenceConfiguration;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = ConferenceConfiguration.class)
public class InMemoryEventStoreTest {

//    @Autowired
//    EventStore eventStore;
//    private OrderId orderId = OrderId.next();
//
//    @BeforeEach
//    public void setUp() throws Exception {
//        eventStore.clear();
//    }
//
//    @Test
//    public void should_store_and_reload() {
//        // Given
//        orderId = OrderId.next();
//        List<Event> events = asList(
//                new OrderCreated(orderId, "toto", "1234-5678-9101"),
//                new AccountDeposited(orderId, 100),
//                new AccountWithdrawn(orderId, 50),
//                new OrderConfirmed(orderId)
//        );
//
//        // When
//        eventStore.store(orderId, events);
//        List<Event> reloadedEvents = eventStore.loadEvents(orderId);
//
//        // Then
//        assertThat(reloadedEvents).isEqualTo(events);
//    }
//
//    @Test
//    public void should_store_and_reload_empty_event_list() {
//        // Given
//        orderId = OrderId.next();
//        List<Event> events = Collections.emptyList();
//
//        // When
//        eventStore.store(orderId, events);
//        List<Event> reloadedEvents = eventStore.loadEvents(orderId);
//
//        // Then
//        assertThat(reloadedEvents).isEqualTo(events);
//    }
//
//    @Test
//    public void should_store_be_idempotent() {
//        // Given
//        orderId = OrderId.next();
//        List<Event> events = asList(
//                new OrderCreated(orderId, "toto", "1234-5678-9101"),
//                new AccountDeposited(orderId, 100),
//                new AccountWithdrawn(orderId, 50),
//                new OrderConfirmed(orderId)
//        );
//        eventStore.store(orderId, events);
//
//        // When
//        List<Event> sameEvents = asList(
//                new OrderCreated(orderId, "toto", "1234-5678-9101"),
//                new AccountDeposited(orderId, 100),
//                new AccountWithdrawn(orderId, 50),
//                new OrderConfirmed(orderId)
//        );
//        eventStore.store(orderId, sameEvents);
//        List<Event> reloadedEvents = eventStore.loadEvents(orderId);
//
//        // Then
//        assertThat(reloadedEvents).isEqualTo(events);
//        assertThat(reloadedEvents).isEqualTo(sameEvents);
//    }
//
//    @Test
//    public void should_store_a_new_event_on_existing_events() {
//
//        // Given
//        orderId = OrderId.next();
//        List<Event> events = newArrayList(
//                new OrderCreated(orderId, "toto", "1234-5678-9101"),
//                new AccountDeposited(orderId, 200)
//        );
//        eventStore.store(orderId, events);
//
//        // When
//        events.add(new OrderConfirmed(orderId));
//        eventStore.store(orderId, events);
//
//        // Then
//        List<Event> reloadedEvents = eventStore.loadEvents(orderId);
//        assertThat(reloadedEvents).isEqualTo(events);
//    }
//
//    @Test
//    public void should_store_new_events_on_existing_events() {
//
//        // Given
//        orderId = OrderId.next();
//        List<Event> events = newArrayList(
//                new OrderCreated(orderId, "toto", "1234-5678-9101"),
//                new AccountDeposited(orderId, 200)
//        );
//        eventStore.store(orderId, events);
//
//        // When
//        events.add(new AccountDeposited(orderId, 10));
//        events.add(new AccountDeposited(orderId, 20));
//        events.add(new AccountDeposited(orderId, 30));
//        eventStore.store(orderId, events);
//
//        // Then
//        List<Event> reloadedEvents = eventStore.loadEvents(orderId);
//        assertThat(reloadedEvents).isEqualTo(events);
//    }
//
//    @Test
//    public void should_fail_to_store_when_one_concurrent_modification_occurs() {
//        // Given
//        orderId = OrderId.next();
//        List<Event> events = asList(
//                new OrderCreated(orderId, "toto", "1234-5678-9101"),
//                new AccountDeposited(orderId, 100),
//                new AccountWithdrawn(orderId, 50)
//        );
//        eventStore.store(orderId, events);
//
//        // When
//        List<Event> newEvents = newArrayList(concat(events, singletonList(new OrderConfirmed(orderId))));
//        eventStore.store(orderId, newEvents);
//
//        // Then
//        List<Event> anotherNewEvents = newArrayList(concat(events, singletonList(new AccountWithdrawn(orderId, 10))));
//        assertThatThrownBy(() -> eventStore.store(orderId, anotherNewEvents))
//                .isInstanceOf(EventConcurrentUpdateException.class)
//                .hasMessageContaining("Failed to save events, version mismatch (there was a concurrent update)");
//    }
//
//    @Test
//    public void should_fail_to_store_when_version_is_very_old() {
//        // Given
//        orderId = OrderId.next();
//        List<Event> events = asList(
//                new OrderCreated(orderId, "toto", "1234-5678-9101"),
//                new AccountDeposited(orderId, 100),
//                new AccountDeposited(orderId, 200),
//                new AccountWithdrawn(orderId, 50)
//        );
//        eventStore.store(orderId, events);
//
//        // When
//        List<Event> anotherEventsLateVersion = asList(
//                new OrderCreated(orderId, "toto", "1234-5678-9101"),
//                new AccountDeposited(orderId, 500)
//        );
//
//        // Then
//        assertThatThrownBy(() -> eventStore.store(orderId, anotherEventsLateVersion))
//                .isInstanceOf(EventConcurrentUpdateException.class)
//                .hasMessageContaining("Failed to save events, version mismatch (there was a concurrent update)");
//    }
}
