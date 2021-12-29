package fr.soat.conference.infra.order;

import fr.soat.conference.domain.order.Order;
import fr.soat.conference.domain.order.OrderEvent;
import fr.soat.conference.domain.order.OrderId;
import fr.soat.eventsourcing.api.Event;
import fr.soat.eventsourcing.api.EventStore;
import org.springframework.stereotype.Repository;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Repository
public class OrderRepository {

    private final EventStore eventStore;

    public OrderRepository(EventStore eventStore) {
        this.eventStore = eventStore;
    }

    public void save(Order order) {
        OrderId aggregateId = order.getId();
        eventStore.store(aggregateId, order.getChanges());
    }

    public Order load(OrderId orderId) {
        List<OrderEvent> events = asOrderEvents(eventStore.loadEvents(orderId));
        return hydrate(orderId, events);
    }

    private static Order hydrate(OrderId orderId, List<OrderEvent> events) {
        Order order = new Order(orderId);
        events.forEach(event -> event.applyOn(order));
        return order;
    }

    private List<OrderEvent> asOrderEvents(List<Event> events) {
        return events.stream().map(OrderEvent.class::cast).collect(toList());
    }
}
