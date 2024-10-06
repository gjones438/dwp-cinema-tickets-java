package uk.gov.dwp.uc.pairtest.domain;

public enum TicketType {
    ADULT(25), CHILD(15), INFANT(0);

    private final int cost;

    TicketType(int cost) {
        this.cost = cost;
    }

    public int getCost() {
        return cost;
    }
}
