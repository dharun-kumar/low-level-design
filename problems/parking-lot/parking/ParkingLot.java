package parking;

import floor.ParkingFloor;
import ticket.ParkingTicket;
import vehicle.Vehicle;
import vehicle.VehicleStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ParkingLot {

    private final List<ParkingFloor> parkingFloors;
    private static volatile ParkingLot INSTANCE;

    private ParkingLot() {
        this.parkingFloors = new ArrayList<>();
    }

    public static ParkingLot getInstance() {
        if(INSTANCE == null) {
            synchronized (ParkingLot.class) {
                if(INSTANCE == null) {
                    INSTANCE = new ParkingLot();
                }
            }
        }
        return INSTANCE;
    }

    public void setCapacity(int floors, int carCapacityPerFloor, int bikeCapacityPerFloor, int truckCapacityPerFloor) {
        if(!parkingFloors.isEmpty()) {
            throw new IllegalStateException("ParkingLot is already configured");
        }
        for(int floorNumber=0; floorNumber<floors; floorNumber++) {
            parkingFloors.add(new ParkingFloor(floorNumber, carCapacityPerFloor, bikeCapacityPerFloor, truckCapacityPerFloor));
        }
    }

    public synchronized Optional<ParkingTicket> checkIn(Vehicle vehicle) {
        for(ParkingFloor parkingFloor : parkingFloors) {
            if(parkingFloor.parkVehicle(vehicle)) {
                vehicle.setStatus(VehicleStatus.PARKED);
                return Optional.of(new ParkingTicket(vehicle, parkingFloor.getFloorNumber()));
            }
        }
        return Optional.empty();
    }

    public synchronized void checkOut(ParkingTicket parkingTicket) {
        parkingFloors.get(parkingTicket.getParkedFloor()).freeVehicleSpot(parkingTicket.getVehicle());
        parkingTicket.calculateFare();
        parkingTicket.getVehicle().setStatus(VehicleStatus.CHECKED_OUT);
    }

}
