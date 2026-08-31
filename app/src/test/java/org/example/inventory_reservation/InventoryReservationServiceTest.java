package org.example.inventory_reservation;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InventoryReservationServiceTest {
    @Test
    void firstReservationDecrementsStockOnce() {
        InventoryReservationService service = serviceWithFiveUnits();

        Reservation reservation = service.reserve(request("req-1", "sku-1", 3));

        assertEquals(request("req-1", "sku-1", 3), reservation.request());
        assertEquals(2, service.availableStock("sku-1"));
        assertEquals(1, service.reservationCount());
    }

    @Test
    void exactReplayReturnsOriginalEvenWhenRemainingStockIsInsufficient() {
        InventoryReservationService service = serviceWithFiveUnits();
        Reservation original = service.reserve(request("req-1", "sku-1", 3));

        Reservation replay = service.reserve(request("req-1", "sku-1", 3));

        assertSame(original, replay);
        assertEquals(2, service.availableStock("sku-1"));
        assertEquals(1, service.reservationCount());
    }

    @Test
    void conflictingReplayIsRejectedWithoutChangingStockOrCount() {
        InventoryReservationService service = serviceWithFiveUnits();
        service.reserve(request("req-1", "sku-1", 1));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.reserve(request("req-1", "sku-1", 2))
        );
        assertEquals(4, service.availableStock("sku-1"));
        assertEquals(1, service.reservationCount());
    }

    @Test
    void insufficientStockIsRejectedWithoutChangingState() {
        InventoryReservationService service = serviceWithFiveUnits();

        assertThrows(
                IllegalStateException.class,
                () -> service.reserve(request("req-1", "sku-1", 6))
        );
        assertEquals(5, service.availableStock("sku-1"));
        assertEquals(0, service.reservationCount());
    }

    @Test
    void blankSkuIsRejectedDuringRequestConstruction() {
        assertThrows(
                IllegalArgumentException.class,
                () -> request("req-1", " ", 1)
        );
    }

    @Test
    void nullSkuIsRejectedDuringRequestConstruction() {
        assertThrows(
                IllegalArgumentException.class,
                () -> request("req-1", null, 1)
        );
    }

    @Test
    void nullRequestIdIsRejectedDuringRequestConstruction() {
        assertThrows(
                IllegalArgumentException.class,
                () -> request(null, "sku-1", 1)
        );
    }

    @Test
    void unknownSkuIsRejectedDuringRequestConstruction() {
        InventoryReservationService service = serviceWithFiveUnits();

        assertThrows(
                IllegalArgumentException.class,
                () -> service.reserve(request("req-1", "null", 1))
        );
    }

    private static InventoryReservationService serviceWithFiveUnits() {
        return new InventoryReservationService(Map.of("sku-1", 5));
    }

    private static ReservationRequest request(String requestId, String sku, int quantity) {
        return new ReservationRequest(requestId, sku, quantity);
    }
}
