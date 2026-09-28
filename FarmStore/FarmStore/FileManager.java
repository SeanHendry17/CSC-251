import java.io.*;
import java.time.LocalDate;
import java.util.*;

/** Reads and writes the store's data as CSV files in the "data" folder. */
public class FileManager {
    private static final String DIR = "data";
    private static final String ITEMS = DIR + "/items.csv";
    private static final String ANIMALS = DIR + "/animals.csv";
    private static final String SERVICES = DIR + "/services.csv";
    private static final String SALES = DIR + "/sales.csv";

    public static void saveItems(List<Item> items) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Item i : items) lines.add(i.toCsv());
        write(ITEMS, "name,price,quantity", lines);
    }

    public static void saveAnimals(List<Animal> animals) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Animal a : animals) lines.add(a.toCsv());
        write(ANIMALS, "species,age,price,specialty,breeder,sold", lines);
    }

    public static void saveServices(List<Service> services) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Service s : services) lines.add(s.toCsv());
        write(SERVICES, "type,date,price,paid", lines);
    }

    public static List<Item> loadItems() {
        List<Item> list = new ArrayList<>();
        for (String line : read(ITEMS)) {
            try { list.add(Item.fromCsv(line)); } catch (RuntimeException e) { /* skip bad row */ }
        }
        return list;
    }

    public static List<Animal> loadAnimals() {
        List<Animal> list = new ArrayList<>();
        for (String line : read(ANIMALS)) {
            try { list.add(Animal.fromCsv(line)); } catch (RuntimeException e) { /* skip bad row */ }
        }
        return list;
    }

    public static List<Service> loadServices() {
        List<Service> list = new ArrayList<>();
        for (String line : read(SERVICES)) {
            try { list.add(Service.fromCsv(line)); } catch (RuntimeException e) { /* skip bad row */ }
        }
        return list;
    }

    /** Appends one transaction to sales.csv (creates the file with a header if needed). */
    public static void logSale(String description, double total) throws IOException {
        new File(DIR).mkdirs();
        File f = new File(SALES);
        boolean isNew = !f.exists();
        try (PrintWriter out = new PrintWriter(new FileWriter(f, true))) {
            if (isNew) out.println("date,description,total");
            out.println(LocalDate.now() + "," + description.replace(",", " ") + "," + total);
        }
    }

    public static List<String> readSales() {
        return read(SALES);
    }

    private static void write(String path, String header, List<String> lines) throws IOException {
        new File(DIR).mkdirs();
        try (PrintWriter out = new PrintWriter(new FileWriter(path))) {
            out.println(header);
            for (String line : lines) out.println(line);
        }
    }

    /** Returns data lines (header skipped). A missing file gives an empty list. */
    private static List<String> read(String path) {
        List<String> lines = new ArrayList<>();
        File f = new File(path);
        if (!f.exists()) return lines;
        try (BufferedReader in = new BufferedReader(new FileReader(f))) {
            in.readLine();
            String line;
            while ((line = in.readLine()) != null) {
                if (!line.trim().isEmpty()) lines.add(line);
            }
        } catch (IOException e) {
            System.err.println("Could not read " + path + ": " + e.getMessage());
        }
        return lines;
    }
}
