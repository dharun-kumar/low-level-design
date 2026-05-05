package entities;

import observer.Observer;
import state.IdleState;
import state.State;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;

public class Elevator implements Runnable {

    private final int elevatorID;
    private final AtomicInteger currentFloor;
    private volatile State state;
    private volatile boolean isRunning;

    private final ConcurrentSkipListSet<Integer> upRequests;
    private final ConcurrentSkipListSet<Integer> downRequests;

    private final List<Observer> observers;

    public Elevator(int elevatorID) {
        this.elevatorID = elevatorID;
        this.currentFloor = new AtomicInteger(0);
        this.state = new IdleState();
        this.isRunning = true;

        this.upRequests = new ConcurrentSkipListSet<>();
        this.downRequests = new ConcurrentSkipListSet<>(Comparator.reverseOrder());  //store in descending order

        this.observers = new ArrayList<>();
    }

    public void addRequest(Request request) {
        state.addRequest(this, request);
    }

    public void move() {
        state.move(this);
    }

    @Override
    public void run() {
        while(isRunning) {
            move();
            try {
                Thread.sleep(500);     //simulate test
            } catch (InterruptedException e) {
                isRunning = false;
                Thread.currentThread().interrupt();
            }
        }
    }

    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    public void notifyObservers() {
        observers.forEach(observer -> observer.update(this));
    }

    public void setState(State state) {
        this.state = state;
        notifyObservers();  //Notify observers on direction change
    }

    public void setCurrentFloor(Integer currentFloor) {
        this.currentFloor.set(currentFloor);
    }

    public int getCurrentFloor() {
        return currentFloor.get();
    }

    public State getState() {
        return state;
    }

    public void stopElevator() {
        isRunning = false;
    }

    public ConcurrentSkipListSet<Integer> getUpRequests() {
        return upRequests;
    }

    public ConcurrentSkipListSet<Integer> getDownRequests() {
        return downRequests;
    }

    public int getElevatorID() {
        return elevatorID;
    }
}
