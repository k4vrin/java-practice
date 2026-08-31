package org.example.inventory_reservation;

public record ReservationRequest(String requestId, String sku, int quantity) {
    public ReservationRequest {
        if (requestId == null || requestId.isBlank()) {
            throw new IllegalArgumentException("requestId must not be null or blank");
        }
        if (sku == null || sku.isBlank()) {
            throw new IllegalArgumentException("sku must not be null or blank");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be greater than zero");
        }
    }
}
