package state;

import entities.Elevator;
import entities.Request;
import enums.Direction;

public abstract class State {

    public abstract Direction getDirection();

    public abstract void move(Elevator elevator);

    /**
        Default routing by floor position — used for INTERNAL requests (passenger already inside).
        Floor above current → upRequests; floor below → downRequests.
        EXTERNAL requests override this in MovingUpState/MovingDownState to factor in passenger's intended direction.
    **/
    public void addRequest(Elevator elevator, Request request) {
        if(request.getTargetFloor() > elevator.getCurrentFloor()) {
            elevator.getUpRequests().add(request.getTargetFloor());
        } else {
            elevator.getDownRequests().add(request.getTargetFloor());
        }
    }

}
