package enums;

import java.time.LocalTime;

public enum ShowTime {

    MORNING(LocalTime.of(10, 30)),
    AFTERNOON(LocalTime.of(14, 30)),
    EVENING(LocalTime.of(18, 30)),
    NIGHT(LocalTime.of(22, 30));

    private final LocalTime showTime;

    ShowTime(LocalTime time) {
        this.showTime = time;
    }

    public LocalTime getTime() {
        return showTime;
    }
}
