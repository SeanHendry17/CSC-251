public class Animal {
    private final String species;
    private final String age;
    private final double price;
    private final boolean specialty;
    private final String breeder; // empty when raised on the farm
    private boolean sold;

    public Animal(String species, String age, double price, boolean specialty, String breeder, boolean sold) {
        this.species = species;
        this.age = age;
        this.price = price;
        this.specialty = specialty;
        this.breeder = breeder;
        this.sold = sold;
    }

    public String getSpecies() { return species; }
    public String getAge() { return age; }
    public String getBreeder() { return breeder; }
    public boolean isSpecialty() { return specialty; }
    public double getPrice() { return price; }
    public boolean isSold() { return sold; }
    public void setSold(boolean sold) { this.sold = sold; }

    public String toCsv() {
        return species + "," + age + "," + price + "," + specialty + "," + breeder + "," + sold;
    }

    public static Animal fromCsv(String line) {
        String[] p = line.split(",", -1);
        return new Animal(p[0], p[1], Double.parseDouble(p[2]),
                Boolean.parseBoolean(p[3]), p[4], Boolean.parseBoolean(p[5]));
    }

}
