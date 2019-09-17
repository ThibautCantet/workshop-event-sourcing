package fr.soat.conference.infra.statistics;

import fr.soat.conference.domain.booking.ConferenceName;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;

public class StatisticsRepositoryTest {

    private StatisticsRepository statisticsRepository = new StatisticsRepository();
    private ConferenceName exoConference = ConferenceName.name("exoconference");
    private ConferenceName endoConference = ConferenceName.name("endoConference");
    private ConferenceName unknownConference = ConferenceName.name("unknownConference");

    @Before
    public void setUp() throws Exception {
        statisticsRepository.clear();
    }

    @Test
    public void should_increase_incomes() {
        statisticsRepository.increaseIncomes(exoConference, 11);
        statisticsRepository.increaseIncomes(exoConference, 1);
        statisticsRepository.increaseIncomes(endoConference, 3);
        Assertions.assertThat(statisticsRepository.getIncomes(exoConference)).isEqualTo(12);
        Assertions.assertThat(statisticsRepository.getIncomes(endoConference)).isEqualTo(3);
        Assertions.assertThat(statisticsRepository.getIncomes(unknownConference)).isEqualTo(0);
    }

    @Test
    public void should_increase_bookings() {
        statisticsRepository.increaseBookingNumber(exoConference);
        statisticsRepository.increaseBookingNumber(exoConference);
        statisticsRepository.decreaseBookingNumber(exoConference);
        statisticsRepository.increaseBookingNumber(exoConference);
        statisticsRepository.increaseBookingNumber(endoConference);
        Assertions.assertThat(statisticsRepository.getBookingNumber(exoConference)).isEqualTo(2);
        Assertions.assertThat(statisticsRepository.getBookingNumber(endoConference)).isEqualTo(1);
        Assertions.assertThat(statisticsRepository.getBookingNumber(unknownConference)).isEqualTo(0);
    }
}
