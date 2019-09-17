package fr.soat.conference.domain;

import fr.soat.conference.domain.booking.Conference;
import fr.soat.conference.domain.booking.ConferenceName;
import fr.soat.conference.domain.order.Order;
import fr.soat.conference.domain.order.OrderFactory;
import fr.soat.conference.domain.order.OrderId;
import fr.soat.conference.domain.payment.AccountId;
import fr.soat.conference.infra.booking.ConferenceRepository;
import fr.soat.conference.infra.order.OrderRepository;
import fr.soat.conference.infra.statistics.StatisticsRepository;
import fr.soat.eventsourcing.api.Command;
import fr.soat.eventsourcing.api.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.PrintStream;
import java.util.Collection;

@Service
public class ConferenceCommandHandler {

    private final OrderRepository orderRepository;
    private final ConferenceRepository conferenceRepository;
    private final StatisticsRepository statisticsRepository;

    @Autowired
    public ConferenceCommandHandler(OrderRepository orderRepository, ConferenceRepository conferenceRepository, StatisticsRepository statisticsRepository) {
        this.orderRepository = orderRepository;
        this.conferenceRepository = conferenceRepository;
        this.statisticsRepository = statisticsRepository;
    }

    @Command
    public OrderId requestOrder(ConferenceName conferenceName, AccountId accountId) {
        Order order = OrderFactory.create();
        order.requestBooking(conferenceName, accountId);
        orderRepository.save(order);
        return order.getId();
    }

    @Query
    public void getStatistics(PrintStream printStream) {
        Collection<ConferenceName> conferences = statisticsRepository.getConferences();
        printStream.println("conferece;incomes");
        for (ConferenceName conferenceName  : conferences) {
            Conference conference = conferenceRepository.load(conferenceName);
            Integer incomes = statisticsRepository.getIncomes(conferenceName);
            printStream.println(String.format("%s;%s", conferenceName.getName(), incomes));
        }
    }
}
