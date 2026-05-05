package entities;

import enums.Direction;
import enums.RequestSource;

public class Request {

    private final int targetFloor;
    private final Direction direction;
    private final RequestSource requestSource;

    public Request(int targetFloor, Direction direction, RequestSource requestSource) {
        this.targetFloor = targetFloor;
        this.direction = direction;
        this.requestSource = requestSource;
    }

    public Direction getDirection() {
        return direction;
    }

    public int getTargetFloor() {
        return targetFloor;
    }

    public RequestSource getSource() {
        return requestSource;
    }
}
