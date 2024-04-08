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

CREATE TABLE Intems (
    item_id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT,
    quantity INTEGER,
    price REAL
);

CREATE TABLE Orders (
    order_id INTEGER,
    user_id TEXT,
    order_date TEXT,
    intem INTEGER,
    FOREIGN KEY (intem) REFERENCES Intems(item_id),
    FOREIGN KEY (user_id) REFERENCES Customers(user_id)
);

-- Inserting data into Customers table
INSERT INTO Customers (user_id, name, nif, credit_card_type, credit_card_number, credit_card_validity, public_key)
VALUES 
    ('1', 'John Doe', '123456789', 'Visa', '1234567890123456', '12/25', 'public_key_1'),
    ('2', 'Jane Smith', '987654321', 'Mastercard', '6543210987654321', '06/24', 'public_key_2');

-- Inserting data into Performances table
INSERT INTO Performances (name, date, price)
VALUES 
    ('Concert', '2024-04-10', 25.00),
    ('Theater Play', '2024-04-12', 30.00);

-- Inserting data into Tickets table
INSERT INTO Tickets (ticket_id, performance_id, user_id, place_in_room)
VALUES 
    ('ticket_1', 1, '1', 'A1'),
    ('ticket_2', 1, '2', 'B2');

-- Inserting data into Vouchers table
INSERT INTO Vouchers (voucher_id, user_id, type_code)
VALUES 
    ('voucher_1', '1', 'Free Coffee'),
    ('voucher_2', '2', 'Popcorn');

-- Inserting data into Transactions table
INSERT INTO Transactions (user_id, transaction_type, transaction_date, transaction_value)
VALUES 
    ('1', 'Ticket Purchase', '2024-04-10', 25.00),
    ('2', 'Ticket Purchase', '2024-04-12', 30.00);

INSERT INTO Intems (name, quantity, price)
VALUES 
    ('Coke', 2, 1.50),
    ('Popcorn', 1, 2.00);

INSERT INTO Orders (order_id, user_id, order_date, intem)
VALUES
    (1, '1', '2024-04-10', 1),
    (2, '2', '2024-04-12', 2);