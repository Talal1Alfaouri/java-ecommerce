package miniamazonapp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.math.*;
import java.sql.*;
import java.util.*;

public class Amazon {

    public static void main(String[] args) {
        PasswordFrame f = new PasswordFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(420, 240);
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }
}

class DB {

    private static final String url = "jdbc:mysql://localhost:3306/mini_amazon";
    private static final String user = "root";
    private static final String pass = "";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception ex) {
            // Simple slide-friendly alert
            JOptionPane.showMessageDialog(null, "MySQL Driver not found in Libraries.");
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, pass);
    }
}

//login
class PasswordFrame extends JFrame {

    JLabel l1 = new JLabel("Username:");
    JLabel l2 = new JLabel("Password:");
    JTextField t1 = new JTextField();
    JPasswordField p1 = new JPasswordField();
    JButton b1 = new JButton("Login");
    JButton b2 = new JButton("Cancel");
    JRadioButton r1 = new JRadioButton("Show Password");

    public PasswordFrame() {
        super("Amazon - Login");
        setLayout(null);

        l1.setBounds(40, 40, 100, 20);
        l2.setBounds(40, 80, 100, 20);
        t1.setBounds(140, 40, 200, 20);
        p1.setBounds(140, 80, 200, 20);
        b1.setBounds(70, 130, 100, 25);
        b2.setBounds(200, 130, 100, 25);
        r1.setBounds(140, 160, 200, 20);

        add(l1);
        add(l2);
        add(t1);
        add(p1);
        add(b1);
        add(b2);
        add(r1);

        Handler h = new Handler();
        b1.addActionListener(h);
        b2.addActionListener(h);
        r1.addActionListener(h);
    }

    private class Handler implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == b1) {
                doLogin();
            } else if (e.getSource() == b2) {
                t1.setText("");
                p1.setText("");
            } else if (e.getSource() == r1) {
                if (r1.isSelected()) {
                    p1.setEchoChar((char) 0);
                    r1.setText("Hide Password");
                } else {
                    p1.setEchoChar('*');
                    r1.setText("Show Password");
                }
            }
        }
    }

    private void doLogin() {
        String user = t1.getText().trim();
        String pass = new String(p1.getPassword());

        if (user.length() == 0 || pass.length() == 0) {
            JOptionPane.showMessageDialog(this, "Enter username and password.");
            return;
        }

        try (Connection con = DB.getConnection(); Statement st = con.createStatement()) {

            String sql = "select * from users where username='" + user
                    + "' and password='" + pass + "'";
            ResultSet rs = st.executeQuery(sql);

            if (rs.next()) {
                String fullName = rs.getString("full_name");
                String role = rs.getString("role"); // ADMIN or CUSTOMER
                int userId = rs.getInt("user_id");

                JOptionPane.showMessageDialog(this, "Welcome " + fullName);
                dispose();

                AmazonFrame f = new AmazonFrame(userId, fullName, role);
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                f.setSize(950, 620);
                f.setLocationRelativeTo(null);
                f.setVisible(true);

            } else {
                JOptionPane.showMessageDialog(this, "Error: Invalid username or password.");
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }
}

//FRAME
class AmazonFrame extends JFrame {

    private int userId;
    private String fullName;
    private String role;

    // Panels
    private Dashboard dashboard = new Dashboard();

    // Admin panels
    private Categories cp = new Categories();
    private Products pp = new Products();

    // Customer panels
    private ShopProducts sp;
    private CartPanel cartP;

    // Menu items
    private JMenuItem exitItem, categoriesItem, manageProductsItem, shopProductsItem, cartItem, aboutItem;

    public AmazonFrame(int userId, String fullName, String role) {
        super("Mini Amazon - " + fullName + " (" + role + ")");
        this.userId = userId;
        this.fullName = fullName;
        this.role = role;

        sp = new ShopProducts(userId);
        cartP = new CartPanel(userId);

        setLayout(new BorderLayout());
        setContentPane(dashboard);

        buildMenu();
    }

    private void buildMenu() {
        JMenuBar bar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenu adminMenu = new JMenu("Admin");
        JMenu shopMenu = new JMenu("Shop");
        JMenu helpMenu = new JMenu("Help");

        exitItem = new JMenuItem("Exit");

        categoriesItem = new JMenuItem("Categories");
        manageProductsItem = new JMenuItem("Manage Products");

        shopProductsItem = new JMenuItem("Shop Products");
        cartItem = new JMenuItem("My Cart");

        aboutItem = new JMenuItem("About");

        fileMenu.add(exitItem);

        if (role.equalsIgnoreCase("ADMIN")) {
            adminMenu.add(categoriesItem);
            adminMenu.add(manageProductsItem);

            // admin can also test shopping
            shopMenu.add(shopProductsItem);
            shopMenu.add(cartItem);

            bar.add(fileMenu);
            bar.add(adminMenu);
            bar.add(shopMenu);
            bar.add(helpMenu);
        } else {
            shopMenu.add(shopProductsItem);
            shopMenu.add(cartItem);

            bar.add(fileMenu);
            bar.add(shopMenu);
            bar.add(helpMenu);
        }

        helpMenu.add(aboutItem);

        setJMenuBar(bar);

        ItemHandler h = new ItemHandler();
        exitItem.addActionListener(h);
        categoriesItem.addActionListener(h);
        manageProductsItem.addActionListener(h);
        shopProductsItem.addActionListener(h);
        cartItem.addActionListener(h);
        aboutItem.addActionListener(h);
    }

    private class ItemHandler implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {

            if (e.getSource() == exitItem) {
                System.exit(0);

            } else if (e.getSource() == categoriesItem) {
                setContentPane(cp);
                cp.refreshList();
                validate();
                repaint();

            } else if (e.getSource() == manageProductsItem) {
                setContentPane(pp);
                pp.refreshListWithThread();
                validate();
                repaint();

            } else if (e.getSource() == shopProductsItem) {
                setContentPane(sp);
                sp.refreshList();
                validate();
                repaint();

            } else if (e.getSource() == cartItem) {
                setContentPane(cartP);
                cartP.refreshCart();
                validate();
                repaint();

            } else if (e.getSource() == aboutItem) {
                JOptionPane.showMessageDialog(AmazonFrame.this,
                        "Mini Amazon\nSwing + JDBC + Menus + Java2D + Threads");
            }
        }
    }
}

// Java2D
class Dashboard extends JPanel {

    public Dashboard() {
        setLayout(new BorderLayout());
        JLabel title = new JLabel("Dashboard", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        add(title, BorderLayout.NORTH);

        JLabel info = new JLabel("Use menus to manage or shop.", SwingConstants.CENTER);
        add(info, BorderLayout.SOUTH);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        int w = getWidth();
        int h = getHeight();

        int boxW = 220;
        int boxH = 120;
        int x = (w - boxW) / 2;
        int y = (h - boxH) / 2;

        Shape rect = new RoundRectangle2D.Double(x, y, boxW, boxH, 30, 30);
        g2d.setStroke(new BasicStroke(3));
        g2d.draw(rect);

        g2d.draw(new Ellipse2D.Double(x + 35, y + boxH - 10, 25, 25));
        g2d.draw(new Ellipse2D.Double(x + boxW - 60, y + boxH - 10, 25, 25));

        g2d.setFont(new Font("SansSerif", Font.BOLD, 16));
        g2d.drawString("Amazon", x + 55, y + 65);
    }
}
class Category {

    int id;
    String name;
    String desc;

    public Category(int id, String name, String desc) {
        this.id = id;
        this.name = name;
        this.desc = desc;
    }

    @Override
    public String toString() {
        return name;
    }
}

class Product {

    int id;
    int categoryId;
    String name;
    BigDecimal price;
    int stock;

    public Product(int id, int categoryId, String name, BigDecimal price, int stock) {
        this.id = id;
        this.categoryId = categoryId;
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    @Override
    public String toString() {
        return name + " | " + price + " | stock:" + stock;
    }
}

// Categories
class Categories extends JPanel {

    private DefaultListModel<Category> model = new DefaultListModel<>();
    private JList<Category> list = new JList<>(model);
    private JTextField nameField = new JTextField(15);
    private JTextField descField = new JTextField(15);
    private JButton addBtn = new JButton("Add");
    private JButton updateBtn = new JButton("Update");
    private JButton deleteBtn = new JButton("Delete");
    private JButton refreshBtn = new JButton("Refresh");

    public Categories() {
        setLayout(new BorderLayout(10, 10));

        JPanel top = new JPanel(new FlowLayout());
        top.add(new JLabel("Name:"));
        top.add(nameField);
        top.add(new JLabel("Desc:"));
        top.add(descField);

        JPanel bottom = new JPanel(new FlowLayout());
        bottom.add(addBtn);
        bottom.add(updateBtn);
        bottom.add(deleteBtn);
        bottom.add(refreshBtn);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(list), BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        Handler h = new Handler();
        addBtn.addActionListener(h);
        updateBtn.addActionListener(h);
        deleteBtn.addActionListener(h);
        refreshBtn.addActionListener(h);

        refreshList();
    }

    public void refreshList() {
        model.clear();
        try (Connection con = DB.getConnection(); Statement st = con.createStatement()) {

            ResultSet rs = st.executeQuery("select * from categories order by name");
            while (rs.next()) {
                model.addElement(new Category(
                        rs.getInt("category_id"),
                        rs.getString("name"),
                        rs.getString("description")
                ));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private class Handler implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == addBtn) {
                addCategory();
            } else if (e.getSource() == updateBtn) {
                updateCategory();
            } else if (e.getSource() == deleteBtn) {
                deleteCategory();
            } else if (e.getSource() == refreshBtn) {
                refreshList();
            }
        }
    }

    private void addCategory() {
        String n = nameField.getText().trim();
        String d = descField.getText().trim();
        if (n.length() == 0) {
            JOptionPane.showMessageDialog(this, "Name required.");
            return;
        }

        try (Connection con = DB.getConnection(); Statement st = con.createStatement()) {

            st.executeUpdate("insert into categories(name, description) "
                    + "values('" + n + "','" + d + "')");
            refreshList();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void updateCategory() {
        Category c = list.getSelectedValue();
        if (c == null) {
            JOptionPane.showMessageDialog(this, "Select category first.");
            return;
        }

        String n = nameField.getText().trim();
        String d = descField.getText().trim();

        try (Connection con = DB.getConnection(); Statement st = con.createStatement()) {

            st.executeUpdate("update categories set name='" + n
                    + "', description='" + d
                    + "' where category_id=" + c.id);
            refreshList();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void deleteCategory() {
        Category c = list.getSelectedValue();
        if (c == null) {
            JOptionPane.showMessageDialog(this, "Select category first.");
            return;
        }

        try (Connection con = DB.getConnection(); Statement st = con.createStatement()) {

            st.executeUpdate("delete from categories where category_id=" + c.id);
            refreshList();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }
}

// Products  
class Products extends JPanel {

    private DefaultListModel<Product> model = new DefaultListModel<>();
    private JList<Product> list = new JList<>(model);

    private JTextField catIdField = new JTextField(5);
    private JTextField nameField = new JTextField(10);
    private JTextField priceField = new JTextField(6);
    private JTextField stockField = new JTextField(4);

    private JButton addBtn = new JButton("Add");
    private JButton updateBtn = new JButton("Update");
    private JButton deleteBtn = new JButton("Delete");
    private JButton refreshBtn = new JButton("Refresh");

    public Products() {
        setLayout(new BorderLayout(10, 10));

        JPanel top = new JPanel(new GridLayout(2, 4, 8, 8));
        top.add(new JLabel("Cat ID:"));
        top.add(catIdField);
        top.add(new JLabel("Name:"));
        top.add(nameField);
        top.add(new JLabel("Price:"));
        top.add(priceField);
        top.add(new JLabel("Stock:"));
        top.add(stockField);

        JPanel bottom = new JPanel(new FlowLayout());
        bottom.add(addBtn);
        bottom.add(updateBtn);
        bottom.add(deleteBtn);
        bottom.add(refreshBtn);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(list), BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        Handler h = new Handler();
        addBtn.addActionListener(h);
        updateBtn.addActionListener(h);
        deleteBtn.addActionListener(h);
        refreshBtn.addActionListener(h);

        refreshList();
    }

    public void refreshListWithThread() {
        Thread t = new Thread(new Runnable() {
            public void run() {
                refreshList();
            }
        });
        t.start();
    }

    public void refreshList() {
        model.clear();
        try (Connection con = DB.getConnection(); Statement st = con.createStatement()) {

            ResultSet rs = st.executeQuery("select * from products order by name");
            while (rs.next()) {
                model.addElement(new Product(
                        rs.getInt("product_id"),
                        rs.getInt("category_id"),
                        rs.getString("name"),
                        rs.getBigDecimal("price"),
                        rs.getInt("stock_qty")
                ));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private class Handler implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == addBtn) {
                addProduct();
            } else if (e.getSource() == updateBtn) {
                updateProduct();
            } else if (e.getSource() == deleteBtn) {
                deleteProduct();
            } else if (e.getSource() == refreshBtn) {
                refreshListWithThread();
            }
        }
    }

    private void addProduct() {
        try {
            int cid = Integer.parseInt(catIdField.getText().trim());
            String n = nameField.getText().trim();
            BigDecimal p = new BigDecimal(priceField.getText().trim());
            int s = Integer.parseInt(stockField.getText().trim());

            try (Connection con = DB.getConnection(); Statement st = con.createStatement()) {

                st.executeUpdate("insert into products(category_id,name,price,stock_qty) "
                        + "values(" + cid + ",'" + n + "'," + p + "," + s + ")");
                refreshList();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid input.");
        }
    }

    private void updateProduct() {
        Product pr = list.getSelectedValue();
        if (pr == null) {
            JOptionPane.showMessageDialog(this, "Select product first.");
            return;
        }

        try {
            int cid = Integer.parseInt(catIdField.getText().trim());
            String n = nameField.getText().trim();
            BigDecimal p = new BigDecimal(priceField.getText().trim());
            int s = Integer.parseInt(stockField.getText().trim());

            try (Connection con = DB.getConnection(); Statement st = con.createStatement()) {

                st.executeUpdate("update products set category_id=" + cid
                        + ", name='" + n
                        + "', price=" + p
                        + ", stock_qty=" + s
                        + " where product_id=" + pr.id);
                refreshList();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid input.");
        }
    }

    private void deleteProduct() {
        Product pr = list.getSelectedValue();
        if (pr == null) {
            JOptionPane.showMessageDialog(this, "Select product first.");
            return;
        }

        try (Connection con = DB.getConnection(); Statement st = con.createStatement()) {

            st.executeUpdate("delete from products where product_id=" + pr.id);
            refreshList();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }
}

// cutomer ShopProducts
class ShopProducts extends JPanel {

    private int userId;

    private DefaultListModel<Product> model = new DefaultListModel<>();
    private JList<Product> list = new JList<>(model);

    private JTextField qtyField = new JTextField("1", 4);
    private JButton addToCartBtn = new JButton("Add To Cart");
    private JButton refreshBtn = new JButton("Refresh");

    public ShopProducts(int userId) {
        this.userId = userId;

        setLayout(new BorderLayout(10, 10));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Qty:"));
        top.add(qtyField);
        top.add(addToCartBtn);
        top.add(refreshBtn);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(list), BorderLayout.CENTER);

        Handler h = new Handler();
        addToCartBtn.addActionListener(h);
        refreshBtn.addActionListener(h);

        refreshList();
    }

    public void refreshList() {
        model.clear();

        try (Connection con = DB.getConnection(); Statement st = con.createStatement()) {

            ResultSet rs = st.executeQuery(
                    "select product_id, category_id, name, price, stock_qty from products order by name"
            );

            while (rs.next()) {
                model.addElement(new Product(
                        rs.getInt("product_id"),
                        rs.getInt("category_id"),
                        rs.getString("name"),
                        rs.getBigDecimal("price"),
                        rs.getInt("stock_qty")
                ));
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private class Handler implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == addToCartBtn) {
                addSelectedToCart();
            } else if (e.getSource() == refreshBtn) {
                refreshList();
            }
        }
    }

    private void addSelectedToCart() {
        Product p = list.getSelectedValue();
        if (p == null) {
            JOptionPane.showMessageDialog(this, "Select a product first.");
            return;
        }

        int qty;
        try {
            qty = Integer.parseInt(qtyField.getText().trim());
            if (qty <= 0) {
                throw new Exception();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid quantity.");
            return;
        }

        try (Connection con = DB.getConnection(); Statement st = con.createStatement()) {

            // Find active cart
            int cartId = -1;
            ResultSet r1 = st.executeQuery(
                    "select cart_id from carts where user_id=" + userId + " and status='ACTIVE'"
            );

            if (r1.next()) {
                cartId = r1.getInt(1);
            } else {
                st.executeUpdate(
                        "insert into carts(user_id, status) values(" + userId + ",'ACTIVE')"
                );
                ResultSet r2 = st.executeQuery(
                        "select cart_id from carts where user_id=" + userId + " and status='ACTIVE'"
                );
                if (r2.next()) {
                    cartId = r2.getInt(1);
                }
            }

            if (cartId == -1) {
                JOptionPane.showMessageDialog(this, "Could not create cart.");
                return;
            }

            // Check if item exists
            ResultSet r3 = st.executeQuery(
                    "select cart_item_id, quantity from cart_items "
                    + "where cart_id=" + cartId + " and product_id=" + p.id
            );

            if (r3.next()) {
                int oldQty = r3.getInt("quantity");
                int newQty = oldQty + qty;
                st.executeUpdate(
                        "update cart_items set quantity=" + newQty
                        + " where cart_item_id=" + r3.getInt("cart_item_id")
                );
            } else {
                st.executeUpdate(
                        "insert into cart_items(cart_id, product_id, quantity, unit_price) values("
                        + cartId + "," + p.id + "," + qty + "," + p.price + ")"
                );
            }

            JOptionPane.showMessageDialog(this, "Added to cart.");

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }
}

// ===========================
// CUSTOMER: CART VIEW
// ===========================
class CartPanel extends JPanel {

    private int userId;

    private DefaultListModel<String> model = new DefaultListModel<>();
    private JList<String> list = new JList<>(model);

    private ArrayList<Integer> itemIds = new ArrayList<>();

    private JLabel totalLabel = new JLabel("Total: 0");
    private JButton removeBtn = new JButton("Remove Selected");
    private JButton refreshBtn = new JButton("Refresh");
    private JButton checkoutBtn = new JButton("Checkout");

    public CartPanel(int userId) {
        this.userId = userId;

        setLayout(new BorderLayout(10, 10));

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(totalLabel);
        bottom.add(removeBtn);
        bottom.add(refreshBtn);
        bottom.add(checkoutBtn);

        add(new JScrollPane(list), BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        Handler h = new Handler();
        removeBtn.addActionListener(h);
        refreshBtn.addActionListener(h);
        checkoutBtn.addActionListener(h);

        refreshCart();
    }

    public void refreshCart() {
        model.clear();
        itemIds.clear();
        totalLabel.setText("Total: 0");

        try (Connection con = DB.getConnection(); Statement st = con.createStatement()) {

            int cartId = -1;
            ResultSet r1 = st.executeQuery(
                    "select cart_id from carts where user_id=" + userId + " and status='ACTIVE'"
            );
            if (r1.next()) {
                cartId = r1.getInt(1);
            }

            if (cartId == -1) {
                return;
            }

            ResultSet rs = st.executeQuery(
                    "select ci.cart_item_id, p.name, ci.quantity, ci.unit_price "
                    + "from cart_items ci, products p "
                    + "where ci.product_id = p.product_id and ci.cart_id=" + cartId
                    + " order by p.name"
            );

            BigDecimal total = BigDecimal.ZERO;

            while (rs.next()) {
                int id = rs.getInt("cart_item_id");
                String name = rs.getString("name");
                int qty = rs.getInt("quantity");
                BigDecimal price = rs.getBigDecimal("unit_price");
                BigDecimal line = price.multiply(new BigDecimal(qty));

                itemIds.add(id);
                model.addElement(name + " | qty:" + qty + " | price:" + price + " | total:" + line);

                total = total.add(line);
            }

            totalLabel.setText("Total: " + total);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private class Handler implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == removeBtn) {
                removeSelected();
            } else if (e.getSource() == refreshBtn) {
                refreshCart();
            } else if (e.getSource() == checkoutBtn) {
                doCheckout();
            }
        }
    }

    private void removeSelected() {
        int index = list.getSelectedIndex();
        if (index < 0) {
            JOptionPane.showMessageDialog(this, "Select an item first.");
            return;
        }

        int cartItemId = itemIds.get(index);

        try (Connection con = DB.getConnection(); Statement st = con.createStatement()) {

            st.executeUpdate("delete from cart_items where cart_item_id=" + cartItemId);
            refreshCart();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    // ===========================
    // CHECKOUT + PAYMENT CHOICE
    // ===========================
    private void doCheckout() {

        String[] methods = {"Visa", "MasterCard", "Apple Pay", "Google Pay"};
        String method = (String) JOptionPane.showInputDialog(
                this,
                "Choose payment method:",
                "Payment",
                JOptionPane.QUESTION_MESSAGE,
                null,
                methods,
                methods[0]
        );

        if (method == null) {
            return; // user canceled
        }
        Connection con = null;
        Statement st = null;
        ResultSet rsItems = null;

        try {
            con = DB.getConnection();
            st = con.createStatement();

            // 1) Find active cart
            int cartId = -1;
            ResultSet r1 = st.executeQuery(
                    "select cart_id from carts where user_id=" + userId + " and status='ACTIVE'"
            );
            if (r1.next()) {
                cartId = r1.getInt(1);
            }

            if (cartId == -1) {
                JOptionPane.showMessageDialog(this, "No active cart.");
                return;
            }

            // 2) Read cart items with stock info
            rsItems = st.executeQuery(
                    "select ci.product_id, ci.quantity, ci.unit_price, p.stock_qty "
                    + "from cart_items ci, products p "
                    + "where ci.product_id=p.product_id and ci.cart_id=" + cartId
            );

            if (!rsItems.next()) {
                JOptionPane.showMessageDialog(this, "Cart is empty.");
                return;
            }

            ArrayList<Integer> productIds = new ArrayList<>();
            ArrayList<Integer> qtys = new ArrayList<>();
            ArrayList<BigDecimal> prices = new ArrayList<>();

            BigDecimal total = BigDecimal.ZERO;

            do {
                int pid = rsItems.getInt("product_id");
                int qty = rsItems.getInt("quantity");
                BigDecimal price = rsItems.getBigDecimal("unit_price");
                int stock = rsItems.getInt("stock_qty");

                if (stock < qty) {
                    JOptionPane.showMessageDialog(this,
                            "Not enough stock for product ID: " + pid);
                    return;
                }

                productIds.add(pid);
                qtys.add(qty);
                prices.add(price);

                total = total.add(price.multiply(new BigDecimal(qty)));

            } while (rsItems.next());

            // 3) Insert order
            st.executeUpdate(
                    "insert into orders(user_id, status, total_amount, payment_method) values("
                    + userId + ",'PAID'," + total + ",'" + method + "')"
            );

            // 4) Get the last order id for this user (simple slide approach)
            ResultSet rOrder = st.executeQuery(
                    "select max(order_id) from orders where user_id=" + userId
            );
            rOrder.next();
            int orderId = rOrder.getInt(1);

            // 5) Insert order items + update stock
            for (int i = 0; i < productIds.size(); i++) {
                int pid = productIds.get(i);
                int qty = qtys.get(i);
                BigDecimal price = prices.get(i);

                st.executeUpdate(
                        "insert into order_items(order_id, product_id, quantity, unit_price) values("
                        + orderId + "," + pid + "," + qty + "," + price + ")"
                );

                st.executeUpdate(
                        "update products set stock_qty = stock_qty - " + qty
                        + " where product_id=" + pid
                );
            }

            // 6) Clear cart items
            st.executeUpdate("delete from cart_items where cart_id=" + cartId);

            // 7) Mark cart as checked out (optional but clean)
            st.executeUpdate("update carts set status='CHECKED_OUT' where cart_id=" + cartId);

            JOptionPane.showMessageDialog(this,
                    "Payment successful using " + method + "\nOrder ID: " + orderId);

            refreshCart();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        } finally {
            try {
                if (rsItems != null) {
                    rsItems.close();
                }
            } catch (Exception ignored) {
            }
            try {
                if (st != null) {
                    st.close();
                }
            } catch (Exception ignored) {
            }
            try {
                if (con != null) {
                    con.close();
                }
            } catch (Exception ignored) {
            }
        }
    }
}
