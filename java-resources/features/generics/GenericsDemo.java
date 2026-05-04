class Box<T> {

    private T value;

    public void set(T value) {
        this.value = value;
    }

    public T get() {
        return value;
    }

}

public class GenericsDemo {


    // Upper bounded type parameter
    public static <T extends Number> void printNumber(T number) {
        System.out.println("Number: " + number);
    }

    public static void main(String[] args) {

        Box<String> stringBox = new Box<>();
        stringBox.set("Hello");
        System.out.println(stringBox.get());    //Hello

        Box<Integer> integerBox = new Box<>();
        integerBox.set(25);
        System.out.println(integerBox.get());   //25

        addBoxes(stringBox, integerBox);

        printIntegerBox(integerBox);

        printStringBox(stringBox);

    }

    public static void addBoxes(Box<?> box1, Box<?> box2) {
        System.out.println(box1.get() + " " + box2.get());
    }

    public static void printIntegerBox(Box<? super Integer> box) {
        System.out.println(box.get());
    }

    public static void printStringBox(Box<? extends String> box) {
        System.out.println(box.get());
    }

}
