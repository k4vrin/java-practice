package org.example.interviewapi;

import org.example.interviewapi.reservation.ReservationIdGenerator;
import org.example.interviewapi.reservation.ReservationRepository;
import org.example.interviewapi.reservation.ReservationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class DependencyInjectionContextTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void registersTheServiceAndItsRequiredCollaborators() {
        assertNotNull(context.getBean(ReservationRepository.class));
        assertNotNull(context.getBean(ReservationIdGenerator.class));
        assertNotNull(context.getBean(ReservationService.class));
    }
}
