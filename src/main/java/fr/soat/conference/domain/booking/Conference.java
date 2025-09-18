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
public class Conference extends AggregateRoot<ConferenceName>  {

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
        //FIXME
        // given the input event, init the conference state
        throw new RuntimeException("implement me !");
    }

    @DecisionFunction
    public Optional<Seat> bookSeat(OrderId orderId) {
        //FIXME
        // if some seats are available, we should remove one seat from available seats and return it
        // The possible expected output events are:
        // - SeatBookingRequestRefused
        // - SeatBooked
        throw new RuntimeException("implement me !");
    }

    @DecisionFunction
    public void cancelBooking(Seat seat) {
        //FIXME
        // The expected output event is:
        // - SeatReleased
        throw new RuntimeException("implement me !");
    }

    @EvolutionFunction
    void apply(SeatBooked conferenceSeatBooked) {
        //FIXME
        // given the input event:
        // - update the remaining available seats
        // - update the conference status if needed
        throw new RuntimeException("implement me !");
    }

    @EvolutionFunction
    void apply(SeatBookingRequestRefused seatBookingRequestRefused) {
        recordChange(seatBookingRequestRefused);
    }

    @EvolutionFunction
    void apply(SeatReleased seatReleased) {
        //FIXME
        // similar to apply(SeatBooked)
        throw new RuntimeException("implement me !");
    }

    @Override
    public String toString() {
        return "room " + this.getId().getName() +
                ": " +
                availableSeats.size() + " / " + seats.size() + " available seats" +
                " (" + status + ")";
    }

}
