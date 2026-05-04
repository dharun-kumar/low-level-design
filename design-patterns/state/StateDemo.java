class Monitor {
    private Setting setting;

    public Monitor() {
        this.setting = Brightness.getInstance();
    }

    public void menuButton() {
        setting.next(this);
    }

    public void upButton() {
        setting.increase();
    }

    public void setSetting(Setting setting) {
        this.setting = setting;
    }
}

interface Setting {
    void next(Monitor monitor);
    void increase();
}

class Brightness implements Setting {

    private static final Brightness INSTANCE = new Brightness();
    private int value = 50;

    public Brightness() { }

    public static Brightness getInstance() {
        System.out.println("Switching to Brightness ... ");
        return INSTANCE;
    }

    @Override
    public void next(Monitor monitor) {
        monitor.setSetting(Contrast.getInstance());
    }

    @Override
    public void increase() {
        value++;
        System.out.println("Brightness increased to " + value);
    }
}

class Contrast implements Setting {

    private static final Contrast INSTANCE = new Contrast();
    private int value = 50;

    public Contrast() { }

    public static Contrast getInstance() {
        System.out.println("Switching to Contrast ... ");
        return INSTANCE;
    }

    @Override
    public void next(Monitor monitor) {
        monitor.setSetting(Sharpness.getInstance());
    }

    @Override
    public void increase() {
        value++;
        System.out.println("Contrast increased to " + value);
    }
}

class Sharpness implements Setting {

    private static final Sharpness INSTANCE = new Sharpness();
    private int value = 50;

    public Sharpness() { }

    public static Sharpness getInstance() {
        System.out.println("Switching to Sharpness ... ");
        return INSTANCE;
    }

    @Override
    public void next(Monitor monitor) {
        monitor.setSetting(Brightness.getInstance());
    }

    @Override
    public void increase() {
        value++;
        System.out.println("Sharpness increased to " + value);
    }
}

public class StateDemo {
    public static void main(String[] args) {
        Monitor monitor = new Monitor();
        monitor.menuButton();
        monitor.menuButton();
        monitor.menuButton();

        monitor.upButton();
    }
}