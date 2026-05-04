import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class Person {
    private String firstName;

    public Person(String firstName) {
        this.firstName = firstName;
    }

    private String getName(String lastName) {
        return firstName + " " + lastName;
    }
}

public class ReflectionDemo {

    public static void main(String[] args) {

        try {
            // 1. Accessing the class
            Class<?> clazz = Class.forName("com.reflection.Person");


            // 2. Creating new Instance Dynamically by using Constructor
            Constructor<?> constructor = clazz.getConstructor(String.class);
            Person person = (Person) constructor.newInstance("dharun");


            // 3. Invoking Methods Dynamically, Even private methods
            Method nameMethod = clazz.getDeclaredMethod("getName", String.class);
            nameMethod.setAccessible(true);
            String name = (String) nameMethod.invoke(person, "kumar");


            // 4. Accessing and Modifying Fields (both public & private)
            Field nameField = clazz.getDeclaredField("name");
            nameField.setAccessible(true);
            nameField.set(person, "Tony Stark");

        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException | NoSuchMethodException | InstantiationException |
                 InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}