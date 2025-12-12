-- Mini Amazon (7-table) MySQL schema
-- Designed for localhost use with JDBC (e.g., MySQL + XAMPP/WAMP).
-- Includes: Users, Categories, Products, Carts, CartItems, Orders, OrderItems

DROP DATABASE IF EXISTS mini_amazon;
CREATE DATABASE mini_amazon
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE mini_amazon;

-- 1) USERS
CREATE TABLE users (
  user_id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL, -- For coursework simplicity. In real apps, store a hash.
  full_name VARCHAR(100) NOT NULL,
  phone VARCHAR(20),
  email VARCHAR(100),
  role ENUM('ADMIN','CUSTOMER') NOT NULL DEFAULT 'CUSTOMER',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 2) CATEGORIES
CREATE TABLE categories (
  category_id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(80) NOT NULL UNIQUE,
  description VARCHAR(255),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 3) PRODUCTS (includes stock to avoid a separate inventory table)
CREATE TABLE products (
  product_id INT AUTO_INCREMENT PRIMARY KEY,
  category_id INT NOT NULL,
  name VARCHAR(120) NOT NULL,
  price DECIMAL(10,2) NOT NULL,
  stock_qty INT NOT NULL DEFAULT 0,
  description TEXT,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_products_category
    FOREIGN KEY (category_id) REFERENCES categories(category_id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT,
  CONSTRAINT chk_products_price CHECK (price >= 0),
  CONSTRAINT chk_products_stock CHECK (stock_qty >= 0)
) ENGINE=InnoDB;

CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_products_name ON products(name);

-- 4) CARTS (one active cart per user for simplicity)
CREATE TABLE carts (
  cart_id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL UNIQUE,
  status ENUM('ACTIVE','CONVERTED','ABANDONED') NOT NULL DEFAULT 'ACTIVE',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_carts_user
    FOREIGN KEY (user_id) REFERENCES users(user_id)
    ON UPDATE CASCADE
    ON DELETE CASCADE
) ENGINE=InnoDB;

-- 5) CART ITEMS
CREATE TABLE cart_items (
  cart_item_id INT AUTO_INCREMENT PRIMARY KEY,
  cart_id INT NOT NULL,
  product_id INT NOT NULL,
  quantity INT NOT NULL DEFAULT 1,
  unit_price DECIMAL(10,2) NOT NULL,
  added_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_cart_items_cart
    FOREIGN KEY (cart_id) REFERENCES carts(cart_id)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
  CONSTRAINT fk_cart_items_product
    FOREIGN KEY (product_id) REFERENCES products(product_id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT,
  CONSTRAINT chk_cart_items_qty CHECK (quantity > 0),
  CONSTRAINT chk_cart_items_price CHECK (unit_price >= 0),
  UNIQUE KEY uniq_cart_product (cart_id, product_id)
) ENGINE=InnoDB;

CREATE INDEX idx_cart_items_cart ON cart_items(cart_id);
CREATE INDEX idx_cart_items_product ON cart_items(product_id);

-- 6) ORDERS
CREATE TABLE orders (
  order_id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  order_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  status ENUM('PLACED','PAID','SHIPPED','CANCELLED') NOT NULL DEFAULT 'PLACED',
  total_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
  payment_method ENUM('CASH','CARD','TRANSFER','OTHER') NULL,
  paid_at TIMESTAMP NULL,
  CONSTRAINT fk_orders_user
    FOREIGN KEY (user_id) REFERENCES users(user_id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT,
  CONSTRAINT chk_orders_total CHECK (total_amount >= 0)
) ENGINE=InnoDB;

CREATE INDEX idx_orders_user ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);

-- 7) ORDER ITEMS
CREATE TABLE order_items (
  order_item_id INT AUTO_INCREMENT PRIMARY KEY,
  order_id INT NOT NULL,
  product_id INT NOT NULL,
  quantity INT NOT NULL,
  unit_price DECIMAL(10,2) NOT NULL,
  CONSTRAINT fk_order_items_order
    FOREIGN KEY (order_id) REFERENCES orders(order_id)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
  CONSTRAINT fk_order_items_product
    FOREIGN KEY (product_id) REFERENCES products(product_id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT,
  CONSTRAINT chk_order_items_qty CHECK (quantity > 0),
  CONSTRAINT chk_order_items_price CHECK (unit_price >= 0)
) ENGINE=InnoDB;

CREATE INDEX idx_order_items_order ON order_items(order_id);
CREATE INDEX idx_order_items_product ON order_items(product_id);

-- -----------------------------
-- SAMPLE DATA (optional)
-- -----------------------------

INSERT INTO users (username, password, full_name, phone, email, role) VALUES
('admin', 'admin123', 'System Admin', '0500000000', 'admin@example.com', 'ADMIN'),
('user1', 'user123', 'First Customer', '0511111111', 'user1@example.com', 'CUSTOMER');

INSERT INTO categories (name, description) VALUES
('Electronics', 'Phones, accessories, and gadgets'),
('Groceries', 'Daily essentials and food'),
('Fashion', 'Clothes and accessories');

INSERT INTO products (category_id, name, price, stock_qty, description) VALUES
(1, 'Wireless Headphones', 199.00, 25, 'Over-ear Bluetooth headphones'),
(1, 'USB-C Charger 25W', 59.00, 50, 'Fast charger compatible with most devices'),
(2, 'Premium Coffee Beans 1kg', 89.50, 40, 'Medium roast arabica blend'),
(3, 'Casual T-Shirt', 45.00, 100, 'Cotton, multiple sizes');

-- Create a cart for user1
INSERT INTO carts (user_id, status) VALUES
(2, 'ACTIVE');

-- Add items to user1 cart
INSERT INTO cart_items (cart_id, product_id, quantity, unit_price) VALUES
(1, 1, 1, 199.00),
(1, 2, 2, 59.00);

-- Example order for user1
INSERT INTO orders (user_id, status, total_amount, payment_method, paid_at) VALUES
(2, 'PAID', 317.00, 'CARD', NOW());

INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES
(1, 1, 1, 199.00),
(1, 2, 2, 59.00);

-- Done.
