const uuid = require('uuid');
const sqlite3 = require('sqlite3').verbose();
const crypto = require('crypto');

// Connect to SQLite database
const db = new sqlite3.Database('./sqlite/db.db', (err) => {
    if (err) {
        console.error('Error connecting to database:', err)
    } else {
        console.log('Connected to the SQLite database')
    }
});


// Function to verify signature
const verifySignature = (user_id, signature) => {
    // Fetch the user's public key from the database based on the user_id
    const publicKey = fetchPublicKey(user_id); // Implement function to fetch public key from database

    // Extract the data that was signed from the user_id
    const data = user_id;

    // Verify the signature
    const verifier = crypto.createVerify('RSA-SHA256');
    verifier.update(data);

    const isVerified = verifier.verify(publicKey, signature, 'base64');
    
    return isVerified;
};

// Example function to fetch public key from database
const fetchPublicKey = (user_id) => {
    db.get('SELECT public_key FROM Customers WHERE user_id = ?', [user_id], (err, row) => {
        if (err) {
            console.error('Error fetching public key:', err);
            return null;
        }
        return row.public_key;
    });
};

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
    // Extract data from request body
    const { performance_id ,performance_date, number_of_tickets, user_id, signature } = req.body;

    // Validate the user signature
    if (!verifySignature(user_id, signature)) {
        return res.status(400).json({ error: 'Invalid signature' });
    }

    // Fetch performance details from the database
    const sql = `SELECT * FROM Performances WHERE date = ? and performance_id = ?`;
    db.get(sql, [performance_date, performance_id], (err, performance) => {
        if (err) {
            console.error('Error retrieving performance details:', err);
            return res.status(500).json({ error: 'Internal Server Error' });
        }
        
        if (!performance) {
            return res.status(404).json({ error: 'Performance not found' });
        }

        // Calculate total price
        const total_price = performance.price * number_of_tickets;

        // Assume payment is successful using the customer credit card
        // Generate unique ticket IDs
        const tickets = [];
        for (let i = 0; i < number_of_tickets; i++) {
            const ticket_id = uuid.v4();
            const place_in_room = crypto.randomInt(1, 100);
            tickets.push({ ticket_id, performance_id: performance.performance_id, user_id, place_in_room });
        }

        // Emit tickets and generate cafeteria vouchers
        const vouchers = [];
        tickets.forEach(ticket => {
            const voucher_id = uuid.v4();
            const type_code = Math.random() < 0.5 ? 'Free Coffee' : 'Popcorn'; // Randomly select voucher type
            vouchers.push({ voucher_id, user_id, type_code });
        });

        // Update database with tickets and vouchers
        const insertTicketSql = `INSERT INTO Tickets (ticket_id, performance_id, user_id, place_in_room) VALUES (?, ?, ?, ?)`;
        const insertVoucherSql = `INSERT INTO Vouchers (voucher_id, user_id, type_code) VALUES (?, ?, ?)`;
        const insertTransactionSql = `INSERT INTO Transactions (user_id, transaction_type, transaction_date, transaction_value) VALUES (?, ?, ?, ?)`;
        
        db.serialize(() => {
            db.run('BEGIN TRANSACTION');
            tickets.forEach(ticket => {
                db.run(insertTicketSql, [ticket.ticket_id, ticket.performance_id, ticket.user_id, ticket.place_in_room]);
            });
            vouchers.forEach(voucher => {
                db.run(insertVoucherSql, [voucher.voucher_id, voucher.user_id, voucher.type_code]);
            });
            db.run(insertTransactionSql, [user_id, 'Ticket Purchase', new Date().toISOString(), total_price], (err) => {
                if (err) {
                    console.error('Error inserting transaction:', err);
                    db.run('ROLLBACK');
                    return res.status(500).json({ error: 'Internal Server Error' });
                }

                db.run('COMMIT', (err) => {
                    if (err) {
                        console.error('Error committing transaction:', err);
                        return res.status(500).json({ error: 'Internal Server Error' });
                    }
                    
                    // Return success response with tickets and vouchers
                    res.status(200).json({ tickets, vouchers, total_price });
                });
            });
        });
    });
};


const validateTickets = (req, res) => {
    // Extract data from request body
    const { user_id, ticket_ids, performance_id, signature } = req.body;

    // Step 1: Verify the signature
    const isSignatureValid = verifySignature(user_id, signature); // Implement verifySignature function

    if (!isSignatureValid) {
        return res.status(400).json({ error: 'Invalid signature' });
    }

    // Step 2: Check the validity of tickets
    const isValidTickets = checkTicketValidity(user_id, ticket_ids, performance_id); // Implement checkTicketValidity function

    if (!isValidTickets) {
        return res.status(400).json({ error: 'Invalid tickets' });
    }

    // Step 3: Update the state of validated tickets in the database
    updateTicketState(ticket_ids); // Implement updateTicketState function

    // Step 4: Return validation result to client
    res.status(200).json({ validated: true });
};

// Function to check the validity of tickets
const checkTicketValidity = (user_id, ticket_ids, performance_id) => {
    ticket_ids.forEach(element => {
        db.get(`SELECT * FROM Tickets WHERE user_id = ? AND ticket_id = ? AND performance_id = ?`, [user_id, element, performance_id], (err, row) => {
            if (err) {
                console.error('Error checking ticket validity:', err);
                return false;
            }

            return row.length > 0;
        });
    });
};

// Function to update the state of validated tickets in the database
const updateTicketState = (ticket_ids) => {
    db.serialize(() => {
        db.run('BEGIN TRANSACTION');
        ticket_ids.forEach(ticket_id => {
            db.run(`UPDATE Tickets SET is_validated = 1 WHERE ticket_id = ?`, [ticket_id], (err) => {
                if (err) {
                    console.error('Error updating ticket state:', err);
                    db.run('ROLLBACK');
                }
            });
        });

        db.run('COMMIT', (err) => {
            if (err) {
                console.error('Error committing transaction:', err);
            }
        });
    });
};

const makeCafeteriaOrder = (req, res) => {
    // Extract data from request body
    const { user_id, items, vouchers, signature } = req.body;

    // Step 1: Verify the signature
    const isSignatureValid = verifySignature(user_id, signature); // Implement verifySignature function

    if (!isSignatureValid) {
        return res.status(400).json({ error: 'Invalid signature' });
    }

    // Step 2: Check the validity of items and vouchers
    const isValidItems = checkItemValidity(items); // Implement checkItemValidity function
    const isValidVouchers = checkVoucherValidity(vouchers); // Implement checkVoucherValidity function

    if (!isValidItems || !isValidVouchers) {
        return res.status(400).json({ error: 'Invalid items or vouchers' });
    }

    // Step 3: Calculate the total price of the order
    const totalPrice = calculateTotalPrice(items, vouchers); // Implement calculateTotalPrice function

    // Step 4: Update the quantity of items in the database
    updateItemQuantities(items); // Implement updateItemQuantities function

    // Step 5: Insert order information into the Orders table
    insertOrderIntoDatabase(user_id, items, totalPrice); // Implement insertOrderIntoDatabase function

    // Step 6: Delete used vouchers from the Vouchers table
    deleteUsedVouchers(vouchers); // Implement deleteUsedVouchers function

    // Step 7: Return order confirmation and total price to the client
    res.status(200).json({ order_confirmation: 'Order placed successfully', total_price: totalPrice });
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
