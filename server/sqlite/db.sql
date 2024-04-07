DROP TABLE IF EXISTS Transactions;
DROP TABLE IF EXISTS Vouchers;
DROP TABLE IF EXISTS Tickets;
DROP TABLE IF EXISTS Performances;
DROP TABLE IF EXISTS Customers;

-- Create Customers table
CREATE TABLE Customers (
    user_id TEXT PRIMARY KEY,
    name TEXT,
    nif TEXT,
    credit_card_type TEXT,
    credit_card_number TEXT,
    credit_card_validity TEXT,
    public_key TEXT UNIQUE
);

-- Create Performances table
CREATE TABLE Performances (
    performance_id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT,
    date TEXT,
    price REAL
);

-- Create Tickets table
CREATE TABLE Tickets (
    ticket_id TEXT PRIMARY KEY,
    performance_id INTEGER,
    user_id TEXT,
    place_in_room TEXT,
    is_used INTEGER DEFAULT 0,
    FOREIGN KEY (performance_id) REFERENCES Performances(performance_id),
    FOREIGN KEY (user_id) REFERENCES Customers(user_id)
);

-- Create Vouchers table
CREATE TABLE Vouchers (
    voucher_id TEXT PRIMARY KEY,
    user_id TEXT,
    type_code TEXT,
    is_used INTEGER DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES Customers(user_id)
);

-- Create Transactions table
CREATE TABLE Transactions (
    transaction_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id TEXT,
    transaction_type TEXT,
    transaction_date TEXT,
    transaction_value REAL,
    FOREIGN KEY (user_id) REFERENCES Customers(user_id)
);
