public class Item {
    private final String name;
    private final double price;
    private int quantity;

    public Item(String name, double price, int quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String toCsv() { return name + "," + price + "," + quantity; }

    public static Item fromCsv(String line) {
        String[] p = line.split(",", -1);
        return new Item(p[0], Double.parseDouble(p[1]), Integer.parseInt(p[2]));
    }

}
