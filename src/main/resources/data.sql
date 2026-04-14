-- Users
INSERT INTO users (email, name, role) VALUES ('john.doe@example.com', 'John Doe', 'CUSTOMER');
INSERT INTO users (email, name, role) VALUES ('jane.smith@example.com', 'Jane Smith', 'CUSTOMER');
INSERT INTO users (email, name, role) VALUES ('admin@bookshop.com', 'Admin User', 'ADMINISTRATOR');
INSERT INTO users (email, name, role) VALUES ('manager@bookshop.com', 'Store Manager', 'MANAGER');

-- Products
INSERT INTO products (title, author, price, description)
  VALUES ('The Great Gatsby', 'F. Scott Fitzgerald', 12.99, 'A novel set in the Jazz Age on Long Island');

INSERT INTO products (title, author, price, description)
  VALUES ('1984', 'George Orwell', 14.99, 'A dystopian social science fiction novel');

INSERT INTO products (title, author, price, description)
  VALUES ('Harry Potter and the Sorcerer''s Stone', 'J.K. Rowling', 24.99, 'A young wizard discovers his magical heritage');

INSERT INTO products (title, author, price, description)
  VALUES ('The Old Man and the Sea', 'Ernest Hemingway', 10.99, 'An aging Cuban fisherman struggles with a giant marlin');

INSERT INTO products (title, author, price, description)
  VALUES ('Murder on the Orient Express', 'Agatha Christie', 13.99, 'Hercule Poirot investigates a murder on a luxury train');

-- Store Items (product_id references: 1-5 match products above)
INSERT INTO store_items (product_id, quantity) VALUES (1, 50);
INSERT INTO store_items (product_id, quantity) VALUES (2, 75);
INSERT INTO store_items (product_id, quantity) VALUES (3, 100);
INSERT INTO store_items (product_id, quantity) VALUES (4, 30);
INSERT INTO store_items (product_id, quantity) VALUES (5, 60);

-- Bookings
INSERT INTO bookings (user_id, product_id, quantity, status, created_at)
  VALUES (1, 1, 2, 'PENDING', CURRENT_TIMESTAMP);

INSERT INTO bookings (user_id, product_id, quantity, status, created_at)
  VALUES (2, 3, 1, 'APPROVED', CURRENT_TIMESTAMP);

INSERT INTO bookings (user_id, product_id, quantity, status, created_at)
  VALUES (1, 5, 3, 'CANCELLED', CURRENT_TIMESTAMP);
