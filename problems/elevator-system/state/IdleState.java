package state;

import entities.Elevator;
import enums.Direction;

public class IdleState extends State {

    /**
        Checks both queues and transitions to the appropriate moving state.
        upRequests is checked first — UP is prioritised when both queues are non-empty.
        No floor movement this tick; actual movement begins next tick after state transition.
    **/
    @Override
    public void move(Elevator elevator) {
        if(!elevator.getUpRequests().isEmpty()) {
            elevator.setState(new MovingUpState());
        } else if(!elevator.getDownRequests().isEmpty()) {
            elevator.setState(new MovingDownState());
        }
    }

    @Override
    public Direction getDirection() {
        return Direction.IDLE;
    }
}
