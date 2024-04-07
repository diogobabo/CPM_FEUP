const uuid = require('uuid');
const sqlite3 = require('sqlite3').verbose();

// Connect to SQLite database
const db = new sqlite3.Database('./sqlite/db.sqlite');

// Controller functions

const registerCustomer = (req, res) => {
    // Extract data from request body
    const { name, nif, credit_card_type, credit_card_number, credit_card_validity, public_key } = req.body;

    // Generate user id and private key
    const user_id = uuid.v4();
    const private_key = generatePrivateKey(); // Implement function to generate private key

    // Insert customer data into database
    const sql = `INSERT INTO Customers (user_id, name, nif, credit_card_type, credit_card_number, credit_card_validity, public_key)
                 VALUES (?, ?, ?, ?, ?, ?, ?)`;
    db.run(sql, [user_id, name, nif, credit_card_type, credit_card_number, credit_card_validity, public_key], (err) => {
        if (err) {
            console.error('Error registering customer:', err);
            res.status(500).json({ error: 'Internal Server Error' });
        } else {
            res.status(200).json({ user_id });
        }
    });
};

const getNextPerformances = (req, res) => {
    // Retrieve next performances from database
    const sql = `SELECT * FROM Performances WHERE date >= date('now') LIMIT 4`;
    db.all(sql, (err, rows) => {
        if (err) {
            console.error('Error retrieving performances:', err);
            res.status(500).json({ error: 'Internal Server Error' });
        } else {
            res.status(200).json(rows);
        }
    });
};

const purchaseTickets = (req, res) => {
    // Implement purchase tickets logic here
};

const validateTickets = (req, res) => {
    // Implement validate tickets logic here
};

const makeCafeteriaOrder = (req, res) => {
    // Implement make cafeteria order logic here
};

const validateVouchers = (req, res) => {
    // Implement validate vouchers logic here
};

const consultTransactions = (req, res) => {
    // Implement consult transactions logic here
};

const presentTickets = (req, res) => {
    // Implement presenting tickets logic here
};

const validateVouchersAndPayOrder = (req, res) => {
    // Extract data from request body
    const { user_id, ordered_products, vouchers } = req.body;

    // Implement validation of vouchers and calculation of final price
    let totalDiscount = 0;
    let totalOrderValue = calculateOrderValue(ordered_products); // Implement function to calculate order value
    let acceptedVouchers = [];

    // Check and validate each voucher
    vouchers.forEach(voucher => {
        if (!voucher.is_used) {
            if (voucher.type_code === '5% Discount') {
                if (totalDiscount === 0) {
                    totalDiscount += 0.05 * totalOrderValue;
                    acceptedVouchers.push(voucher.voucher_id);
                }
            } else {
                acceptedVouchers.push(voucher.voucher_id);
            }
        }
    });

    // Calculate final price
    const finalPrice = totalOrderValue - totalDiscount;

    // Mark accepted vouchers as used
    acceptedVouchers.forEach(voucher_id => {
        // Update voucher state in database
        db.run(`UPDATE Vouchers SET is_used = 1 WHERE voucher_id = ?`, [voucher_id], (err) => {
            if (err) {
                console.error('Error updating voucher:', err);
            }
        });
    });

    // Generate a new 5% discount voucher if applicable
    if ((totalOrderValue + finalPrice) >= 200) {
        const newVoucherId = uuid.v4();
        // Insert new voucher into database
        db.run(`INSERT INTO Vouchers (voucher_id, user_id, type_code, is_used) VALUES (?, ?, ?, ?)`, [newVoucherId, user_id, '5% Discount', 0], (err) => {
            if (err) {
                console.error('Error generating new voucher:', err);
            }
        });
    }

    // Return validation result and final price to client
    res.status(200).json({ validated: true, final_price: finalPrice, accepted_vouchers: acceptedVouchers });
};

// Export controller functions
module.exports = {
    registerCustomer,
    getNextPerformances,
    purchaseTickets,
    validateTickets,
    makeCafeteriaOrder,
    validateVouchers,
    consultTransactions,
    presentTickets,
    validateVouchersAndPayOrder
};
