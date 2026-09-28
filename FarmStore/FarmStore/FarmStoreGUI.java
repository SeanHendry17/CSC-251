import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class FarmStoreGUI extends JFrame {
    private static final Color GREEN = new Color(46, 125, 50);
    private static final Color BARN = new Color(178, 34, 34);
    private static final Color CREAM = new Color(250, 246, 234);
    private static final Color STRIPE = new Color(240, 245, 236);
    private static final int LOW_STOCK = 5;
    private static final String[] SERVICE_TYPES = {
        "Checkup", "Vaccination", "Grooming", "Nail Trim", "Emergency Visit"
    };

    private final List<Item> items = FileManager.loadItems();
    private final List<Animal> animals = FileManager.loadAnimals();
    private final List<Service> services = FileManager.loadServices();

    private final DefaultTableModel itemModel = model("Item", "Price", "Stock", "Status");
    private final DefaultTableModel animalModel = model("Species", "Age", "Price", "Type", "Breeder", "Status");
    private final DefaultTableModel serviceModel = model("Service", "Date", "Price", "Status");
    private final DefaultTableModel salesModel = model("Date", "Description", "Total");
    private final JTable itemTable = table(itemModel);
    private final JTable animalTable = table(animalModel);
    private final JTable serviceTable = table(serviceModel);
    private final JTable salesTable = table(salesModel);
    private final JLabel[] stats = new JLabel[4];

    public FarmStoreGUI() {
        super("Farm Store Manager");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(CREAM);
        add(header(), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("SansSerif", Font.BOLD, 14));
        tabs.addTab("Store Items", tab(itemTable,
                button("Sell", GREEN, e -> sellItem()),
                button("Restock", GREEN, e -> restock()),
                button("Add New Item", BARN, e -> addItem())));
        tabs.addTab("Animals", tab(animalTable,
                button("Sell Selected", GREEN, e -> sellAnimal()),
                button("Add Animal", BARN, e -> addAnimal())));
        tabs.addTab("Services", tab(serviceTable,
                button("Schedule Service", GREEN, e -> scheduleService()),
                button("Mark Paid", BARN, e -> markPaid())));
        tabs.addTab("Sales History", tab(salesTable));
        add(tabs, BorderLayout.CENTER);

        refreshAll();
        setSize(920, 620);
        setLocationRelativeTo(null);
    }

    // ---------- Layout ----------

    private JPanel header() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(GREEN);
        p.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        JLabel title = new JLabel("Farm Store Manager");
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        title.setForeground(Color.WHITE);
        JPanel cards = new JPanel(new GridLayout(1, 4, 10, 0));
        cards.setOpaque(false);
        cards.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        String[] names = {"Revenue", "Animals Available", "Unpaid Services", "Low Stock Items"};
        for (int i = 0; i < names.length; i++) cards.add(card(i, names[i]));
        p.add(title, BorderLayout.NORTH);
        p.add(cards, BorderLayout.CENTER);
        return p;
    }

    private JPanel card(int index, String name) {
        JPanel c = new JPanel(new GridLayout(2, 1));
        c.setBackground(Color.WHITE);
        c.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        stats[index] = new JLabel("0");
        stats[index].setFont(new Font("SansSerif", Font.BOLD, 22));
        stats[index].setForeground(index < 2 ? GREEN : BARN);
        JLabel label = new JLabel(name);
        label.setForeground(Color.GRAY);
        c.add(stats[index]);
        c.add(label);
        return c;
    }

    private JPanel tab(JTable table, JButton... buttons) {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(CREAM);
        p.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        bar.setOpaque(false);
        for (JButton b : buttons) bar.add(b);
        p.add(bar, BorderLayout.SOUTH);
        return p;
    }

    private static JButton button(String text, Color color, ActionListener action) {
        JButton b = new JButton(text);
        b.setBackground(color);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setFocusPainted(false);
        b.addActionListener(action);
        return b;
    }

    private static DefaultTableModel model(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
    }

    private static JTable table(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setRowHeight(28);
        t.setFont(new Font("SansSerif", Font.PLAIN, 14));
        t.setShowGrid(false);
        t.setSelectionBackground(new Color(165, 214, 167));
        t.setSelectionForeground(Color.BLACK);
        t.setDefaultRenderer(Object.class, new StatusRenderer());
        t.getTableHeader().setBackground(GREEN);
        t.getTableHeader().setForeground(Color.WHITE);
        t.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        t.getTableHeader().setReorderingAllowed(false);
        return t;
    }

    /** Stripes rows and colors status words so problems (low stock, unpaid) stand out. */
    private static class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean selected,
                boolean focused, int row, int col) {
            Component c = super.getTableCellRendererComponent(t, value, selected, focused, row, col);
            if (!selected) {
                c.setBackground(row % 2 == 0 ? Color.WHITE : STRIPE);
                c.setForeground(colorFor(String.valueOf(value)));
            }
            return c;
        }

        private static Color colorFor(String text) {
            switch (text) {
                case "PAID": case "In stock": case "Available": return GREEN;
                case "UNPAID": case "OUT": return BARN;
                case "LOW": return new Color(230, 120, 0);
                case "Specialty": return new Color(106, 27, 154);
                case "Sold": return Color.GRAY;
                default: return Color.BLACK;
            }
        }
    }

    // ---------- Store items ----------

    private void sellItem() {
        int row = itemTable.getSelectedRow();
        if (row < 0) { info("Select an item first."); return; }
        Item item = items.get(row);
        int qty = askInt("Quantity to sell:");
        if (qty <= 0) return;
        if (qty > item.getQuantity()) {
            info("Not enough stock. Only " + item.getQuantity() + " left.");
            return;
        }
        item.setQuantity(item.getQuantity() - qty);
        sale("Item: " + qty + " x " + item.getName(), qty * item.getPrice());
    }

    private void restock() {
        int row = itemTable.getSelectedRow();
        if (row < 0) { info("Select an item first."); return; }
        int qty = askInt("Quantity to add:");
        if (qty <= 0) return;
        Item item = items.get(row);
        item.setQuantity(item.getQuantity() + qty);
        save();
    }

    private void addItem() {
        JTextField name = new JTextField();
        JTextField price = new JTextField();
        JTextField qty = new JTextField();
        if (!form("Add New Item", "Name", name, "Price", price, "Quantity", qty)) return;
        try {
            String n = clean(name.getText());
            double p = Double.parseDouble(price.getText().trim());
            int q = Integer.parseInt(qty.getText().trim());
            if (n.isEmpty() || p < 0 || q < 0) throw new NumberFormatException();
            items.add(new Item(n, p, q));
            save();
        } catch (NumberFormatException e) {
            info("Please fill in every field with valid values.");
        }
    }

    // ---------- Animals ----------

    private void sellAnimal() {
        int row = animalTable.getSelectedRow();
        if (row < 0) { info("Select an animal first."); return; }
        Animal a = animals.get(row);
        if (a.isSold()) { info("That animal has already been sold."); return; }
        int ok = JOptionPane.showConfirmDialog(this,
                String.format("Sell %s for $%.2f?", a.getSpecies(), a.getPrice()),
                "Farm Store", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;
        a.setSold(true);
        sale("Animal: " + a.getSpecies(), a.getPrice());
    }

    private void addAnimal() {
        JTextField species = new JTextField();
        JTextField age = new JTextField();
        JTextField price = new JTextField();
        JCheckBox specialty = new JCheckBox("Specialty resale from a local breeder");
        JTextField breeder = new JTextField();
        JTextField markup = new JTextField("20");
        if (!form("Add Animal", "Species", species, "Age", age,
                "Price (breeder cost if specialty)", price, "", specialty,
                "Breeder name", breeder, "Markup % (specialty only)", markup)) return;
        try {
            String s = clean(species.getText());
            String ageText = clean(age.getText());
            double p = Double.parseDouble(price.getText().trim());
            if (s.isEmpty() || ageText.isEmpty() || p < 0) throw new NumberFormatException();
            if (specialty.isSelected()) {
                String b = clean(breeder.getText());
                double m = Double.parseDouble(markup.getText().trim());
                if (b.isEmpty() || m < 0) throw new NumberFormatException();
                animals.add(new Animal(s, ageText, p * (1 + m / 100.0), true, b, false));
            } else {
                animals.add(new Animal(s, ageText, p, false, "", false));
            }
            save();
        } catch (NumberFormatException e) {
            info("Please fill in every field with valid values.");
        }
    }

    // ---------- Services ----------

    private void scheduleService() {
        JComboBox<String> type = new JComboBox<>(SERVICE_TYPES);
        JTextField date = new JTextField(LocalDate.now().toString());
        if (!form("Schedule Service", "Service", type, "Date (YYYY-MM-DD)", date)) return;
        try {
            LocalDate when = LocalDate.parse(date.getText().trim());
            String t = (String) type.getSelectedItem();
            services.add(new Service(t, when.toString(), servicePrice(t), false));
            save();
        } catch (DateTimeParseException e) {
            info("Please enter the date as YYYY-MM-DD.");
        }
    }

    private static double servicePrice(String type) {
        switch (type) {
            case "Checkup": return 40;
            case "Vaccination": return 25;
            case "Grooming": return 30;
            case "Nail Trim": return 15;
            default: return 100;
        }
    }

    private void markPaid() {
        int row = serviceTable.getSelectedRow();
        if (row < 0) { info("Select a service first."); return; }
        Service s = services.get(row);
        if (s.isPaid()) { info("That service is already paid."); return; }
        s.setPaid(true);
        sale("Service: " + s.getType(), s.getPrice());
    }

    // ---------- Saving and refreshing ----------

    private void sale(String description, double total) {
        try {
            FileManager.logSale(description, total);
        } catch (IOException e) {
            error(e);
        }
        save();
    }

    private void save() {
        try {
            FileManager.saveItems(items);
            FileManager.saveAnimals(animals);
            FileManager.saveServices(services);
        } catch (IOException e) {
            error(e);
        }
        refreshAll();
    }

    private void refreshAll() {
        itemModel.setRowCount(0);
        int low = 0;
        for (Item i : items) {
            int q = i.getQuantity();
            if (q <= LOW_STOCK) low++;
            String status = q == 0 ? "OUT" : q <= LOW_STOCK ? "LOW" : "In stock";
            itemModel.addRow(new Object[]{i.getName(), money(i.getPrice()), q, status});
        }

        animalModel.setRowCount(0);
        int available = 0;
        for (Animal a : animals) {
            if (!a.isSold()) available++;
            animalModel.addRow(new Object[]{a.getSpecies(), a.getAge(), money(a.getPrice()),
                a.isSpecialty() ? "Specialty" : "Farm", a.isSpecialty() ? a.getBreeder() : "-",
                a.isSold() ? "Sold" : "Available"});
        }

        serviceModel.setRowCount(0);
        int unpaid = 0;
        for (Service s : services) {
            if (!s.isPaid()) unpaid++;
            serviceModel.addRow(new Object[]{s.getType(), s.getDate(), money(s.getPrice()),
                s.isPaid() ? "PAID" : "UNPAID"});
        }

        salesModel.setRowCount(0);
        double revenue = 0;
        for (String line : FileManager.readSales()) {
            String[] p = line.split(",", -1);
            if (p.length < 3) continue;
            try {
                double total = Double.parseDouble(p[2]);
                revenue += total;
                salesModel.addRow(new Object[]{p[0], p[1], money(total)});
            } catch (NumberFormatException e) {
                // a hand-edited bad row should not break the history view
            }
        }

        stats[0].setText(money(revenue));
        stats[1].setText(String.valueOf(available));
        stats[2].setText(String.valueOf(unpaid));
        stats[3].setText(String.valueOf(low));
    }

    // ---------- Helpers ----------

    private boolean form(String title, Object... rows) {
        JPanel p = new JPanel(new GridLayout(rows.length / 2, 2, 8, 8));
        for (int i = 0; i < rows.length; i += 2) {
            p.add(new JLabel((String) rows[i]));
            p.add((JComponent) rows[i + 1]);
        }
        return JOptionPane.showConfirmDialog(this, p, title, JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION;
    }

    private int askInt(String msg) {
        String s = JOptionPane.showInputDialog(this, msg, "Farm Store", JOptionPane.QUESTION_MESSAGE);
        if (s == null) return -1;
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            info("Please enter a whole number.");
            return -1;
        }
    }

    /** Commas would split a value across CSV columns, so they are replaced with spaces. */
    private static String clean(String s) {
        return s.trim().replace(",", " ");
    }

    private static String money(double value) {
        return String.format("$%.2f", value);
    }

    private void info(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Farm Store", JOptionPane.INFORMATION_MESSAGE);
    }

    private void error(IOException e) {
        JOptionPane.showMessageDialog(this, "File error: " + e.getMessage(), "Farm Store",
                JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            // the system look still works, just with less color
        }
        SwingUtilities.invokeLater(() -> new FarmStoreGUI().setVisible(true));
    }
}
