# Banking API Mock Server

This is a mock server implementation of the Banking API specification. It provides a complete set of endpoints for managing banking operations and customer data, along with schema information.

## Features

- Complete implementation of the Banking API specification
- Schema/model exposure through dedicated endpoints
- Mock data generation
- Swagger UI documentation
- Pagination support
- Filtering capabilities
- CORS enabled

## Setup

1. Create a virtual environment:
```bash
python -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate
```

2. Install dependencies:
```bash
pip install -r requirements.txt
```

3. Run the server:
```bash
python app/main.py
```

The server will start at `http://localhost:8000`

## API Documentation

Once the server is running, you can access:
- Swagger UI: http://localhost:8000/docs

## Available Endpoints

### Schema Endpoints
- GET /api/schemas - Get all available schemas
- GET /api/schemas/{schema_name} - Get specific schema by name

### Customer Endpoints
- GET /api/customers - List all customers
- POST /api/customers - Create new customer
- GET /api/customers/{customer_id} - Get customer details

### Account Endpoints
- GET /api/accounts - List all accounts
- GET /api/accounts?customer_id={customer_id} - List accounts for specific customer

### Card Endpoints
- GET /api/cards - List all cards
- GET /api/cards?customer_id={customer_id} - List cards for specific customer

### Transaction Endpoints
- GET /api/transactions - List all transactions
- GET /api/transactions?account_id={account_id} - List transactions for specific account

### Loan Endpoints
- GET /api/loans - List all loans
- GET /api/loans?customer_id={customer_id} - List loans for specific customer
- GET /api/loans?status={status} - List loans by status

## Mock Data

The server automatically generates mock data for:
- 10 customers
- 1-3 accounts per customer
- Cards for checking and savings accounts
- Associated transactions and loans

## Query Parameters

All list endpoints support:
- page: Page number (default: 1)
- limit: Items per page (default: 20, max: 100)

Additional filters are available for specific endpoints as documented in the API specification.

## Example Usage

1. Get all schemas:
```bash
curl http://localhost:8000/api/schemas
```

2. Get customer schema:
```bash
curl http://localhost:8000/api/schemas/Customer
```

3. List customers:
```bash
curl http://localhost:8000/api/customers?page=1&limit=10
```

4. Get customer details:
```bash
curl http://localhost:8000/api/customers/{customer_id}
```

## Development

The mock server is built using:
- FastAPI for the API framework
- Pydantic for data validation
- Faker for mock data generation
- PyYAML for OpenAPI specification parsing 