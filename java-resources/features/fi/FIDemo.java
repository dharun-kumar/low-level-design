@FunctionalInterface
interface Animal {
    String makeSound(String animal, String sound);
}

public class FIDemo {

    public static void main(String[] args) {

        //Anonymous class
        Animal cat = new Animal() {
            public String makeSound(String animal, String sound) {
                return animal + " sounds " + sound;
            }
        };

        cat.makeSound("cat", "meow");  //cat sounds meow

        //Lambda Expressions
        Animal dog = (animal, sound) -> animal + " sounds " + sound;
        dog.makeSound("dog", "bow");  //dog sounds bow

        Animal lion = FIDemo::soundRoars;
        lion.makeSound("lion", "roars");    //lion roars

    }

    public static String soundRoars(String animal, String sound) {
        return animal + " " + sound;
    }

}

