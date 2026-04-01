INSERT INTO users (email, name, role) VALUES
    ('admin@bookshop.com', 'Admin User', 'ADMINISTRATOR'),
    ('manager@bookshop.com', 'Manager User', 'MANAGER'),
    ('customer@bookshop.com', 'John Doe', 'CUSTOMER');

INSERT INTO products (title, author, price, description) VALUES
    ('Clean Code', 'Robert C. Martin', 29.99, 'A handbook of agile software craftsmanship'),
    ('The Pragmatic Programmer', 'David Thomas', 35.00, 'Your journey to mastery'),
    ('Design Patterns', 'Gang of Four', 45.00, 'Elements of reusable object-oriented software'),
    ('Effective Java', 'Joshua Bloch', 40.00, 'Best practices for the Java platform');

INSERT INTO store_items (product_id, quantity) VALUES
    (1, 10),
    (2, 5),
    (3, 8),
    (4, 3);
