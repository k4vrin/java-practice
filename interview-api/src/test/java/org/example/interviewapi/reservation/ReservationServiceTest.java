package org.example.interviewapi.reservation;

import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class ReservationServiceTest {

    @Test
    void createsReservationWithPlainJavaConstructorInjection() {
        UUID fixedId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        AtomicReference<Reservation> saved = new AtomicReference<>();
        ReservationRepository repository = reservation -> {
            saved.set(reservation);
            return reservation;
        };
        ReservationIdGenerator idGenerator = () -> fixedId;
        ReservationService service = new ReservationService(repository, idGenerator);

        Reservation result = service.create("customer-42", 3);

        assertEquals(fixedId, result.id());
        assertEquals("customer-42", result.ownerId());
        assertEquals(3, result.quantity());
        assertSame(result, saved.get());
    }
}
