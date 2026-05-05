package service;

import entities.Elevator;
import entities.Request;
import enums.Direction;
import enums.RequestSource;
import observer.Display;
import observer.Observer;
import strategy.ElevatorSelectionStrategy;
import strategy.NearestElevationStrategy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ElevatorService {

    private static ElevatorService instance;

    private final Map<Integer, Elevator> elevators;
    private final ExecutorService executorService;
    private final ElevatorSelectionStrategy selectionStrategy;

    private ElevatorService(int noOfElevators) {
        executorService = Executors.newFixedThreadPool(noOfElevators);
        selectionStrategy = new NearestElevationStrategy();

        elevators = new ConcurrentHashMap<>();

        Observer elevatorDisplay = new Display(); // Create the observer

        for(int i=1; i<=noOfElevators; i++) {
            Elevator elevator = new Elevator(i);
            elevators.put(i, elevator);
            elevator.addObserver(elevatorDisplay);
        }
    }

    public static synchronized ElevatorService getInstance(int noOfElevators) {
        if(instance == null) {
            instance = new ElevatorService(noOfElevators);
        }
        return instance;
    }

    public synchronized void requestElevator(int floor, Direction direction) {
        Request request = new Request(floor, direction, RequestSource.EXTERNAL);
        selectionStrategy.selectElevator(elevators.values(), request).ifPresent(elevator -> elevator.addRequest(request));
    }

    public synchronized void selectFloor(int elevatorID, int destinationFloor) {
        Request request = new Request(destinationFloor, Direction.IDLE, RequestSource.INTERNAL);
        Elevator elevator = elevators.get(elevatorID);
        if (elevator != null) {
            elevator.addRequest(request);
        }
    }

    public synchronized void start() {
        for(Elevator elevator : elevators.values()) {
            executorService.submit(elevator);
        }
    }

    public synchronized void shutdown()  {
        elevators.values().forEach(Elevator::stopElevator);
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdown();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

}
