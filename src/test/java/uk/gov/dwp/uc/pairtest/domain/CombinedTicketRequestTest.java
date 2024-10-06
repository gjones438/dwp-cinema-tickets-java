package uk.gov.dwp.uc.pairtest.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class CombinedTicketRequestTest {

    @Test
    void ofTicketTypeRequestCombiner() {
        var ticketRequest = CombinedTicketRequest.of(new TicketTypeRequest(TicketType.CHILD, 3),
                new TicketTypeRequest(TicketType.INFANT, 4),
                new TicketTypeRequest(TicketType.ADULT, 2),
                new TicketTypeRequest(TicketType.CHILD, 1),
                new TicketTypeRequest(TicketType.ADULT, 3),
                new TicketTypeRequest(TicketType.CHILD, 5));

        assertEquals(5, ticketRequest.adults());
        assertEquals(9, ticketRequest.children());
        assertEquals(4, ticketRequest.infants());
        assertEquals(260, ticketRequest.cost());
        assertEquals(18, ticketRequest.tickets());
        assertEquals(14, ticketRequest.seats());
    }

}
