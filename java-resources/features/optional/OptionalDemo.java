import java.util.Optional;

public class OptionalDemo {

    public static void main(String[] args) {

        Optional<String> name = Optional.ofNullable(getName());

        name.ifPresent(System.out::println);

        String nameUC = name.map(String::toUpperCase)
                .orElse("DEFAULT");

        System.out.println(nameUC);

    }

    public static String getName() {
        return null;
    }

}
