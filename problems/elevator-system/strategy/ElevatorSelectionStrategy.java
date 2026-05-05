package strategy;

import entities.Elevator;
import entities.Request;
import java.util.Collection;
import java.util.Optional;

public interface ElevatorSelectionStrategy {
    Optional<Elevator> selectElevator(Collection<Elevator> elevators, Request request);
}
