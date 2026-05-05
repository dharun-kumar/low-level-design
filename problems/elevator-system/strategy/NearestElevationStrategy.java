package strategy;

import entities.Elevator;
import entities.Request;
import enums.Direction;
import java.util.Collection;
import java.util.Optional;

public class NearestElevationStrategy implements ElevatorSelectionStrategy {

    @Override
    public Optional<Elevator> selectElevator(Collection<Elevator> elevators, Request request) {
        Elevator nearestElevator = null;
        int minDistance = Integer.MAX_VALUE;

        for(Elevator elevator : elevators) {
            if(isSuitable(elevator, request)) {
                int distance = Math.abs(elevator.getCurrentFloor() - request.getTargetFloor());
                if(distance < minDistance) {
                    minDistance = distance;
                    nearestElevator = elevator;
                }
            }
        }
        return Optional.ofNullable(nearestElevator);
    }

    private boolean isSuitable(Elevator elevator, Request request) {
        if(elevator.getState().getDirection() == Direction.IDLE) {
            return true;
        }
        if(elevator.getState().getDirection() == request.getDirection()) {
            if(request.getDirection() == Direction.UP && elevator.getCurrentFloor() <= request.getTargetFloor()) {
                return true;
            } else if (request.getDirection() == Direction.DOWN && elevator.getCurrentFloor() >= request.getTargetFloor()) {
                return true;
            }
        }
        return false;
    }
}
