package ticket;

import vehicle.Vehicle;
import vehicle.VehicleStatus;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class ParkingTicket {

    private final Vehicle vehicle;
    private final long entryTimeStamp;
    private final int parkedFloor;
    private double fare;

    public ParkingTicket(Vehicle vehicle, int floor) {
        this.vehicle = vehicle;
        this.entryTimeStamp = Instant.now().getEpochSecond();
        this.parkedFloor = floor;
    }

    public void calculateFare() {
        double parkedHours = (Instant.now().getEpochSecond() - entryTimeStamp) / (double)TimeUnit.HOURS.toSeconds(1);
        fare = vehicle.getFarePerHour() * Math.ceil(parkedHours);
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public int getParkedFloor() {
        return parkedFloor;
    }

    public Optional<Double> getFare() {
        if(vehicle.getStatus().equals(VehicleStatus.CHECKED_OUT)) {
            return Optional.of(fare);
        } return Optional.empty();
    }

}
