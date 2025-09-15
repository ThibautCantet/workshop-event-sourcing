package fr.soat.conference.domain.booking;

import fr.soat.conference.domain.order.OrderId;
import fr.soat.conference.infra.booking.ConferenceRepository;
import fr.soat.eventsourcing.impl.InMemoryEventStore;
import fr.soat.eventsourcing.impl.NOOPEventPublisher;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ConferenceTest {

    ConferenceRepository repository = new ConferenceRepository(new InMemoryEventStore(new NOOPEventPublisher()));

    @Test
    public void should_create_book_then_release_seat() {
        Conference picasso = new Conference(ConferenceName.name("picasso dans tous ses etats")).open(3, 10);
        assertThat(picasso.getStatus()).isEqualTo(ConferenceStatus.OPEN);
        Seat seat1 = picasso.bookSeat(OrderId.next()).get();
        Seat seat2 = picasso.bookSeat(OrderId.next()).get();
        Seat seat3 = picasso.bookSeat(OrderId.next()).get();

        assertThat(picasso.getAvailableSeats()).isEmpty();
        assertThat(picasso.getStatus()).isEqualTo(ConferenceStatus.FULL);
        picasso.cancelBooking(seat2);

        repository.save(picasso);
        Conference reloadedPicasso = repository.load(picasso.getId());
        assertThat(picasso.getAvailableSeats()).containsExactly(seat2);
        assertThat(picasso.getSeatPrice()).isEqualTo(10);
        assertThat(picasso.getStatus()).isEqualTo(ConferenceStatus.OPEN);
    }

    @Test
    public void should_return_empty_when_booking_with_no_more_places() {
        Conference picasso = new Conference(ConferenceName.name("picasso dans tous ses etats")).open(3, 10);
        assertThat(picasso.getStatus()).isEqualTo(ConferenceStatus.OPEN);
        Seat seat1 = picasso.bookSeat(OrderId.next()).get();
        Seat seat2 = picasso.bookSeat(OrderId.next()).get();
        assertThat(picasso.getStatus()).isEqualTo(ConferenceStatus.OPEN);
        Seat seat3 = picasso.bookSeat(OrderId.next()).get();

        assertThat(picasso.bookSeat(OrderId.next())).isEmpty();
        assertThat(picasso.getStatus()).isEqualTo(ConferenceStatus.FULL);
    }

}
