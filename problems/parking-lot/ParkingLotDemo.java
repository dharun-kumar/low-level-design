import service.ParkingLotService;
import entities.ParkingTicket;
import vehicle.Car;
import vehicle.Vehicle;

import java.util.Optional;

public class ParkingLotDemo {

    public static void main(String[] args) throws InterruptedException {
        ParkingLotService parkingLotService = ParkingLotService.getInstance();
        parkingLotService.setCapacity(1, 2, 5, 1);

        Vehicle car1 = new Car("TN 88 AB 1234");
        Optional<ParkingTicket> car1Ticket = parkingLotService.checkIn(car1);
        car1Ticket.ifPresent(ticket -> System.out.println("Vehicle " + ticket.getVehicle().getVehicleNumber() + " parked in floor " + ticket.getParkedFloor()));

        Vehicle car2 = new Car("TN 19 AB 7887");
        Optional<ParkingTicket> car2Ticket = parkingLotService.checkIn(car2);
        car2Ticket.ifPresent(ticket -> System.out.println("Vehicle " + ticket.getVehicle().getVehicleNumber() + " parked in floor " + ticket.getParkedFloor()));

        Vehicle car3 = new Car("TN 01 A 8055");
        Optional<ParkingTicket> car3Ticket = parkingLotService.checkIn(car3);
        car3Ticket.ifPresent(ticket -> System.out.println("Vehicle " + ticket.getVehicle().getVehicleNumber() + " parked in floor " + ticket.getParkedFloor()));

        //for testing
        Thread.sleep(5000);

        if(car1Ticket.isPresent()) {
            parkingLotService.checkOut(car1Ticket.get());
            Optional<Double> fare = car1Ticket.get().getFare();
            fare.ifPresent(amount -> System.out.println("Fare for vehicle " + car1Ticket.get().getVehicle().getVehicleNumber() + " : " + amount));
        }

        car3Ticket = parkingLotService.checkIn(car3);
        car3Ticket.ifPresent(ticket -> System.out.println("Vehicle " + ticket.getVehicle().getVehicleNumber() + " parked in floor " + ticket.getParkedFloor()));

    }

}
