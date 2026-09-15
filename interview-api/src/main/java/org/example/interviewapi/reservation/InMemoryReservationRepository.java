package org.example.interviewapi.reservation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** Infrastructure adapter. Decide explicitly how Spring should register it. */
public final class InMemoryReservationRepository implements ReservationRepository {
    private final Map<UUID, Reservation> reservations = new LinkedHashMap<>();

    @Override
    public Reservation save(Reservation reservation) {
        reservations.put(reservation.id(), reservation);
        return reservation;
    }
}
