import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;

public class ShoppingManagementSystem {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/shopping_db";
    private static final String USER = "root";
    private static final String PASS = "Subi@2005";

    private JFrame frame;
    private JTextField nameField, priceField, stockField, orderIdField, orderQtyField;
    private JTextField usernameField, passwordField, regUsernameField, regPasswordField, regNameField;
    private JTable productTable, orderTable, userOrderTable;
    private DefaultTableModel productModel, orderModel, userOrderModel;
    private int currentUserId = -1;
    private String currentUserName = "";

    public ShoppingManagementSystem() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored) {}

        initializeDatabase();
        showLoginScreen();
    }

    private void initializeDatabase() {
        try (Connection con = connect()) {
            Statement stmt = con.createStatement();
            
            // Create tables if they don't exist
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "username VARCHAR(50) UNIQUE NOT NULL, " +
                    "password VARCHAR(50) NOT NULL, " +
                    "name VARCHAR(100) NOT NULL)");
            
            stmt.execute("CREATE TABLE IF NOT EXISTS products (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "name VARCHAR(100) NOT NULL, " +
                    "price DECIMAL(10,2) NOT NULL, " +
                    "stock INT NOT NULL)");
            
            stmt.execute("CREATE TABLE IF NOT EXISTS orders (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "user_id INT NOT NULL, " +
                    "product_id INT NOT NULL, " +
                    "quantity INT NOT NULL, " +
                    "total_price DECIMAL(10,2) NOT NULL, " +
                    "order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "delivery_date DATE, " +
                    "status VARCHAR(20) DEFAULT 'Processing', " +
                    "FOREIGN KEY (user_id) REFERENCES users(id), " +
                    "FOREIGN KEY (product_id) REFERENCES products(id))");
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database initialization error: " + e.getMessage());
        }
    }

    private void showLoginScreen() {
        JFrame loginFrame = new JFrame("Login");
        loginFrame.setSize(400, 300);
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();

        // Login Panel
        JPanel loginPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        loginPanel.add(new JLabel("Username:"));
        usernameField = new JTextField();
        loginPanel.add(usernameField);

        loginPanel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        loginPanel.add(passwordField);

        JButton loginBtn = new JButton("Login");
        loginBtn.addActionListener(e -> {
            if (authenticateUser(usernameField.getText(), passwordField.getText())) {
                loginFrame.dispose();
                showMainApplication();
            } else {
                JOptionPane.showMessageDialog(loginFrame, "Invalid username or password");
            }
        });
        loginPanel.add(loginBtn);

        // Registration Panel
        JPanel registerPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        registerPanel.add(new JLabel("Username:"));
        regUsernameField = new JTextField();
        registerPanel.add(regUsernameField);

        registerPanel.add(new JLabel("Password:"));
        regPasswordField = new JPasswordField();
        registerPanel.add(regPasswordField);

        registerPanel.add(new JLabel("Full Name:"));
        regNameField = new JTextField();
        registerPanel.add(regNameField);

        JButton registerBtn = new JButton("Register");
        registerBtn.addActionListener(e -> registerUser());
        registerPanel.add(registerBtn);

        tabbedPane.addTab("Login", loginPanel);
        tabbedPane.addTab("Register", registerPanel);

        loginFrame.add(tabbedPane);
        loginFrame.setVisible(true);
    }




    

    private boolean authenticateUser(String username, String password) {
        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement("SELECT id, name FROM users WHERE username=? AND password=?")) {
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                currentUserId = rs.getInt("id");
                currentUserName = rs.getString("name");
                return true;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Authentication error: " + e.getMessage());
        }
        return false;
    }

    private void registerUser() {
        String username = regUsernameField.getText();
        String password = regPasswordField.getText();
        String name = regNameField.getText();

        if (username.isEmpty() || password.isEmpty() || name.isEmpty()) {
            JOptionPane.showMessageDialog(null, "All fields are required");
            return;
        }

        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement("INSERT INTO users (username, password, name) VALUES (?, ?, ?)")) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, name);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(null, "Registration successful! Please login.");
            regUsernameField.setText("");
            regPasswordField.setText("");
            regNameField.setText("");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Registration error: " + e.getMessage());
        }
    }

    private void showMainApplication() {
        frame = new JFrame("Shopping Management System - Welcome " + currentUserName);
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Products", createProductPanel());
        tabbedPane.addTab("Place Order", createOrderPanel());
        tabbedPane.addTab("My Orders", createUserOrderPanel());

        frame.add(tabbedPane);
        frame.setVisible(true);
        loadProducts();
        loadOrders();
        loadUserOrders();
    }

    private JPanel createProductPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 10, 10));

        inputPanel.add(new JLabel("Product Name:"));
        nameField = new JTextField();
        inputPanel.add(nameField);

        inputPanel.add(new JLabel("Price:"));
        priceField = new JTextField();
        inputPanel.add(priceField);

        inputPanel.add(new JLabel("Stock:"));
        stockField = new JTextField();
        inputPanel.add(stockField);

        JButton addProductBtn = new JButton("Add Product");
        addProductBtn.addActionListener(e -> addProduct());
        inputPanel.add(addProductBtn);

        panel.add(inputPanel, BorderLayout.NORTH);

        productModel = new DefaultTableModel(new String[]{"ID", "Name", "Price", "Stock"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        productTable = new JTable(productModel);
        panel.add(new JScrollPane(productTable), BorderLayout.CENTER);

        JButton refreshBtn = new JButton("Refresh Products");
        refreshBtn.addActionListener(e -> loadProducts());
        panel.add(refreshBtn, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createOrderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 10, 10));

        inputPanel.add(new JLabel("Product ID:"));
        orderIdField = new JTextField();
        inputPanel.add(orderIdField);

        inputPanel.add(new JLabel("Quantity:"));
        orderQtyField = new JTextField();
        inputPanel.add(orderQtyField);

        JButton placeOrderBtn = new JButton("Place Order");
        placeOrderBtn.addActionListener(e -> placeOrder());
        inputPanel.add(placeOrderBtn);

        panel.add(inputPanel, BorderLayout.NORTH);

        orderModel = new DefaultTableModel(new String[]{"Order ID", "Product", "Quantity", "Total Price", "Order Date", "Delivery Date", "Status"}, 0);
        orderTable = new JTable(orderModel);
        panel.add(new JScrollPane(orderTable), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton refreshBtn = new JButton("Refresh Orders");
        refreshBtn.addActionListener(e -> loadOrders());
        buttonPanel.add(refreshBtn);

        JButton invoiceBtn = new JButton("Generate Invoice");
        invoiceBtn.addActionListener(e -> generateInvoice());
        buttonPanel.add(invoiceBtn);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createUserOrderPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        userOrderModel = new DefaultTableModel(new String[]{"Order ID", "Product", "Quantity", "Total Price", "Order Date", "Delivery Date", "Status"}, 0);
        userOrderTable = new JTable(userOrderModel);
        
        // Add double-click listener for order details
        userOrderTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = userOrderTable.getSelectedRow();
                    if (row >= 0) {
                        int orderId = (int) userOrderModel.getValueAt(row, 0);
                        showOrderDetails(orderId);
                    }
                }
            }
        });
        
        panel.add(new JScrollPane(userOrderTable), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton refreshBtn = new JButton("Refresh My Orders");
        refreshBtn.addActionListener(e -> loadUserOrders());
        buttonPanel.add(refreshBtn);

        JButton trackBtn = new JButton("Track Selected Order");
        trackBtn.addActionListener(e -> {
            int row = userOrderTable.getSelectedRow();
            if (row >= 0) {
                int orderId = (int) userOrderModel.getValueAt(row, 0);
                trackOrder(orderId);
            } else {
                JOptionPane.showMessageDialog(frame, "Please select an order to track");
            }
        });
        buttonPanel.add(trackBtn);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    private void addProduct() {
        String name = nameField.getText();
        String priceText = priceField.getText();
        String stockText = stockField.getText();

        if (name.isEmpty() || priceText.isEmpty() || stockText.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "All fields are required");
            return;
        }

        try {
            double price = Double.parseDouble(priceText);
            int stock = Integer.parseInt(stockText);

            try (Connection con = connect();
                 PreparedStatement ps = con.prepareStatement("INSERT INTO products (name, price, stock) VALUES (?, ?, ?)")) {
                ps.setString(1, name);
                ps.setDouble(2, price);
                ps.setInt(3, stock);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(frame, "Product added successfully!");
                nameField.setText("");
                priceField.setText("");
                stockField.setText("");
                loadProducts();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "Please enter valid numbers for price and stock");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadProducts() {
        try (Connection con = connect();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM products")) {
            productModel.setRowCount(0);
            while (rs.next()) {
                productModel.addRow(new Object[]{
                    rs.getInt("id"), 
                    rs.getString("name"), 
                    rs.getDouble("price"), 
                    rs.getInt("stock")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void placeOrder() {
        String productIdText = orderIdField.getText();
        String quantityText = orderQtyField.getText();

        if (productIdText.isEmpty() || quantityText.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please enter product ID and quantity");
            return;
        }

        try {
            int productId = Integer.parseInt(productIdText);
            int quantity = Integer.parseInt(quantityText);

            if (quantity <= 0) {
                JOptionPane.showMessageDialog(frame, "Quantity must be positive");
                return;
            }

            try (Connection con = connect()) {
                con.setAutoCommit(false);

                // Check product availability
                PreparedStatement checkStock = con.prepareStatement("SELECT name, stock, price FROM products WHERE id=?");
                checkStock.setInt(1, productId);
                ResultSet rs = checkStock.executeQuery();

                if (!rs.next()) {
                    JOptionPane.showMessageDialog(frame, "Product not found.");
                    return;
                }

                String productName = rs.getString("name");
                int stock = rs.getInt("stock");
                double price = rs.getDouble("price");

                if (stock < quantity) {
                    JOptionPane.showMessageDialog(frame, "Not enough stock! Available: " + stock);
                    return;
                }

                // Calculate delivery date (3 days from now)
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DAY_OF_MONTH, 3);
                Date deliveryDate = cal.getTime();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

                double totalPrice = price * quantity;
                
                // Create order
                PreparedStatement placeOrder = con.prepareStatement(
                    "INSERT INTO orders (user_id, product_id, quantity, total_price, delivery_date) VALUES (?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
                placeOrder.setInt(1, currentUserId);
                placeOrder.setInt(2, productId);
                placeOrder.setInt(3, quantity);
                placeOrder.setDouble(4, totalPrice);
                placeOrder.setString(5, sdf.format(deliveryDate));
                placeOrder.executeUpdate();

                // Update stock
                PreparedStatement updateStock = con.prepareStatement("UPDATE products SET stock = stock - ? WHERE id=?");
                updateStock.setInt(1, quantity);
                updateStock.setInt(2, productId);
                updateStock.executeUpdate();

                con.commit();
                
                // Get the generated order ID
                ResultSet generatedKeys = placeOrder.getGeneratedKeys();
                int orderId = -1;
                if (generatedKeys.next()) {
                    orderId = generatedKeys.getInt(1);
                }

                JOptionPane.showMessageDialog(frame, 
                    "<html>Order placed successfully!<br>" +
                    "Order ID: " + orderId + "<br>" +
                    "Product: " + productName + "<br>" +
                    "Quantity: " + quantity + "<br>" +
                    "Total: $" + String.format("%.2f", totalPrice) + "<br>" +
                    "Estimated Delivery: " + sdf.format(deliveryDate) + "</html>");
                
                orderIdField.setText("");
                orderQtyField.setText("");
                loadProducts();
                loadOrders();
                loadUserOrders();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "Please enter valid numbers for product ID and quantity");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadOrders() {
        try (Connection con = connect();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT o.id, p.name, o.quantity, o.total_price, o.order_date, o.delivery_date, o.status " +
                 "FROM orders o JOIN products p ON o.product_id = p.id " +
                 "ORDER BY o.order_date DESC")) {
            orderModel.setRowCount(0);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat deliveryFormat = new SimpleDateFormat("yyyy-MM-dd");
            
            while (rs.next()) {
                orderModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getInt("quantity"),
                    String.format("$%.2f", rs.getDouble("total_price")),
                    dateFormat.format(rs.getTimestamp("order_date")),
                    deliveryFormat.format(rs.getDate("delivery_date")),
                    rs.getString("status")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadUserOrders() {
        if (currentUserId == -1) return;
        
        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(
                 "SELECT o.id, p.name, o.quantity, o.total_price, o.order_date, o.delivery_date, o.status " +
                 "FROM orders o JOIN products p ON o.product_id = p.id " +
                 "WHERE o.user_id = ? " +
                 "ORDER BY o.order_date DESC")) {
            ps.setInt(1, currentUserId);
            ResultSet rs = ps.executeQuery();
            
            userOrderModel.setRowCount(0);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat deliveryFormat = new SimpleDateFormat("yyyy-MM-dd");
            
            while (rs.next()) {
                userOrderModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getInt("quantity"),
                    String.format("$%.2f", rs.getDouble("total_price")),
                    dateFormat.format(rs.getTimestamp("order_date")),
                    deliveryFormat.format(rs.getDate("delivery_date")),
                    rs.getString("status")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showOrderDetails(int orderId) {
        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(
                 "SELECT o.*, p.name as product_name, u.name as user_name " +
                 "FROM orders o " +
                 "JOIN products p ON o.product_id = p.id " +
                 "JOIN users u ON o.user_id = u.id " +
                 "WHERE o.id = ?")) {
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                SimpleDateFormat deliveryFormat = new SimpleDateFormat("yyyy-MM-dd");
                
                String details = "<html><h3>Order Details</h3>" +
                    "<b>Order ID:</b> " + rs.getInt("id") + "<br>" +
                    "<b>Customer:</b> " + rs.getString("user_name") + "<br>" +
                    "<b>Product:</b> " + rs.getString("product_name") + "<br>" +
                    "<b>Quantity:</b> " + rs.getInt("quantity") + "<br>" +
                    "<b>Total Price:</b> $" + String.format("%.2f", rs.getDouble("total_price")) + "<br>" +
                    "<b>Order Date:</b> " + dateFormat.format(rs.getTimestamp("order_date")) + "<br>" +
                    "<b>Estimated Delivery:</b> " + deliveryFormat.format(rs.getDate("delivery_date")) + "<br>" +
                    "<b>Status:</b> " + rs.getString("status") + "</html>";
                
                JOptionPane.showMessageDialog(frame, details, "Order Details", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void trackOrder(int orderId) {
        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(
                 "SELECT o.*, p.name as product_name, u.name as user_name " +
                 "FROM orders o " +
                 "JOIN products p ON o.product_id = p.id " +
                 "JOIN users u ON o.user_id = u.id " +
                 "WHERE o.id = ?")) {
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                SimpleDateFormat DateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                SimpleDateFormat deliveryFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date deliveryDate = rs.getDate("delivery_date");
                String status = rs.getString("status");
                
                // Calculate days remaining
                Calendar today = Calendar.getInstance();
                Calendar delivery = Calendar.getInstance();
                delivery.setTime(deliveryDate);
                long diff = delivery.getTimeInMillis() - today.getTimeInMillis();
                int daysRemaining = (int) (diff / (24 * 60 * 60 * 1000));
                
                String trackingInfo = "<html><h3>Order Tracking</h3>" +
                    "<b>Order ID:</b> " + rs.getInt("id") + "<br>" +
                    "<b>Product:</b> " + rs.getString("product_name") + "<br>" +
                    "<b>Status:</b> " + status + "<br><br>";
                
                if (status.equals("Delivered")) {
                    trackingInfo += "Your order has been delivered.";
                } else if (daysRemaining <= 0) {
                    trackingInfo += "Your order is out for delivery today!";
                } else {
                    trackingInfo += "Estimated delivery in " + daysRemaining + " day(s) on " + 
                                    deliveryFormat.format(deliveryDate);
                }
                
                trackingInfo += "</html>";
                
                JOptionPane.showMessageDialog(frame, trackingInfo, "Order Tracking", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void generateInvoice() {
        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(frame, "Please select an order to generate invoice");
            return;
        }
        
        int orderId = (int) orderModel.getValueAt(selectedRow, 0);
        
        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(
                 "SELECT o.*, p.name as product_name, u.name as user_name, u.username " +
                 "FROM orders o " +
                 "JOIN products p ON o.product_id = p.id " +
                 "JOIN users u ON o.user_id = u.id " +
                 "WHERE o.id = ?")) {
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                SimpleDateFormat deliveryFormat = new SimpleDateFormat("yyyy-MM-dd");
                
                String invoice = "<html><center><h2>INVOICE</h2></center>" +
                    "<table width='100%'>" +
                    "<tr><td><b>Invoice #:</b> " + rs.getInt("id") + "</td>" +
                    "<td align='right'><b>Date:</b> " + dateFormat.format(rs.getTimestamp("order_date")) + "</td></tr>" +
                    "<tr><td colspan='2'><b>Customer:</b> " + rs.getString("user_name") + "</td></tr>" +
                    "<tr><td colspan='2'><b>Username:</b> " + rs.getString("username") + "</td></tr>" +
                    "</table><hr>" +
                    "<table width='100%' border='1' cellpadding='5'>" +
                    "<tr><th>Product</th><th>Quantity</th><th>Unit Price</th><th>Total</th></tr>" +
                    "<tr><td>" + rs.getString("product_name") + "</td>" +
                    "<td align='center'>" + rs.getInt("quantity") + "</td>" +
                    "<td align='right'>$" + String.format("%.2f", rs.getDouble("total_price")/rs.getInt("quantity")) + "</td>" +
                    "<td align='right'>$" + String.format("%.2f", rs.getDouble("total_price")) + "</td></tr>" +
                    "<tr><td colspan='3' align='right'><b>Subtotal:</b></td>" +
                    "<td align='right'>$" + String.format("%.2f", rs.getDouble("total_price")) + "</td></tr>" +
                    "<tr><td colspan='3' align='right'><b>Tax (10%):</b></td>" +
                    "<td align='right'>$" + String.format("%.2f", rs.getDouble("total_price") * 0.1) + "</td></tr>" +
                    "<tr><td colspan='3' align='right'><b>Total:</b></td>" +
                    "<td align='right'>$" + String.format("%.2f", rs.getDouble("total_price") * 1.1) + "</td></tr>" +
                    "</table><hr>" +
                    "<p><b>Delivery Date:</b> " + deliveryFormat.format(rs.getDate("delivery_date")) + "</p>" +
                    "<p><b>Status:</b> " + rs.getString("status") + "</p>" +
                    "<p align='center'>Thank you for your business!</p></html>";
                
                JTextPane invoicePane = new JTextPane();
                invoicePane.setContentType("text/html");
                invoicePane.setText(invoice);
                invoicePane.setEditable(false);
                
                JOptionPane.showMessageDialog(frame, new JScrollPane(invoicePane), 
                    "Invoice for Order #" + orderId, JOptionPane.PLAIN_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ShoppingManagementSystem::new);
    }
}