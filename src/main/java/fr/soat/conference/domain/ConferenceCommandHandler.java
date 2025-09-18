package fr.soat.conference.domain;

import fr.soat.conference.domain.booking.ConferenceName;
import fr.soat.conference.domain.order.Order;
import fr.soat.conference.domain.order.OrderFactory;
import fr.soat.conference.domain.order.OrderId;
import fr.soat.conference.domain.payment.AccountId;
import fr.soat.conference.infra.order.OrderRepository;
import fr.soat.eventsourcing.api.Command;
import org.springframework.stereotype.Service;

@Service
public class ConferenceCommandHandler {

    private final OrderRepository repository;

    public ConferenceCommandHandler(OrderRepository repository) {
        this.repository = repository;
    }

    @Command
    public OrderId requestOrder(ConferenceName conferenceName, AccountId accountId) {
        Order order = OrderFactory.create();
        order.requestBooking(conferenceName, accountId);
        repository.save(order);
        return order.getId();
    }

}
