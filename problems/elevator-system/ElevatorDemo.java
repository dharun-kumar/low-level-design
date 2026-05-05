import enums.Direction;
import service.ElevatorService;

public class ElevatorDemo {

   public static void main(String[] args) throws InterruptedException {

       ElevatorService service = ElevatorService.getInstance(2);
       service.start();

       service.requestElevator(6, Direction.UP);
       Thread.sleep(100);

       service.selectFloor(1, 12);
       Thread.sleep(1000);

       service.requestElevator(3, Direction.DOWN);
       Thread.sleep(1000);

       service.selectFloor(2, 1);
       Thread.sleep(10000);

       service.shutdown();
    }

}
