# 🛒 Java Shopping Management System

A robust Java-based shopping application featuring user authentication, product management, order placement, tracking, and invoice generation.  
Built with Swing for GUI and MySQL for backend data storage.

---

## 📌 Features

### 👤 User Management
- Login and registration functionality
- Each user has a unique account
- Personalized order history and tracking

### 📦 Product Management
- Add and display products (name, price, stock)
- Admin or authenticated users can manage inventory

### 🧾 Order Placement & Tracking
- Place orders by product ID and quantity
- Auto-check stock availability
- Auto-calculate estimated delivery date
- Track order status with estimated delivery time

### 📑 Invoice Generation
- Generate printable HTML invoices for orders
- Shows order summary, tax, and delivery info

---

## 🛠️ Technologies Used

| Tech Stack       | Purpose                                |
|------------------|----------------------------------------|
| Java Swing       | User interface design                  |
| MySQL            | Backend database                       |
| JDBC             | Java-MySQL database connectivity       |
| HTML             | Dynamic invoice display                |
| AWT/Javax Swing  | GUI components and dialog rendering    |

---

## 🎯 Modules Overview

### 1. Authentication Module
- `registerUser()` – Handles new user signup
- `authenticateUser()` – Validates login credentials
- User info stored in `users` table

### 2. Product Module
- Add/view products via GUI
- Stored in `products` table
- Product attributes: ID, name, price, stock

### 3. Order Module
- Orders stored in `orders` table
- Linked to users and products via foreign keys
- Includes delivery date & status tracking

### 4. Invoice Generator
- Fetches full order and user info
- Generates detailed HTML invoice in dialog box

---

## 🧪 Setup & Installation

### 🔧 Requirements
- Java 8+
- MySQL Server
- MySQL Connector/J (JDBC driver)

### 📦 Database Setup

1. Open MySQL and run:
```sql
CREATE DATABASE shopping_db;
In your Java file, ensure the following DB config is correct:

java
Copy
Edit
private static final String DB_URL = "jdbc:mysql://localhost:3306/shopping_db";
private static final String USER = "root";
private static final String PASS = "your_password_here";
Tables (users, products, orders) are automatically created by initializeDatabase() at runtime.

▶️ Run the Application
Compile the file:

bash
Copy
Edit
javac ShoppingManagementSystem.java
Run it:

bash
Copy
Edit
java ShoppingManagementSystem
A GUI window will appear for login/registration.

🧭 App Navigation
1. Login/Register
Enter credentials to log in

Or register a new account

2. Tabs Overview
Products: View/Add items

Place Order: Enter Product ID + Quantity

My Orders: View history, track, or get invoice

3. Order Workflow
Place order

System checks stock & updates it

Shows delivery date (auto set to +3 days)

Track order or generate invoice anytime

🖼️ Sample Screenshots
[Add your screenshots here to showcase the app UI]
Example:

Login/Register window

Products table

Order placement form

Invoice popup

📋 Example Invoice Output
html
Copy
Edit
-----------------------------------------------
INVOICE
-----------------------------------------------
Invoice #: 1032                 Date: 2025-04-19
Customer: John Doe             Username: johndoe

------------------------------------------------
Product       Qty   Unit Price    Total
------------------------------------------------
Laptop         1      $700.00     $700.00

Subtotal:                           $700.00
Tax (10%):                          $70.00
Total:                              $770.00
------------------------------------------------
Estimated Delivery: 2025-04-22
Status: Processing

Thank you for your purchase!
-----------------------------------------------
🧠 Future Improvements
✅ Invoice export to PDF

⏳ Admin user panel

📦 Product categories

📈 Dashboard with analytics

🛍️ Order cancellation/refund flow

🧑‍💻 Author
Developed by: Your Name Here
Feel free to fork, customize, and contribute. ⭐️ Star this repo if you found it useful!

📄 License
This project is licensed under the MIT License - see the LICENSE file for details.

yaml
Copy
Edit

---

Let me know if you'd like me to:
- Include screenshots or sample data
- Customize author details
- Add a `LICENSE` file or `contributing.md` file for GitHub

I can also generate this as a `.md` file ready for download if you'd like!