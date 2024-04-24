const express = require('express');
const app = express();
const port = 3000;

// Middleware to parse JSON requests
app.use(express.json());

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
    validateVouchersAndPayOrder,
    getItems,
    getTickets
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
app.get('/consult-transactions', consultTransactions);
app.post('/validate-vouchers-and-pay-order', validateVouchersAndPayOrder);
app.get('/items',getItems);
app.post('/tickets', getTickets);

// Start the server
app.listen(port, () => {
    console.log(`Example app listening on port ${port}`);
});
