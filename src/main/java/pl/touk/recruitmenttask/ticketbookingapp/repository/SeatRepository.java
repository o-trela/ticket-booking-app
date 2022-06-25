package pl.touk.recruitmenttask.ticketbookingapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.touk.recruitmenttask.ticketbookingapp.model.Room;
import pl.touk.recruitmenttask.ticketbookingapp.model.Seat;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Integer> {
    List<Seat> findAllByIdIn(Integer[] ids);
    Seat findById(int id);
    Seat findByRoomAndRowNumAndSeatNum(Room room, int rowNum, int seatNum);
    List<Seat> findAllByRowNumAndRoom(int rowNum, Room room);
}