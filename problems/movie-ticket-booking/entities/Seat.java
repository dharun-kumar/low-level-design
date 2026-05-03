package entities;

import enums.SeatState;
import enums.SeatType;
import exception.SeatUnavailableException;

public class Seat {

    private final String seatID;
    private final SeatType type;
    private volatile SeatState state;

    public Seat(String seatID, SeatType type) {
        this.seatID = seatID;
        this.type = type;
        this.state = SeatState.AVAILABLE;
    }

    public SeatState getState() {
        return state;
    }

    public SeatType getType() {
        return type;
    }

    public String getSeatID() {
        return seatID;
    }

    public void lock() {
        if(state == SeatState.AVAILABLE) {
            state = SeatState.LOCKED;
        } else {
            throw new SeatUnavailableException("Seat can't be locked !, Current status is " + state);
        }
    }

    public void book() {
        if(state == SeatState.LOCKED) {
            state = SeatState.BOOKED;
        } else {
            throw new SeatUnavailableException("Seat can't be Booked !, Current status is " + state);
        }
    }

    public void release() {
        if(state == SeatState.LOCKED) {
            state = SeatState.AVAILABLE;
        } else {
            throw new SeatUnavailableException("Seat can't be freed !, Current status is " + state);
        }
    }

}
