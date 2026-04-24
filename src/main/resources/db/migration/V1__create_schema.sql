CREATE TABLE users (
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    email    VARCHAR(255) NOT NULL,
    name     VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role     VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE TABLE products (
    id          BIGINT         NOT NULL AUTO_INCREMENT,
    title       VARCHAR(255)   NOT NULL,
    author      VARCHAR(255)   NOT NULL,
    price       DECIMAL(10, 2) NOT NULL,
    description VARCHAR(255),
    quantity    INT            NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_products_title_author UNIQUE (title, author)
);

CREATE TABLE bookings (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    user_id    BIGINT       NOT NULL,
    product_id BIGINT       NOT NULL,
    quantity   INT          NOT NULL,
    status     VARCHAR(255) NOT NULL,
    created_at DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_bookings_user    FOREIGN KEY (user_id)    REFERENCES users    (id),
    CONSTRAINT fk_bookings_product FOREIGN KEY (product_id) REFERENCES products (id)
);

CREATE INDEX idx_bookings_user_id    ON bookings (user_id);
CREATE INDEX idx_bookings_product_id ON bookings (product_id);
