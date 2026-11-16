-- DevType Sample Schema: PostgreSQL 16+
-- Single source of truth for SQL, PL/SQL, and ORM snippet contexts

CREATE TABLE IF NOT EXISTS departments (
    department_id   SERIAL PRIMARY KEY,
    department_name VARCHAR(100) NOT NULL,
    location_id     INTEGER,
    manager_id      INTEGER,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS employees (
    employee_id     SERIAL PRIMARY KEY,
    first_name      VARCHAR(50) NOT NULL,
    last_name       VARCHAR(50) NOT NULL,
    email           VARCHAR(100) NOT NULL UNIQUE,
    phone_number    VARCHAR(20),
    hire_date       DATE NOT NULL DEFAULT CURRENT_DATE,
    job_id          VARCHAR(30) NOT NULL,
    salary          NUMERIC(10, 2) NOT NULL CHECK (salary >= 0),
    commission_pct  NUMERIC(4, 2),
    manager_id      INTEGER REFERENCES employees(employee_id),
    department_id   INTEGER REFERENCES departments(department_id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS customers (
    customer_id     SERIAL PRIMARY KEY,
    first_name      VARCHAR(50) NOT NULL,
    last_name       VARCHAR(50) NOT NULL,
    email           VARCHAR(100) NOT NULL UNIQUE,
    phone           VARCHAR(20),
    address         VARCHAR(255),
    city            VARCHAR(50),
    country         VARCHAR(50) NOT NULL DEFAULT 'Uzbekistan',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS products (
    product_id      SERIAL PRIMARY KEY,
    product_name    VARCHAR(120) NOT NULL,
    description     TEXT,
    category        VARCHAR(50) NOT NULL,
    unit_price      NUMERIC(10, 2) NOT NULL CHECK (unit_price >= 0),
    stock_quantity  INTEGER NOT NULL DEFAULT 0 CHECK (stock_quantity >= 0),
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS orders (
    order_id        SERIAL PRIMARY KEY,
    customer_id     INTEGER NOT NULL REFERENCES customers(customer_id),
    order_date      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    total_amount    NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS order_items (
    item_id         SERIAL PRIMARY KEY,
    order_id        INTEGER NOT NULL REFERENCES orders(order_id) ON DELETE CASCADE,
    product_id      INTEGER NOT NULL REFERENCES products(product_id),
    quantity        INTEGER NOT NULL CHECK (quantity > 0),
    unit_price      NUMERIC(10, 2) NOT NULL CHECK (unit_price >= 0),
    discount_pct    NUMERIC(4, 2) NOT NULL DEFAULT 0.00
);

CREATE TABLE IF NOT EXISTS payments (
    payment_id      SERIAL PRIMARY KEY,
    order_id        INTEGER NOT NULL REFERENCES orders(order_id),
    payment_date    TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    amount          NUMERIC(12, 2) NOT NULL CHECK (amount > 0),
    payment_method  VARCHAR(30) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'COMPLETED'
);

CREATE TABLE IF NOT EXISTS audit_log (
    log_id          BIGSERIAL PRIMARY KEY,
    table_name      VARCHAR(50) NOT NULL,
    action          VARCHAR(20) NOT NULL,
    entity_id       BIGINT,
    changed_by      VARCHAR(100) NOT NULL,
    changed_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    details         TEXT
);
