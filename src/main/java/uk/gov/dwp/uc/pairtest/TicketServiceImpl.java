package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.CombinedTicketRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

public class TicketServiceImpl implements TicketService {
    private final TicketPaymentService ticketPaymentService;
    private final SeatReservationService seatReservationService;

    public TicketServiceImpl(TicketPaymentService ticketPaymentService,
            SeatReservationService seatReservationService) {
        this.ticketPaymentService = ticketPaymentService;
        this.seatReservationService = seatReservationService;
    }

    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {
        validateAccountId(accountId);
        var combinedTicketRequest = CombinedTicketRequest.of(ticketTypeRequests);
        validateTicketRequest(combinedTicketRequest);

        ticketPaymentService.makePayment(accountId, combinedTicketRequest.cost());
        seatReservationService.reserveSeat(accountId, combinedTicketRequest.seats());
    }

    private void validateAccountId(Long accountId) {
        if (accountId == null) {
            throw new InvalidPurchaseException("accountId required");
        }
        if (accountId <= 0) {
            throw new InvalidPurchaseException("Invalid accountId");
        }
    }

    private void validateTicketRequest(CombinedTicketRequest ticketRequest) {
        if (ticketRequest.tickets() <= 0) {
            throw new InvalidPurchaseException("Minimum of 1 ticket required");
        }
        if (ticketRequest.tickets() > 25) {
            throw new InvalidPurchaseException("Maximum of 25 tickets");
        }
        if (ticketRequest.adults() == 0) {
            throw new InvalidPurchaseException(
                    "Adult ticket required in order to buy child or infant tickets");
        }
        // Infants sit on the laps of an adult instead of having their own seat
        if (ticketRequest.infants() > ticketRequest.adults()) {
            throw new InvalidPurchaseException(
                    "One adult ticket is required for each infant ticket");
        }
    }

}
