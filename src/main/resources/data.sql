-- Users
INSERT IGNORE INTO users (email, name, role, password) VALUES ('john.doe@example.com', 'John Doe', 'CUSTOMER', '$2a$10$Uf.Vf0njzII/YDD6lWLKzePkgskC/k25jsy6Z2aQoi84fqGZRy8Vm');
INSERT IGNORE INTO users (email, name, role, password) VALUES ('jane.smith@example.com', 'Jane Smith', 'CUSTOMER', '$2a$10$Uf.Vf0njzII/YDD6lWLKzePkgskC/k25jsy6Z2aQoi84fqGZRy8Vm');
INSERT IGNORE INTO users (email, name, role, password) VALUES ('admin@bookshop.com', 'Admin User', 'ADMINISTRATOR', '$2a$10$Uf.Vf0njzII/YDD6lWLKzePkgskC/k25jsy6Z2aQoi84fqGZRy8Vm');
INSERT IGNORE INTO users (email, name, role, password) VALUES ('manager@bookshop.com', 'Store Manager', 'MANAGER', '$2a$10$Uf.Vf0njzII/YDD6lWLKzePkgskC/k25jsy6Z2aQoi84fqGZRy8Vm');

-- Products
INSERT IGNORE INTO products (title, author, price, description, quantity)
  VALUES ('The Great Gatsby', 'F. Scott Fitzgerald', 12.99, 'A novel set in the Jazz Age on Long Island', 50);

INSERT IGNORE INTO products (title, author, price, description, quantity)
  VALUES ('1984', 'George Orwell', 14.99, 'A dystopian social science fiction novel', 75);

INSERT IGNORE INTO products (title, author, price, description, quantity)
  VALUES ('Harry Potter and the Sorcerer''s Stone', 'J.K. Rowling', 24.99, 'A young wizard discovers his magical heritage', 100);

INSERT IGNORE INTO products (title, author, price, description, quantity)
  VALUES ('The Old Man and the Sea', 'Ernest Hemingway', 10.99, 'An aging Cuban fisherman struggles with a giant marlin', 30);

INSERT IGNORE INTO products (title, author, price, description, quantity)
  VALUES ('Murder on the Orient Express', 'Agatha Christie', 13.99, 'Hercule Poirot investigates a murder on a luxury train', 60);

-- Bookings
INSERT IGNORE INTO bookings (user_id, product_id, quantity, status, created_at)
  VALUES (1, 1, 2, 'PENDING', CURRENT_TIMESTAMP);

INSERT IGNORE INTO bookings (user_id, product_id, quantity, status, created_at)
  VALUES (2, 3, 1, 'APPROVED', CURRENT_TIMESTAMP);

INSERT IGNORE INTO bookings (user_id, product_id, quantity, status, created_at)
  VALUES (1, 5, 3, 'CANCELLED', CURRENT_TIMESTAMP);
