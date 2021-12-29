package fr.soat.conference.domain.booking;

import fr.soat.conference.domain.order.OrderId;
import fr.soat.eventsourcing.api.AggregateRoot;
import fr.soat.eventsourcing.api.DecisionFunction;
import fr.soat.eventsourcing.api.EvolutionFunction;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static fr.soat.conference.domain.booking.ConferenceStatus.*;
import static java.util.Optional.*;

@Getter
public class Conference extends AggregateRoot<ConferenceName> {

    private final List<Seat> seats = new ArrayList<>();
    private final List<Seat> availableSeats = new ArrayList<>();
    private int seatPrice;
    private ConferenceStatus status;

    public Conference(ConferenceName conferenceName) {
        super(conferenceName);
        this.status = NEW;
    }

    @DecisionFunction
    public Conference open(int places, int seatPrice) {
        apply(new ConferenceOpened(getId(), places, seatPrice));
        return this;
    }

    @EvolutionFunction
    public void apply(ConferenceOpened conferenceOpened) {
        // given the input event, init the conference state
        this.status = OPEN;
        this.seatPrice = conferenceOpened.getSeatPrice();
        IntStream.range(0, conferenceOpened.getPlaces())
                .forEach(this::initializeSeats);
        recordChange(conferenceOpened);
    }

    private void initializeSeats(int seatNumber) {
        Seat seat = new Seat(seatNumber + 1);
        this.seats.add(seatNumber, seat);
        this.availableSeats.add(seatNumber, seat);
    }

    @DecisionFunction
    public Optional<Seat> bookSeat(OrderId orderId) {
        // if some seats are available, we should remove one seat from available seats and return it
        // The possible expected output events are:
        // - SeatBookingRequestRefused
        // - SeatBooked
        if (!availableSeats.isEmpty()) {
            Seat bookedSeat = availableSeats.remove(0);
            apply(new SeatBooked(getId(), orderId, bookedSeat));
            return of(bookedSeat);
        } else {
            apply(new SeatBookingRequestRefused(getId(), orderId));
            return empty();
        }
    }

    @DecisionFunction
    public void cancelBooking(Seat seat) {
        // The expected output event is:
        // - SeatReleased
        apply(new SeatReleased(getId(), seat));
    }

    @EvolutionFunction
    public void apply(SeatBooked conferenceSeatBooked) {
        // given the input event:
        // - update the remaining available seats
        // - update the conference status if needed
        recordChange(conferenceSeatBooked);
        if (availableSeats.isEmpty()) {
            status = FULL;
        }
        this.availableSeats.remove(conferenceSeatBooked.getSeat());
    }

    @EvolutionFunction
    public void apply(SeatBookingRequestRefused seatBookingRequestRefused) {
        recordChange(seatBookingRequestRefused);
    }

    @EvolutionFunction
    public void apply(SeatReleased seatReleased) {
        // similar to apply(SeatBooked)
        recordChange(seatReleased);
        this.availableSeats.add(seatReleased.getSeat());
        status = OPEN;
    }

    @Override
    public String toString() {
        return "room " + this.getId().getName() +
                ": " +
                availableSeats.size() + " / " + seats.size() + " available seats" +
                " (" + status + ")";
    }

}
