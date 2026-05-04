import java.util.*;
import java.util.function.*;

class User {

    private String name;
    private int age;

    public User(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    @Override
    public String toString(){
        return name + " (" + age + ")";
    }
}

public class FIBuiltIn {

    public static void main(String[] args) {

        List<User> users = Arrays.asList(new User("Alice", 25), new User("Bob", 16), new User("Charlie", 30));

        // filter users who are adults (18+)
        Predicate<User> isAdult = user -> user.getAge() >= 18;

        // transform user name to uppercase
        Function<User, String> getNameUC = user -> user.getName().toUpperCase();

        // print user name
        Consumer<String> printUser = System.out::println;

        // provide default user object
        Supplier<User> defaultUser = () -> new User("default", -1);

        System.out.println("Adult users : " );

        // filter adults, get names in uppercase & print each
        users.stream()
                .filter(isAdult)
                .map(getNameUC)
                .forEach(printUser);

        // filter non-adults, return any oen user & default user if none
        User nonAdultUser = users.stream()
                .filter(isAdult.negate())
                .findAny()
                .orElseGet(defaultUser);

        System.out.println("Non-adult users : " + nonAdultUser);
    }

}