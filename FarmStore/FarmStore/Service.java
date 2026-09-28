public class Service {
    private final String type;
    private final String date;
    private final double price;
    private boolean paid;

    public Service(String type, String date, double price, boolean paid) {
        this.type = type;
        this.date = date;
        this.price = price;
        this.paid = paid;
    }

    public String getType() { return type; }
    public String getDate() { return date; }
    public double getPrice() { return price; }
    public boolean isPaid() { return paid; }
    public void setPaid(boolean paid) { this.paid = paid; }

    public String toCsv() { return type + "," + date + "," + price + "," + paid; }

    public static Service fromCsv(String line) {
        String[] p = line.split(",", -1);
        return new Service(p[0], p[1], Double.parseDouble(p[2]), Boolean.parseBoolean(p[3]));
    }

}
