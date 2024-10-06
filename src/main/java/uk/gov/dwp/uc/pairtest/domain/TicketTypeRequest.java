package uk.gov.dwp.uc.pairtest.domain;

public record TicketTypeRequest(TicketType ticketType, int numberOfTickets) {

    public int cost() {
        return numberOfTickets * ticketType.getCost();
    }

    public int seats() {
        return ticketType.requiresSeat() ? numberOfTickets : 0;
    }

}

