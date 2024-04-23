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
async function verifySignature (data, user_id, signature) {
    // Fetch the user's public key from the database based on the user_id

    const publicKeyBase64 = await fetchPublicKey(user_id); // Implement function to fetch public key from database
    console.log(publicKeyBase64);
    const pemKey = `-----BEGIN PUBLIC KEY-----
${publicKeyBase64}-----END PUBLIC KEY-----`;

    // Verify the signature
    const verifier = crypto.createVerify('RSA-SHA256');
    verifier.update(data);

    try {
        const isVerified = verifier.verify(pemKey, signature, 'base64');
        console.log('Signature verified:', isVerified);
        return isVerified;
    } catch (err) {
        console.error('Error verifying signature:', err);
        return false;
}
};

// Example function to fetch public key from database
function fetchPublicKey(user_id) {
    return new Promise((resolve, reject) => {
        db.get('SELECT * FROM Customers WHERE user_id = ?', [user_id], (err, row) => {
            if (err) {
                console.error('Error fetching public key:', err);
                reject(err);
                return;
            }
            
            if (row) {
                resolve(row.public_key);
            } else {
                resolve(null); // Return null if public key not found
            }
        });
    });
}

// Controller functions

const registerCustomer = (req, res) => {
    // Extract data from request body
    console.log(req.body);
    const { name, nif, creditCardType, creditCardNumber, creditCardValidity, publicKey } = req.body;

    // Generate user id and private key
    const user_id = uuid.v4();

    // Insert customer data into database
    const sql = `INSERT INTO Customers (user_id, name, nif, credit_card_type, credit_card_number, credit_card_validity, public_key)
                 VALUES (?, ?, ?, ?, ?, ?, ?)`;
    db.run(sql, [user_id, name, nif, creditCardType, creditCardNumber, creditCardValidity, publicKey], (err) => {
        if (err) {
            console.error('Error registering customer:', err);
            res.status(500).json({ error: 'Internal Server Error' });
        } else {
            console.log('Customer registered successfully');
            res.status(200).json({ user_id: user_id });
        }
    });
};

const getNextPerformances = (req, res) => {
    // Retrieve next performances from database
    const sql = `SELECT * FROM Performances LIMIT 4`;
    db.all(sql, (err, rows) => {
        if (err) {
            console.error('Error retrieving performances:', err);
            res.status(500).json({ error: 'Internal Server Error' });
        } else {
            console.log(rows);
            res.status(200).json(rows);
        }
    });
};

async function purchaseTickets (req, res) {
    // Extract data from request body

    const { performance_id ,performance_date, number_of_tickets, user_id, signature } = req.body;
    const jsondata = JSON.stringify({user_id, performance_id ,performance_date, number_of_tickets});
    const jsonByteArray = Buffer.from(jsondata);

    const isSignatureValid = await verifySignature(jsonByteArray, user_id, signature);
    if (!isSignatureValid) {
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
                    res.status(200).json({ "tickets":tickets, "vouchers":vouchers, "total_price":total_price });
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

    if (isValidTickets === 0) {
        return res.status(400).json({ error: 'Invalid tickets' });
    }

    else if (isValidTickets === 1) {
        return res.status(200).json({ error: 'Ticket already used' });
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
                return 0;
            }

            if (row.length === 0) {
                return 0;
            }
            else if (row.is_used === 1) {
                return 1;
            }
            return 2;
        });
    });
};

// Function to update the state of validated tickets in the database
const updateTicketState = (ticket_ids) => {
    db.serialize(() => {
        db.run('BEGIN TRANSACTION');
        ticket_ids.forEach(ticket_id => {
            db.run(`UPDATE Tickets SET is_used = 1 WHERE ticket_id = ?`, [ticket_id], (err) => {
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

const checkItemValidity = async (items) => {
    let isValid = true;
    console.log(items);

    // Convert object to array of key-value pairs
    const itemEntries = Object.entries(items);

    // Use async/await to wait for all database queries to complete
    const promises = itemEntries.map(async ([itemId, quantity]) => {
        return new Promise((resolve, reject) => {
            db.get(`SELECT * FROM Intems WHERE item_id = ? AND quantity >= ?`, [itemId, quantity], (err, row) => {
                if (err) {
                    console.error('Error checking item validity:', err);
                    reject(err);
                    return;
                }

                if (row) {
                    resolve(true); // Item is valid
                } else {
                    resolve(false); // Item is invalid
                }
            });
        });
    });

    // Wait for all promises to resolve
    try {
        const results = await Promise.all(promises);
        // Check if any item is invalid
        isValid = results.every(result => result === true);
        return isValid;
    } catch (err_1) {
        console.error('Error in promises:', err_1);
        return false;
    }
};

const checkVoucherValidity = (vouchers, user_id) => {
    let isValid = true;
    if(vouchers === undefined || vouchers.length === 0) {
        return [];
    }
    if (vouchers.length > 2) {
        return [];
    }
    let validVoucher = [];

    vouchers.forEach(voucher => {
        db.get(`SELECT * FROM Vouchers WHERE voucher_id = ? AND is_used = 0 AND user_id = ?`, [voucher.voucher_id, user_id], (err, row) => {
            if (err) {
                console.error('Error checking voucher validity:', err);
                isValid = false;
            }

            if (row.length > 0){
                validVoucher.push(voucher);
            }
        })
    });

    return validVoucher
};

const calculateTotalPrice = async (items, vouchers) => {
    let totalPrice = 0;
    let itemPromises = [];

    // Calculate total price for each item
    for (const [itemId, quantity] of Object.entries(items)) {
        itemPromises.push(new Promise((resolve, reject) => {
            db.get(`SELECT * FROM Intems WHERE item_id = ? AND quantity >= ?`, [itemId, quantity], (err, row) => {
                if (err) {
                    console.error('Error calculating total price:', err);
                    reject(err);
                    return;
                }

                if (row) {
                    totalPrice += row.price * quantity;
                    resolve();
                } else {
                    reject(`Item with ID ${itemId} and quantity ${quantity} not found or insufficient quantity.`);
                }
            });
        }));
    }

    // Calculate total price for vouchers
    if (vouchers.length == 0) {
        try {
            await Promise.all([...itemPromises]);
            return totalPrice;
        } catch (err_1) {
            console.error('Error in promises:', err_1);
            return 0;
        }
    }

    const voucherPromise = new Promise((resolve, reject) => {
        db.get(`SELECT * FROM Vouchers WHERE voucher_id = ?`, [vouchers.voucher_id], (err, row) => {
            if (err) {
                console.error('Error calculating total price:', err);
                reject(err);
                return;
            }

            if (row && row.type_code === '5') {
                totalPrice *= 0.95; // Apply 5% discount
            }

            resolve();
        });
    });

    // Wait for all promises to resolve
    try {
        await Promise.all([...itemPromises, voucherPromise]);
        return totalPrice;
    } catch (err_3) {
        console.error('Error in promises:', err_3);
        return 0;
    }
};


const updateItemQuantities = (items) => {
    db.serialize(() => {
        db.run('BEGIN TRANSACTION');

        // Iterate through each item in the items object
        for (const [itemId, quantity] of Object.entries(items)) {
            db.run(`UPDATE Intems SET quantity = quantity - ? WHERE item_id = ?`, [quantity, itemId], (err) => {
                if (err) {
                    console.error('Error updating item quantities:', err);
                    db.run('ROLLBACK');
                    return;
                }
            });
        }

        db.run('COMMIT', (err) => {
            if (err) {
                console.error('Error committing transaction:', err);
            }
        });
    });
};


const insertOrderIntoDatabase = (user_id, items) => {
    let lastID = 0;
    db.get(`SELECT MAX(order_id) as lastID FROM Orders`, (err, row) => {
        if (err) {
            console.error('Error inserting order into database:', err);
        }

        lastID = row.lastID;

        db.serialize(() => {
        db.run('BEGIN TRANSACTION');

        // Iterate through each item in the items object
        for (const [itemId, quantity] of Object.entries(items)) {
            db.run(`INSERT INTO Orders (order_id, user_id, intem, quantity, order_date) VALUES (?, ?, ?, ?, ?)`, [lastID + 1, user_id, itemId, quantity, new Date().toISOString()], (err) => {
                if (err) {
                    console.error('Error inserting order into database:', err);
                    db.run('ROLLBACK');
                    return;
                }
            });
        }

        db.run('COMMIT', (err) => {
            if (err) {
                console.error('Error committing transaction:', err);
            }
            return lastID + 1;
        });
    });
    });

    
};


const deleteUsedVouchers = (vouchers) => {
    db.serialize(() => {
        db.run('BEGIN TRANSACTION');
        vouchers.forEach(voucher => {
            db.run(`UPDATE Vouchers SET is_used = 1 WHERE voucher_id = ?`, [voucher.voucher_id], (err) => {
                if (err) {
                    console.error('Error deleting used vouchers:', err);
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

async function makeCafeteriaOrder (req, res) {
    // Extract data from request body
    console.log(req.body);
    const { user_id, selectedItems, signature } = req.body;
    let { vouchers } = req.body;
    if (vouchers === undefined) {
        vouchers = [];
    }

    const jsondata = JSON.stringify({selectedItems});
    const jsonByteArray = Buffer.from(jsondata);
    
    const isSignatureValid = await verifySignature(jsonByteArray, user_id, signature);

    if (!isSignatureValid) {
        return res.status(400).json({ error: 'Invalid signature' });
    }

    // Step 2: Check the validity of items and vouchers
    const isValidItems = await checkItemValidity(selectedItems); // Implement checkItemValidity function
    const validVouchers = checkVoucherValidity(vouchers, user_id); // Implement checkVoucherValidity function

    if (!isValidItems) {
        return res.status(400).json({ error: 'Invalid items or vouchers' });
    }

    // Step 3: Calculate the total price of the order
    const totalPrice = await calculateTotalPrice(selectedItems, vouchers); // Implement calculateTotalPrice function

    // Step 4: Update the quantity of items in the database
    updateItemQuantities(selectedItems); // Implement updateItemQuantities function

    // Step 5: Insert order information into the Orders table
    const order_id = insertOrderIntoDatabase(user_id, selectedItems); // Implement insertOrderIntoDatabase function

    // Step 6: Delete used vouchers from the Vouchers table
    deleteUsedVouchers(vouchers); // Implement deleteUsedVouchers function

    // Step 7: Return order confirmation and total price to the client
    res.status(200).json({ order_confirmation: 'Order placed successfully', total_price: totalPrice, order_id: order_id});
};


const consultTransactions = (req, res) => {
    const { user_id } = req.body;

    let transactions = [];
    let orders = [];
    let vouchers = [];
    let intems = [];


    db.get(`SELECT * FROM Transactions WHERE user_id = ?`, [user_id], (err, row) => {
        if (err) {
            console.error('Error consulting transactions:', err);
            return res.status(500).json({ error: 'Internal Server Error' });
        }

        transactions = row;
    });

    db.get(`SELECT * FROM Orders WHERE user_id = ?`, [user_id], (err, row) => {
        if (err) {
            console.error('Error consulting transactions:', err);
            return res.status(500).json({ error: 'Internal Server Error' });
        }

        orders = row;
    });

    db.get(`SELECT * FROM Vouchers WHERE user_id = ?`, [user_id], (err, row) => {
        if (err) {
            console.error('Error consulting transactions:', err);
            return res.status(500).json({ error: 'Internal Server Error' });
        }

        vouchers = row;
    });

    db.get(`SELECT * FROM Items `, [], (err, row) => {
        if (err) {
            console.error('Error consulting transactions:', err);
            return res.status(500).json({ error: 'Internal Server Error' });
        }

        intems = row;
    });


    res.status(200).json({ transactions, orders, vouchers, intems });
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

const getItems = (req, res) => {
    db.all(`SELECT * FROM Intems`, [], (err, rows) => {
        if (err) {
            console.error('Error retrieving items:', err);
            return res.status(500).json({ error: 'Internal Server Error' });
        }

        res.status(200).json(rows);
    });
}


// Export controller functions
module.exports = {
    registerCustomer,
    getNextPerformances,
    purchaseTickets,
    validateTickets,
    makeCafeteriaOrder,
    consultTransactions,
    validateVouchersAndPayOrder,
    getItems
};
