package fr.soat.conference.domain.statistics;

import fr.soat.conference.domain.booking.ConferenceName;
import fr.soat.conference.domain.booking.SeatBooked;
import fr.soat.conference.domain.booking.SeatReleased;
import fr.soat.conference.domain.order.Order;
import fr.soat.conference.domain.payment.PaymentAccepted;
import fr.soat.conference.infra.order.OrderRepository;
import fr.soat.conference.infra.statistics.StatisticsRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class StatisticsUpdateManager {

    private final StatisticsRepository statisticsRepository;
    private final OrderRepository orderRepository;

    public StatisticsUpdateManager(StatisticsRepository statisticsRepository, OrderRepository orderRepository) {
        this.statisticsRepository = statisticsRepository;
        this.orderRepository = orderRepository;
    }

    @EventListener
    public void on(PaymentAccepted paymentAccepted) {
        Order order = orderRepository.load(paymentAccepted.getOrderId());
        ConferenceName conferenceName = order.getConferenceName();
        int amount = paymentAccepted.getAmount();
        statisticsRepository.increaseIncomes(conferenceName, amount);
    }

}
