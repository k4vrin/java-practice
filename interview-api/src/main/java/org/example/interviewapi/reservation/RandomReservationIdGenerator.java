package org.example.interviewapi.reservation;

import java.util.UUID;

/** Infrastructure adapter. Decide explicitly how Spring should register it. */
public final class RandomReservationIdGenerator implements ReservationIdGenerator {

    @Override
    public UUID nextId() {
        return UUID.randomUUID();
    }
}
