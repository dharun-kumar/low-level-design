package floor;

import vehicle.Vehicle;
import vehicle.VehicleType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ParkingFloor {

    private final int floorNumber;
    private final Map<VehicleType, Integer> totalSpots;
    private final Map<VehicleType, Integer> availableSpots;

    public ParkingFloor(int floorNumber, int carSpots, int bikeSpots, int truckSpots) {
        this.floorNumber = floorNumber;
        this.totalSpots = new ConcurrentHashMap<>();
        this.availableSpots = new ConcurrentHashMap<>();

        this.totalSpots.put(VehicleType.CAR, carSpots);
        this.totalSpots.put(VehicleType.BIKE, bikeSpots);
        this.totalSpots.put(VehicleType.TRUCK, truckSpots);

        this.availableSpots.putAll(totalSpots);
    }

    public boolean parkVehicle(Vehicle vehicle) {
        if(availableSpots.get(vehicle.getVehicleType()) > 0) {
            availableSpots.compute(vehicle.getVehicleType(), ((type, spots) -> spots - 1));
            return true;
        }
        return false;
    }

    public void freeVehicleSpot(Vehicle vehicle) {
        availableSpots.computeIfPresent(vehicle.getVehicleType(), ((type, spots) -> spots + 1));
    }

    public int getFloorNumber() {
        return floorNumber;
    }

}
