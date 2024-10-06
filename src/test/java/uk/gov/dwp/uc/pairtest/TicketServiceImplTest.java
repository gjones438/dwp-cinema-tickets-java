package uk.gov.dwp.uc.pairtest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketType;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

class TicketServiceImplTest {
    static final long ACCOUNT_ID = 23;

    @Mock
    private TicketPaymentService ticketPaymentService;

    @Mock
    private SeatReservationService seatReservationService;

    @InjectMocks
    private TicketServiceImpl ticketService;

    @ParameterizedTest(
            name = "{0} adults {1} children {2} infants should need {4} seats costing £{3}")
    @MethodSource("purchaseTicketsSuccessArgs")
    void purchaseTicketsSuccess(int adults, int children, int infants, int expectedCost,
            int expectedSeats) {
        var adultsRequest = new TicketTypeRequest(TicketType.ADULT, adults);
        var childrenRequest = new TicketTypeRequest(TicketType.CHILD, children);
        var infantsRequest = new TicketTypeRequest(TicketType.INFANT, infants);
        ticketService.purchaseTickets(ACCOUNT_ID, childrenRequest, adultsRequest, infantsRequest);

        verify(ticketPaymentService).makePayment(ACCOUNT_ID, expectedCost);
        verify(seatReservationService).reserveSeat(ACCOUNT_ID, expectedSeats);
    }

    static Stream<Arguments> purchaseTicketsSuccessArgs() {
        return Stream.of( //
                Arguments.of(1, 2, 0, 55, 3), //
                Arguments.of(1, 0, 1, 25, 1), //
                Arguments.of(4, 2, 3, 130, 6), //
                Arguments.of(1, 5, 0, 100, 6), //
                Arguments.of(1, 1, 0, 40, 2), //
                Arguments.of(10, 15, 0, 475, 25), //
                Arguments.of(25, 0, 25, 625, 25) //
        );
    }

    @Test
    void purchaseTicketsAccountIdRequired() {
        var ticketRequest = new TicketTypeRequest(TicketType.ADULT, 2);
        var e = assertThrows(InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(null, ticketRequest));
        assertEquals("accountId required", e.getMessage());
    }

    @Test
    void purchaseTicketsAccountIdZeroInvalid() {
        var ticketRequest = new TicketTypeRequest(TicketType.ADULT, 2);
        var e = assertThrows(InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(0L, ticketRequest));
        assertEquals("Invalid accountId", e.getMessage());
    }

    @Test
    void purchaseTicketsAccountIdNegativeInvalid() {
        var ticketRequest = new TicketTypeRequest(TicketType.ADULT, 2);
        var e = assertThrows(InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(-7L, ticketRequest));
        assertEquals("Invalid accountId", e.getMessage());
    }

    @Test
    void purchaseTicketsMinimun1Seat() {
        var ticketRequest = new TicketTypeRequest(TicketType.ADULT, 0);
        var e = assertThrows(InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(ACCOUNT_ID, ticketRequest));
        assertEquals("Minimun of 1 seat required", e.getMessage());
    }

    @Test
    void purchaseTicketsMaximum25Seats() {
        var adultRequest = new TicketTypeRequest(TicketType.ADULT, 20);
        var childRequest = new TicketTypeRequest(TicketType.CHILD, 6);
        var e = assertThrows(InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(ACCOUNT_ID, adultRequest, childRequest));
        assertEquals("Maximum of 25 seats", e.getMessage());
    }

    @Test
    void purchaseTicketsAdultRequiredForChildTickets() {
        var ticketRequest = new TicketTypeRequest(TicketType.CHILD, 1);
        var e = assertThrows(InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(ACCOUNT_ID, ticketRequest));
        assertEquals("Adult ticket required in order to buy child or infant tickets",
                e.getMessage());
    }

    @Test
    void purchaseTicketsAdultRequiredForInfantTickets() {
        var ticketRequest = new TicketTypeRequest(TicketType.INFANT, 1);
        var e = assertThrows(InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(ACCOUNT_ID, ticketRequest));
        assertEquals("Adult ticket required in order to buy child or infant tickets",
                e.getMessage());
    }

    @Test
    void purchaseTicketsAtLeastAsManyAdultAsInfantTickets() {
        var adultRequest = new TicketTypeRequest(TicketType.ADULT, 3);
        var infantRequest = new TicketTypeRequest(TicketType.INFANT, 4);
        var e = assertThrows(InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(ACCOUNT_ID, adultRequest, infantRequest));
        assertEquals("One adult ticket is required for each infant ticket", e.getMessage());
    }

}