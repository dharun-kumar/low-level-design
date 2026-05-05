package observer;

import entities.Elevator;

public class Display implements Observer {
    @Override
    public void update(Elevator elevator) {
        System.out.println("[DISPLAY] Elevator " + elevator.getElevatorID() +
                " | Current Floor: " + elevator.getCurrentFloor() +
                " | Direction: " + elevator.getState().getDirection());
    }
}
