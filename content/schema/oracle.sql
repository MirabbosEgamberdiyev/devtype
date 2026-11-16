-- DevType Sample Schema: Oracle Database 19c+ / 23ai Free
-- Single source of truth for Oracle SQL & PL/SQL exercises

CREATE TABLE departments (
    department_id   NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    department_name VARCHAR2(100) NOT NULL,
    location_id     NUMBER,
    manager_id      NUMBER,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT SYSTIMESTAMP NOT NULL
);
/

CREATE TABLE employees (
    employee_id     NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    first_name      VARCHAR2(50) NOT NULL,
    last_name       VARCHAR2(50) NOT NULL,
    email           VARCHAR2(100) NOT NULL UNIQUE,
    phone_number    VARCHAR2(20),
    hire_date       DATE DEFAULT SYSDATE NOT NULL,
    job_id          VARCHAR2(30) NOT NULL,
    salary          NUMBER(10, 2) NOT NULL CHECK (salary >= 0),
    commission_pct  NUMBER(4, 2),
    manager_id      NUMBER REFERENCES employees(employee_id),
    department_id   NUMBER REFERENCES departments(department_id),
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT SYSTIMESTAMP NOT NULL
);
/

CREATE TABLE customers (
    customer_id     NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    first_name      VARCHAR2(50) NOT NULL,
    last_name       VARCHAR2(50) NOT NULL,
    email           VARCHAR2(100) NOT NULL UNIQUE,
    phone           VARCHAR2(20),
    address         VARCHAR2(255),
    city            VARCHAR2(50),
    country         VARCHAR2(50) DEFAULT 'Uzbekistan' NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT SYSTIMESTAMP NOT NULL
);
/

CREATE TABLE products (
    product_id      NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    product_name    VARCHAR2(120) NOT NULL,
    description     VARCHAR2(4000),
    category        VARCHAR2(50) NOT NULL,
    unit_price      NUMBER(10, 2) NOT NULL CHECK (unit_price >= 0),
    stock_quantity  NUMBER(8) DEFAULT 0 NOT NULL CHECK (stock_quantity >= 0),
    is_active       NUMBER(1) DEFAULT 1 NOT NULL CHECK (is_active IN (0, 1)),
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT SYSTIMESTAMP NOT NULL
);
/

CREATE TABLE orders (
    order_id        NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    customer_id     NUMBER NOT NULL REFERENCES customers(customer_id),
    order_date      TIMESTAMP WITH TIME ZONE DEFAULT SYSTIMESTAMP NOT NULL,
    status          VARCHAR2(20) DEFAULT 'PENDING' NOT NULL,
    total_amount    NUMBER(12, 2) DEFAULT 0.00 NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT SYSTIMESTAMP NOT NULL
);
/

CREATE TABLE order_items (
    item_id         NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id        NUMBER NOT NULL REFERENCES orders(order_id) ON DELETE CASCADE,
    product_id      NUMBER NOT NULL REFERENCES products(product_id),
    quantity        NUMBER(6) NOT NULL CHECK (quantity > 0),
    unit_price      NUMBER(10, 2) NOT NULL CHECK (unit_price >= 0),
    discount_pct    NUMBER(4, 2) DEFAULT 0.00 NOT NULL
);
/

CREATE TABLE payments (
    payment_id      NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id        NUMBER NOT NULL REFERENCES orders(order_id),
    payment_date    TIMESTAMP WITH TIME ZONE DEFAULT SYSTIMESTAMP NOT NULL,
    amount          NUMBER(12, 2) NOT NULL CHECK (amount > 0),
    payment_method  VARCHAR2(30) NOT NULL,
    status          VARCHAR2(20) DEFAULT 'COMPLETED' NOT NULL
);
/

CREATE TABLE audit_log (
    log_id          NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    table_name      VARCHAR2(50) NOT NULL,
    action          VARCHAR2(20) NOT NULL,
    entity_id       NUMBER,
    changed_by      VARCHAR2(100) NOT NULL,
    changed_at      TIMESTAMP WITH TIME ZONE DEFAULT SYSTIMESTAMP NOT NULL,
    details         VARCHAR2(4000)
);
/
