package enums;

public enum SeatType {

    REGULAR(150),
    PREMIUM(200);

    private final double price;

    SeatType(double price) {
        this.price = price;
    }

    public double getPrice() {
        return price;
    }

}
