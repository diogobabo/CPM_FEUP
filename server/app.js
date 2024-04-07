const express = require('express');
const app = express();
const port = 3000;

// Import controller functions
const {
    registerCustomer,
    getNextPerformances,
    purchaseTickets,
    validateTickets,
    makeCafeteriaOrder,
    validateVouchers,
    consultTransactions,
    presentTickets,
    validateVouchersAndPayOrder
} = require('./controllers/controller.js');

// Define routes
app.get('/', (req, res) => {
    res.send('Hello World!');
});

// Routes for each controller
app.post('/register', registerCustomer);
app.get('/performances', getNextPerformances);
app.post('/purchase-tickets', purchaseTickets);
app.post('/validate-tickets', validateTickets);
app.post('/make-cafeteria-order', makeCafeteriaOrder);
app.post('/validate-vouchers', validateVouchers);
app.get('/consult-transactions', consultTransactions);
app.post('/present-tickets', presentTickets);
app.post('/validate-vouchers-and-pay-order', validateVouchersAndPayOrder);

// Start the server
app.listen(port, () => {
    console.log(`Example app listening on port ${port}`);
});