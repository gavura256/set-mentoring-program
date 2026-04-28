ALTER TABLE products ADD CONSTRAINT chk_product_quantity_non_negative CHECK (quantity >= 0);
