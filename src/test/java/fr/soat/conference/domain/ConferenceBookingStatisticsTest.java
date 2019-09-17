package fr.soat.conference.domain;

import fr.soat.conference.application.configuration.ConferenceConfiguration;
import fr.soat.conference.domain.booking.Conference;
import fr.soat.conference.domain.booking.ConferenceName;
import fr.soat.conference.domain.payment.Account;
import fr.soat.conference.domain.payment.AccountId;
import fr.soat.conference.infra.booking.ConferenceRepository;
import fr.soat.conference.infra.payment.AccountRepository;
import fr.soat.conference.infra.statistics.StatisticsRepository;
import fr.soat.eventsourcing.api.EventStore;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = ConferenceConfiguration.class)
public class ConferenceBookingStatisticsTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ConferenceRepository conferenceRepository;

    @Autowired
    private ConferenceCommandHandler conferenceCommandHandler;

    @Autowired
    private EventStore eventStore;

    @Autowired
    private StatisticsRepository statisticsRepository;

    @Before
    public void setUp() {
        eventStore.clear();
        statisticsRepository.clear();
    }

    @Test
    public void should_update_conference_statistics_successfully() throws UnsupportedEncodingException {
        // Given
        AccountId myAccountId = AccountId.next();
        Account myAccount = new Account(myAccountId).credit(100);
        accountRepository.save(myAccount);

        AccountId yourAccountId = AccountId.next();
        Account yourAccount = new Account(yourAccountId).credit(100);
        accountRepository.save(yourAccount);

        ConferenceName conferenceStrategyName = ConferenceName.name("La stratégie de l'echec");
        Conference conferenceStrategy = new Conference(conferenceStrategyName).open(10, 5);
        conferenceRepository.save(conferenceStrategy);

        ConferenceName conferenceMondeName = ConferenceName.name("10 astuces pour devenir maitre du monde. La 4ème va vous étonner ");
        Conference conferenceMonde = new Conference(conferenceMondeName).open(10, 7);
        conferenceRepository.save(conferenceMonde);

        // When
        conferenceCommandHandler.requestOrder(conferenceStrategyName, myAccountId);
        conferenceCommandHandler.requestOrder(conferenceMondeName, myAccountId);
        conferenceCommandHandler.requestOrder(conferenceMondeName, yourAccountId);

        // Then
        String output = execute(conferenceCommandHandler::getStatistics);
        assertThat(output).isEqualTo("conferece;booking_rate;incomes\n" +
                "La stratégie de l'echec;10%;5\n" +
                "10 astuces pour devenir maitre du monde. La 4ème va vous étonner ;20%;14\n");
    }

    private String execute(Consumer<PrintStream> func) throws UnsupportedEncodingException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        func.accept(ps);
        String output = os.toString("UTF8");
        System.out.println(os);
        return output;
    }


}
