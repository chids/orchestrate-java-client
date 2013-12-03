package io.orchestrate.client;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Iterator;
import java.util.List;

/**
 * A container for the events from an event query.
 *
 * @param <T> The deserializable type for the event objects.
 */
@ToString
@EqualsAndHashCode
public class Events<T> implements Iterable<Event<T>> {

    /** The events from the request. */
    private final List<Event<T>> events;

    Events(final List<Event<T>> events) {
        assert (events != null);

        this.events = events;
    }

    /**
     * Returns the events from this request.
     *
     * @return The events.
     */
    public final Iterable<Event<T>> getEvents() {
        return events;
    }

    /**
     * Returns the number of events.
     *
     * @return The number of events.
     */
    public final int count() {
        return events.size();
    }

    /** {@inheritDoc} */
    @Override
    public final Iterator<Event<T>> iterator() {
        return events.iterator();
    }

}
