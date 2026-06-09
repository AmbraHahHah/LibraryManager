CREATE TABLE IF NOT EXISTS author (
    id          UUID        NOT NULL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    first_name  VARCHAR(100),
    biography   TEXT,
    nationality VARCHAR(100),
    birth_date  DATE
);

CREATE TABLE IF NOT EXISTS book (
    id         UUID         NOT NULL PRIMARY KEY,
    title      VARCHAR(500) NOT NULL,
    summary    TEXT,
    language   VARCHAR(50)  DEFAULT 'English',
    created_at TIMESTAMP    NOT NULL
);

CREATE TABLE IF NOT EXISTS category (
    id          UUID         NOT NULL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    description TEXT,
    parent_id   UUID         REFERENCES category(id)
);

CREATE INDEX IF NOT EXISTS idx_category_parent ON category(parent_id);

CREATE TABLE IF NOT EXISTS editor (
    id      UUID         NOT NULL PRIMARY KEY,
    name    VARCHAR(150) NOT NULL,
    address TEXT,
    email   VARCHAR(255),
    country VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS book_author (
    book_id   UUID        NOT NULL REFERENCES book(id),
    author_id UUID        NOT NULL REFERENCES author(id),
    role      VARCHAR(50) DEFAULT 'Author',
    PRIMARY KEY (book_id, author_id)
);

CREATE TABLE IF NOT EXISTS book_category (
    book_id     UUID NOT NULL REFERENCES book(id),
    category_id UUID NOT NULL REFERENCES category(id),
    PRIMARY KEY (book_id, category_id)
);

CREATE TABLE IF NOT EXISTS client (
    id                UUID         NOT NULL PRIMARY KEY,
    name              VARCHAR(200) NOT NULL,
    first_name        VARCHAR(200) NOT NULL,
    email             VARCHAR(250) NOT NULL,
    phone             VARCHAR(50),
    address           TEXT,
    city              VARCHAR(150),
    postal_code       VARCHAR(20),
    country           VARCHAR(100),
    active            BOOLEAN      NOT NULL DEFAULT TRUE,
    registration_date TIMESTAMP    NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_client_email ON client(email);

CREATE TABLE IF NOT EXISTS copy (
    id               UUID          NOT NULL PRIMARY KEY,
    isbn             VARCHAR(20)   NOT NULL,
    format           VARCHAR(10)   NOT NULL,
    price            DECIMAL(10,2) NOT NULL DEFAULT 0,
    page_count       INTEGER,
    publication_date DATE,
    image_url        TEXT,
    updated_at       TIMESTAMP,
    book_id          UUID          NOT NULL REFERENCES book(id),
    publisher_id     UUID          REFERENCES editor(id)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_copy_isbn ON copy(isbn);

CREATE INDEX IF NOT EXISTS idx_copy_book ON copy(book_id);
CREATE INDEX IF NOT EXISTS idx_copy_publisher ON copy(publisher_id);

CREATE TABLE IF NOT EXISTS orders (
    id                UUID           NOT NULL PRIMARY KEY,
    customer_id       UUID           NOT NULL REFERENCES client(id),
    payment_reference VARCHAR(255),
    status            VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    total_amount      DECIMAL(12,2)  NOT NULL DEFAULT 0,
    shipping_fee      DECIMAL(12,2)  NOT NULL DEFAULT 0,
    shipping_address  TEXT,
    payment_method    VARCHAR(100),
    order_date        TIMESTAMP      NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_orders_customer ON orders(customer_id);

CREATE TABLE IF NOT EXISTS stock (
    id                 UUID      NOT NULL PRIMARY KEY,
    available_quantity INTEGER   NOT NULL DEFAULT 0,
    reserved_quantity  INTEGER   NOT NULL DEFAULT 0,
    alert_threshold    INTEGER   NOT NULL DEFAULT 5,
    last_updated       TIMESTAMP,
    copy_id            UUID      NOT NULL UNIQUE REFERENCES copy(id)
);

CREATE TABLE IF NOT EXISTS stock_movement (
    id            UUID        NOT NULL PRIMARY KEY,
    quantity      INTEGER     NOT NULL,
    movement_type VARCHAR(20) NOT NULL,
    reason        TEXT,
    movement_date TIMESTAMP   NOT NULL,
    copy_id       UUID        NOT NULL REFERENCES copy(id),
    order_id      UUID        REFERENCES orders(id)
);

CREATE INDEX IF NOT EXISTS idx_stock_movement_copy ON stock_movement(copy_id);
CREATE INDEX IF NOT EXISTS idx_stock_movement_order ON stock_movement(order_id);

CREATE TABLE IF NOT EXISTS order_line (
    id         UUID           NOT NULL PRIMARY KEY,
    order_id   UUID           NOT NULL REFERENCES orders(id),
    copy_id    UUID           NOT NULL REFERENCES copy(id),
    quantity   INTEGER        NOT NULL DEFAULT 1,
    unit_price DECIMAL(12,2)  NOT NULL DEFAULT 0,
    discount   DECIMAL(12,2)  NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_order_line_order ON order_line(order_id);
CREATE INDEX IF NOT EXISTS idx_order_line_copy ON order_line(copy_id);

CREATE TABLE IF NOT EXISTS review (
    id         UUID      NOT NULL PRIMARY KEY,
    book_id    UUID      NOT NULL REFERENCES book(id),
    client_id  UUID      NOT NULL REFERENCES client(id),
    rating     INTEGER   NOT NULL,
    comment    TEXT,
    valid      BOOLEAN   NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_review_book ON review(book_id);
CREATE INDEX IF NOT EXISTS idx_review_client ON review(client_id);

CREATE UNIQUE INDEX IF NOT EXISTS uk_review_book_client ON review(book_id, client_id);
