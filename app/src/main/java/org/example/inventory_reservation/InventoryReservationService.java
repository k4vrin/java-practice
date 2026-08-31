package org.example.inventory_reservation;

import java.util.HashMap;
import java.util.Map;

public final class InventoryReservationService {
    private final Map<String, Integer> availableBySku;
    private final Map<String, Reservation> reservationsByRequestId = new HashMap<>();

    public InventoryReservationService(Map<String, Integer> initialStock) {
        this.availableBySku = new HashMap<>(initialStock);
    }

    public Reservation reserve(ReservationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }

        Reservation existing = reservationsByRequestId.get(request.requestId());
        if (existing != null) {
            if (!existing.equals(new Reservation(request))) {
                throw new IllegalArgumentException("Conflicting request");
            }
            return existing;
        }

        Integer available = availableBySku.get(request.sku());
        if (available == null) {
            throw new IllegalArgumentException("unknown sku");
        }
        if (available < request.quantity()) {
            throw new IllegalStateException("insufficient stock");
        }

        availableBySku.put(request.sku(), available - request.quantity());
        Reservation created = new Reservation(request);
        reservationsByRequestId.put(request.requestId(), created);
        return created;
    }

    public int availableStock(String sku) {
        if (sku == null || sku.isBlank()) {
            throw new IllegalArgumentException("sku can not be null or blank");
        }
        Integer available = availableBySku.get(sku);
        if (available == null) {
            throw new IllegalArgumentException("unknown sku");
        }
        return available;
    }

    public int reservationCount() {
        return reservationsByRequestId.size();
    }
}
