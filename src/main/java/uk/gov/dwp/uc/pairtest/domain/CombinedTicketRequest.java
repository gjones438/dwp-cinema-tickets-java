package uk.gov.dwp.uc.pairtest.domain;

import java.util.EnumMap;
import java.util.Map;

public record CombinedTicketRequest(int adults, int children, int infants, int cost, int tickets,
        int seats) {

    public static CombinedTicketRequest of(TicketTypeRequest... ticketTypeRequests) {
        // using map to allow for multiple TicketTypeRequest of the same ticket type
        Map<TicketType, Integer> combinedTickets = new EnumMap<>(TicketType.class);
        int cost = 0;
        int tickets = 0;
        int seats = 0;
        for (TicketTypeRequest ticketRequest : ticketTypeRequests) {
            cost += ticketRequest.cost();
            tickets += ticketRequest.numberOfTickets();
            seats += ticketRequest.seats();
            combinedTickets.merge(ticketRequest.ticketType(), ticketRequest.numberOfTickets(),
                    Integer::sum);
        }

        int adults = combinedTickets.getOrDefault(TicketType.ADULT, 0);
        int children = combinedTickets.getOrDefault(TicketType.CHILD, 0);
        int infants = combinedTickets.getOrDefault(TicketType.INFANT, 0);
        return new CombinedTicketRequest(adults, children, infants, cost, tickets, seats);
    }

}
