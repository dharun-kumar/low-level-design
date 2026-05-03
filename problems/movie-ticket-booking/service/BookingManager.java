package service;

import entities.*;
import enums.PaymentStatus;
import strategy.payment.PaymentStrategy;
import java.util.Set;
import java.util.concurrent.*;

public class BookingManager {

    private final ExecutorService executorService;

    public BookingManager() {
        this.executorService = Executors.newFixedThreadPool(1);
    }

    public CompletableFuture<Ticket> processBooking(User user, Show show, Set<Seat> seats, PaymentStrategy paymentStrategy) {

        lockSeats(show, seats);

        double totalPrice = show.getPriceStrategy().calculatePrice(seats);

        return CompletableFuture.supplyAsync(() ->
                        paymentStrategy.pay(totalPrice), executorService)
                .thenApply(payment -> {              // Use thenApply to return a value
                    if(payment.getStatus() == PaymentStatus.SUCCESS) {
                        confirmBooking(show, seats);
                        return new Ticket(user, show, seats, payment);
                    } else {
                        releaseSeats(show, seats);
                        return null;
                    }
                })
                .orTimeout(600, TimeUnit.SECONDS)
                .exceptionally(e -> {
                    releaseSeats(show, seats);
                    System.err.println("Exception while processing payment : " + e.getMessage());
                    return null;
                });
    }

    public void lockSeats(Show show, Set<Seat> seats) {
        seats.forEach(Seat::lock);
    }

    public void confirmBooking(Show show, Set<Seat> seats) {
        seats.forEach(Seat::book);
    }

    public void releaseSeats(Show show, Set<Seat> seats) {
        seats.forEach(Seat::release);
    }

    public void shutdown() {
        executorService.shutdown();
        try {
            if(!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
            // When InterruptedException is caught, the thread's interrupted status is reset to false.
            // We call interrupt() to restore that status to true, signaling to the ExecutorService that this thread should indeed stop.
        }
    }

}
