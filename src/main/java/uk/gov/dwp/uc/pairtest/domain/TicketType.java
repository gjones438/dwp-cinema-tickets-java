package uk.gov.dwp.uc.pairtest.domain;

public enum TicketType {
    ADULT(25), CHILD(15), INFANT(0, false);

    private final int cost;
    private final boolean requiresSeat;

    TicketType(int cost) {
        this(cost, true);
    }

    TicketType(int cost, boolean requiresSeat) {
        this.cost = cost;
        this.requiresSeat = requiresSeat;
    }

    public int getCost() {
        return cost;
    }

    public boolean requiresSeat() {
        return requiresSeat;
    }

}
