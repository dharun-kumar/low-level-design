interface Laptop {
    String getDescription();
    double getPrice();
}

class Macbook implements Laptop {

    @Override
    public String getDescription() {
        return "Macbook M2 -> One Year Limited Warranty ";
    }

    @Override
    public double getPrice() {
        return 69000;
    }
}

abstract class LaptopDecorator implements Laptop {
    Laptop decoratedLaptop;

    public LaptopDecorator(Laptop laptop) {
        this.decoratedLaptop = laptop;
    }
}

class ExtendedWarrantyLaptop extends LaptopDecorator {

    public ExtendedWarrantyLaptop(Laptop laptop) {
        super(laptop);
    }

    @Override
    public String getDescription() {
        return decoratedLaptop.getDescription() + " + 3 Years Extended Warranty ";
    }

    @Override
    public double getPrice() {
        return decoratedLaptop.getPrice() + 1499;
    }
}

class CompleteProtectionLaptop extends LaptopDecorator {

    public CompleteProtectionLaptop(Laptop laptop) {
        super(laptop);
    }

    @Override
    public String getDescription() {
        return decoratedLaptop.getDescription() + " + One Year Complete Protection";
    }

    @Override
    public double getPrice() {
        return decoratedLaptop.getPrice() + 999;
    }
}

public class DecoratorDemo {
    public static void main(String[] args) {
        Laptop macbook = new Macbook();
        System.out.println("Price : " + macbook.getPrice());
        System.out.println("Description: " + macbook.getDescription() + "\n");

        Laptop extWarrantyLaptop = new ExtendedWarrantyLaptop(macbook);
        System.out.println("Price : " + extWarrantyLaptop.getPrice());
        System.out.println("Description: " + extWarrantyLaptop.getDescription() + "\n");

        Laptop protectedLaptop = new CompleteProtectionLaptop(extWarrantyLaptop);
        System.out.println("Price : " + protectedLaptop.getPrice());
        System.out.println("Description: " + protectedLaptop.getDescription() + "\n");
    }
}