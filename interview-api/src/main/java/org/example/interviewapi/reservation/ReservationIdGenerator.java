package org.example.interviewapi.reservation;

import java.util.UUID;

public interface ReservationIdGenerator {
    UUID nextId();
}
