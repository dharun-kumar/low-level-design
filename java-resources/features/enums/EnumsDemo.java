enum Level {
    HIGH, MEDIUM, LOW
}

enum Status {
    RUNNING("Running"), COMPLETED("Completed"), STOPPED("Stopped");

    private String state;

    Status(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }

}

public class EnumsDemo {

    public static void main(String[] args) {

        Level level = Level.HIGH;
        System.out.println(level);

        Status status = Status.RUNNING;
        System.out.println(status.getState());



    }

}
