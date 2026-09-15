package org.example.interviewapi.reservation;

import java.util.Objects;

/**
 * Plain Java application service with two required collaborators.
 *
 * <p>The constructor is complete. The Spring bean-registration decision is
 * intentionally left to the learner.</p>
 */
public final class ReservationService {
    private final ReservationRepository repository;
    private final ReservationIdGenerator idGenerator;

    public ReservationService(
            ReservationRepository repository,
            ReservationIdGenerator idGenerator
    ) {
        this.repository = Objects.requireNonNull(repository);
        this.idGenerator = Objects.requireNonNull(idGenerator);
    }

    public Reservation create(String ownerId, int quantity) {
        Reservation reservation = new Reservation(
                idGenerator.nextId(),
                ownerId,
                quantity
        );
        return repository.save(reservation);
    }
}
