package fr.soat.conference.infra.booking;

import fr.soat.conference.domain.booking.Conference;
import fr.soat.conference.domain.booking.ConferenceEvent;
import fr.soat.conference.domain.booking.ConferenceName;
import fr.soat.eventsourcing.api.Event;
import fr.soat.eventsourcing.api.EventStore;
import org.springframework.stereotype.Repository;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Repository
public class ConferenceRepository {

    private final EventStore eventStore;

    public ConferenceRepository(EventStore eventStore) {
        this.eventStore = eventStore;
    }

    public void save(Conference conference) {
        ConferenceName aggregateId = conference.getId();
        eventStore.store(aggregateId, conference.getChanges());
    }

    public Conference load(ConferenceName conferenceName) {
        List<ConferenceEvent> events = asRoomEvents(eventStore.loadEvents(conferenceName));
        return hydrate(conferenceName, events);
    }

    private static Conference hydrate(ConferenceName conferenceName, List<ConferenceEvent> events) {
        Conference conference = new Conference(conferenceName);
        events.forEach(event -> event.applyOn(conference));
        return conference;
    }

    private List<ConferenceEvent> asRoomEvents(List<Event> events) {
        return events.stream().map(ConferenceEvent.class::cast).collect(toList());
    }
}
