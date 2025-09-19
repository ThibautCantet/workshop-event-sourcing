package fr.soat.conference.domain.booking;

import fr.soat.conference.domain.order.OrderId;
import fr.soat.eventsourcing.api.AggregateRoot;
import fr.soat.eventsourcing.api.DecisionFunction;
import fr.soat.eventsourcing.api.EvolutionFunction;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static fr.soat.conference.domain.booking.ConferenceStatus.*;

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
    void apply(ConferenceOpened conferenceOpened) {
        status = OPEN;
        this.seatPrice = conferenceOpened.getSeatPrice();
        for (int i = 1; i <= conferenceOpened.getPlaces(); i++) {
            Seat seat = new Seat(i);
            seats.add(seat);
            availableSeats.add(seat);
        }
        recordChange(conferenceOpened);
    }

    @DecisionFunction
    public Optional<Seat> bookSeat(OrderId orderId) {
        if (!availableSeats.isEmpty()) {
            Optional<Seat> availableSeat = availableSeats.stream().findFirst();
            apply(new SeatBooked(getId(), orderId, availableSeat.get()));
            return availableSeat;
        }
        apply(new SeatBookingRequestRefused(getId(), orderId));
        return Optional.empty();
    }

    @DecisionFunction
    public void cancelBooking(Seat seat) {
        apply(new SeatReleased(getId(), seat));
    }

    @EvolutionFunction
    void apply(SeatBooked conferenceSeatBooked) {
        availableSeats.remove(conferenceSeatBooked.getSeat());
        if (availableSeats.isEmpty()) {
            status = FULL;
        }
        recordChange(conferenceSeatBooked);
    }

    @EvolutionFunction
    void apply(SeatBookingRequestRefused seatBookingRequestRefused) {
        recordChange(seatBookingRequestRefused);
    }

    @EvolutionFunction
    void apply(SeatReleased seatReleased) {
        availableSeats.add(seatReleased.getSeat());
        if (status == FULL) {
            status = OPEN;
        }
        recordChange(seatReleased);
    }

    @Override
    public String toString() {
        return "room " + this.getId().getName() +
               ": " +
               availableSeats.size() + " / " + seats.size() + " available seats" +
               " (" + status + ")";
    }

}
