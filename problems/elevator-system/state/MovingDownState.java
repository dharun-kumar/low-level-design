package state;

import entities.Elevator;
import entities.Request;
import enums.Direction;
import enums.RequestSource;

public class MovingDownState extends State {

    /**
     Decrements floor each tick toward the next downRequests target.
     Stops and dequeues when currentFloor matches nextStop.
     Transitions to IdleState when downRequests is exhausted.
     **/
    @Override
    public void move(Elevator elevator) {
        if(elevator.getDownRequests().isEmpty()) {
            elevator.setState(new IdleState());
        } else {
            Integer nextStop = elevator.getDownRequests().first();
            elevator.setCurrentFloor(elevator.getCurrentFloor() - 1);   //moving towards down

            if(elevator.getCurrentFloor() == nextStop) {
                System.out.println("Elevator " + elevator.getElevatorID() + " stopped at floor " + nextStop);
                elevator.getDownRequests().pollFirst();
            }

            if (elevator.getDownRequests().isEmpty()) {
                elevator.setState(new IdleState());
            }
        }
    }

    /**
     EXTERNAL: routes by passenger's intended direction, not floor position.
     DOWN → downRequests (ahead: serve this trip; behind: serve next downward sweep).
     UP → upRequests (defer to return trip — avoids passenger boarding going the wrong way).
     **/
    @Override
    public void addRequest(Elevator elevator, Request request) {
        if(request.getSource() == RequestSource.INTERNAL) {
            super.addRequest(elevator, request);
        } else  {
            //prioritize down requests over up requests
            if(request.getDirection() == Direction.DOWN && request.getTargetFloor() <= elevator.getCurrentFloor()) {
                elevator.getDownRequests().add(request.getTargetFloor());
            } else if(request.getDirection() == Direction.UP) {
                elevator.getUpRequests().add(request.getTargetFloor());
            }
        }
    }

    @Override
    public Direction getDirection() {
        return Direction.DOWN;
    }
}