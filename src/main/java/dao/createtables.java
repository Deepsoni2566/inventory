package dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class createtables {

    public static void main(String[] args) {
        try (Connection con = ConnectionProvider.getCon();
             Statement stmt = con.createStatement()) {

            // 1) Create and switch to our schema
            stmt.execute("CREATE DATABASE IF NOT EXISTS inventory");
            stmt.execute("USE inventory");
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");

            // 2) categories table matches ProductManager.categoryCombo usage
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS categories (
                  id INT AUTO_INCREMENT PRIMARY KEY,
                  name VARCHAR(100) NOT NULL UNIQUE,
                  is_deleted BOOLEAN NOT NULL DEFAULT FALSE
                )
            """);

            // 3) products table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS products (
                  id INT AUTO_INCREMENT PRIMARY KEY,
                  name VARCHAR(255) NOT NULL,
                  category VARCHAR(100) NOT NULL,
                  description TEXT,
                  quantity INT NOT NULL,
                  price DECIMAL(10,2) NOT NULL,
                  buying_price DECIMAL(10,2) NOT NULL,
                  location VARCHAR(100),
                  vat INT NOT NULL,
                  is_deleted BOOLEAN NOT NULL DEFAULT FALSE
                )
            """);

            // 4) sales table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS sales (
                  id INT AUTO_INCREMENT PRIMARY KEY,
                  product_id INT NOT NULL,
                  user_id INT,
                  quantity INT NOT NULL,
                  total_amount DECIMAL(12,2) NOT NULL,
                  sold_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                  FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
                )
            """);

            // 5) (optional) other tables: product_images, product_reviews, etc.
            // ... omitted for brevity, add as needed ...

            // 6) seed sample categories
            String[] categories = {"Dairy", "Snacks", "Electronics", "Cleaning", "Frozen"};
            for (String cat : categories) {
                stmt.execute("INSERT IGNORE INTO categories(name) VALUES ('" + cat + "')");
            }

            // 7) seed 100 sample products
            for (int i = 1; i <= 100; i++) {
                String cat = categories[i % categories.length];
                String name = "Product " + i;
                String description = "Sample description for " + name;
                int qty = 5 + (i % 20);
                double price = 1.0 + (i % 10) * 0.5;
                double buy = price * 0.6;
                String loc = "Shelf " + ((char)('A' + (i % 5)));
                int vat = (i % 3) * 5;  // 0%, 5%, 10%
                String insert = String.format(
                        "INSERT INTO products(name,category,description,quantity,price,buying_price,location,vat) " +
                                "VALUES('%s','%s','%s',%d,%.2f,%.2f,'%s',%d)",
                        name.replace("'", "''"),
                        cat.replace("'", "''"),
                        description.replace("'", "''"),
                        qty, price, buy, loc, vat
                );
                stmt.execute(insert);
            }

            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
            System.out.println("✅ Tables created and 100 sample products inserted.");

        } catch (SQLException e) {
            System.err.println("❌ Error creating tables: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
