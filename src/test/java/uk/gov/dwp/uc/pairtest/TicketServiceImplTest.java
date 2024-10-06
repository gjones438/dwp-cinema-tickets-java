package uk.gov.dwp.uc.pairtest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketType;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

@ExtendWith(MockitoExtension.class)
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
        return Stream.of(
                Arguments.of(1, 2, 0, 55, 3),
                Arguments.of(1, 0, 1, 25, 1),
                Arguments.of(4, 2, 3, 130, 6),
                Arguments.of(1, 5, 0, 100, 6),
                Arguments.of(1, 1, 0, 40, 2),
                Arguments.of(10, 15, 0, 475, 25),
                Arguments.of(20, 0, 5, 500, 20)
        );
    }

    @ParameterizedTest
    @MethodSource("purchaseTicketsSuccessDuplicatesArgs")
    void purchaseTicketsSuccessDuplicates(PurchaseTicketsTestArgs args) {
        ticketService.purchaseTickets(ACCOUNT_ID, args.ticketRequests());

        verify(ticketPaymentService).makePayment(ACCOUNT_ID, args.expectedCost());
        verify(seatReservationService).reserveSeat(ACCOUNT_ID, args.expectedSeats());
    }

    static Stream<PurchaseTicketsTestArgs> purchaseTicketsSuccessDuplicatesArgs() {
        return Stream.of(
                new PurchaseTicketsTestArgs(85, 5, new TicketTypeRequest(TicketType.CHILD, 2),
                        new TicketTypeRequest(TicketType.ADULT, 1),
                        new TicketTypeRequest(TicketType.CHILD, 2)),
                new PurchaseTicketsTestArgs(175, 7, new TicketTypeRequest(TicketType.ADULT, 2),
                        new TicketTypeRequest(TicketType.ADULT, 5)),
                new PurchaseTicketsTestArgs(190, 10, new TicketTypeRequest(TicketType.INFANT, 2),
                        new TicketTypeRequest(TicketType.CHILD, 3),
                        new TicketTypeRequest(TicketType.INFANT, 1),
                        new TicketTypeRequest(TicketType.ADULT, 2),
                        new TicketTypeRequest(TicketType.ADULT, 2),
                        new TicketTypeRequest(TicketType.CHILD, 2),
                        new TicketTypeRequest(TicketType.CHILD, 1))
        );
    }

    private record PurchaseTicketsTestArgs(int expectedCost, int expectedSeats,
            TicketTypeRequest... ticketRequests) {
        @Override
        public String toString() {
            var ticketRequestStr = Stream.of(ticketRequests)
                    .map(r -> "%s=%s".formatted(r.ticketType(), r.numberOfTickets()))
                    .collect(Collectors.joining(", "));
            return "%s expect %s seats costing £%s".formatted(ticketRequestStr, expectedSeats,
                    expectedCost);
        }
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
        assertEquals("Minimum of 1 ticket required", e.getMessage());
    }

    @Test
    void purchaseTicketsMaximum25Seats() {
        var adultRequest = new TicketTypeRequest(TicketType.ADULT, 20);
        var childRequest = new TicketTypeRequest(TicketType.CHILD, 3);
        var infantRequest = new TicketTypeRequest(TicketType.INFANT, 3);
        var e = assertThrows(InvalidPurchaseException.class, () -> ticketService
                .purchaseTickets(ACCOUNT_ID, adultRequest, childRequest, infantRequest));
        assertEquals("Maximum of 25 tickets", e.getMessage());
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
