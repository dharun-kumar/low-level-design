package state;

import entities.Elevator;
import entities.Request;
import enums.Direction;
import enums.RequestSource;

public class MovingUpState extends State {

    /**
        Increments floor each tick toward the next upRequests target.
        Stops and dequeues when currentFloor matches nextStop.
        Transitions to IdleState when upRequests is exhausted.
     **/
    @Override
    public void move(Elevator elevator) {
        if(elevator.getUpRequests().isEmpty()) {
            elevator.setState(new IdleState());
        } else  {
            Integer nextStop = elevator.getUpRequests().first();
            elevator.setCurrentFloor(elevator.getCurrentFloor() + 1); //moving upwards

            if(elevator.getCurrentFloor() == nextStop) {
                System.out.println("Elevator " + elevator.getElevatorID() + " stopped at floor " + nextStop);
                elevator.getUpRequests().pollFirst();
            }

            if(elevator.getUpRequests().isEmpty()) {
                elevator.setState(new IdleState());
            }
        }
    }

    /**
        EXTERNAL: routes by passenger's intended direction, not floor position.
        UP → upRequests (ahead: serve this trip; behind: serve next upward sweep).
        DOWN → downRequests (defer to return trip — avoids passenger boarding going the wrong way).
    **/
    @Override
    public void addRequest(Elevator elevator, Request request) {
        if(request.getSource() == RequestSource.INTERNAL) {
            super.addRequest(elevator, request);
        } else {
            //prioritize up requests over down requests
            if(request.getDirection() == Direction.UP && request.getTargetFloor() >= elevator.getCurrentFloor()) {
                elevator.getUpRequests().add(request.getTargetFloor());
            } else if(request.getDirection() == Direction.DOWN) {
                elevator.getDownRequests().add(request.getTargetFloor());
            }
        }
    }

    @Override
    public Direction getDirection() {
        return Direction.UP;
    }
}
