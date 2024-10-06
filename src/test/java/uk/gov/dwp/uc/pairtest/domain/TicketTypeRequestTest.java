package uk.gov.dwp.uc.pairtest.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class TicketTypeRequestTest {

    @Test
    void cost() {
        var ticketRequest = new TicketTypeRequest(TicketType.ADULT, 3);
        assertEquals(75, ticketRequest.cost());
    }

    @Test
    void seats() {
        var ticketRequest = new TicketTypeRequest(TicketType.CHILD, 4);
        assertEquals(4, ticketRequest.seats());
    }

    @Test
    void seatsInfantSeatNotRequired() {
        var ticketRequest = new TicketTypeRequest(TicketType.INFANT, 4);
        assertEquals(0, ticketRequest.seats());
    }

}
