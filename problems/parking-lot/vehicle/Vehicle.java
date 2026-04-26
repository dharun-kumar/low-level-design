package vehicle;

public abstract class Vehicle {

    private final VehicleType vehicleType;
    private final String vehicleNumber;
    private final double farePerHour;
    private VehicleStatus status;

    public Vehicle(VehicleType vehicleType, String vehicleNumber, double farePerHour) {
        this.vehicleType = vehicleType;
        this.vehicleNumber = vehicleNumber;
        this.farePerHour = farePerHour;
        this.status = VehicleStatus.UNPARKED;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public double getFarePerHour() {
        return farePerHour;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public VehicleStatus getStatus() {
        return status;
    }

    public void setStatus(VehicleStatus status) {
        this.status = status;
    }
}
