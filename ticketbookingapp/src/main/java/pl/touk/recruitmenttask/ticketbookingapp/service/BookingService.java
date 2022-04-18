package pl.touk.recruitmenttask.ticketbookingapp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.touk.recruitmenttask.ticketbookingapp.exception.ResourceNotFoundException;
import pl.touk.recruitmenttask.ticketbookingapp.exception.TooLateException;
import pl.touk.recruitmenttask.ticketbookingapp.exception.WrongSeatException;
import pl.touk.recruitmenttask.ticketbookingapp.model.*;
import pl.touk.recruitmenttask.ticketbookingapp.repository.ReservationRepository;
import pl.touk.recruitmenttask.ticketbookingapp.repository.ScreeningRepository;
import pl.touk.recruitmenttask.ticketbookingapp.repository.SeatRepository;
import pl.touk.recruitmenttask.ticketbookingapp.repository.TicketRepository;
import pl.touk.recruitmenttask.ticketbookingapp.service.properties.PropertiesConfig;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final ScreeningRepository screeningRepository;
    private final SeatRepository seatRepository;
    private final ReservationRepository reservationRepository;
    private final TicketRepository ticketRepository;
    private final SeatService seatService;

    public Reservation makeReservation(int screeningId, String name, String surname, Map<Integer, TicketType> seatsType) {
        Reservation reservation = new Reservation();

        Screening screening;
        try {
            screening = screeningRepository.findById(screeningId).orElseThrow();
        }
        catch (NoSuchElementException e) {
            throw new ResourceNotFoundException("Screening Not Found");
        }

        LocalDateTime startTime = screening.getStartTime();
        if (startTime
                .minusMinutes(PropertiesConfig.expirationTimeMin)
                .isBefore(LocalDateTime.now())) {
            throw new TooLateException("It Is Too Late To Make A Reservation");
        }

        reservation.setScreening(screening);
        reservation.setName(name);
        reservation.setSurname(surname);

        List<Ticket> tickets = prepareTickets(reservation, seatsType);
        reservation.setTicket(tickets);

        reservationRepository.save(reservation);
        ticketRepository.saveAll(tickets);

        return reservation;
    }

    private List<Ticket> prepareTickets(Reservation reservation, Map<Integer, TicketType> seatsType) {
        Screening screening = reservation.getScreening();
        Integer[] seatIds = seatsType.keySet().toArray(new Integer[0]);
        List<Seat> seatList = seatRepository.findAllByIdIn(seatIds);

        if (seatList.isEmpty() || !seatService.validateSeats(screening, seatList)) {
            throw new WrongSeatException("Seat Not Found");
        }

        List<Ticket> tickets = new ArrayList<>();
        for (Seat seat : seatList) {
            Ticket ticket = new Ticket();
            TicketType ticketType = seatsType.get(seat.getId());

            ticket.setTicketType(ticketType);
            ticket.setReservation(reservation);
            ticket.setScreening(screening);
            ticket.setSeat(seat);

            tickets.add(ticket);
        }

        return tickets;
    }
}
