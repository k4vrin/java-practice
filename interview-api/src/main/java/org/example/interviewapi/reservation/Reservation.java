package org.example.interviewapi.reservation;

import java.util.UUID;

public record Reservation(UUID id, String ownerId, int quantity) {
}
