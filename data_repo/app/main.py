from fastapi import FastAPI, HTTPException, Depends, Query
from fastapi.middleware.cors import CORSMiddleware
from fastapi.openapi.utils import get_openapi
from typing import List, Optional, Dict, Any, Generic, TypeVar
from datetime import datetime, date
import uuid
from pydantic import BaseModel, EmailStr, Field, constr
from faker import Faker
import json
import yaml

# Generic type for paginated responses
T = TypeVar('T')

class PaginatedResponse(BaseModel, Generic[T]):
    data: List[T]
    total: int
    page: int
    limit: int

    class Config:
        schema_extra = {
            "example": {
                "data": [],
                "total": 0,
                "page": 1,
                "limit": 20
            }
        }

# Pydantic Models
class Address(BaseModel):
    street: str
    city: str
    state: Optional[str] = None
    country: constr(min_length=2, max_length=2)
    postalCode: str

    class Config:
        schema_extra = {
            "example": {
                "street": "123 Main St",
                "city": "New York",
                "state": "NY",
                "country": "US",
                "postalCode": "10001"
            }
        }

class Customer(BaseModel):
    id: Optional[str] = None
    customerType: str = Field(..., enum=["PERSONAL", "BUSINESS"])
    firstName: str
    lastName: str
    email: EmailStr
    phone: Optional[str] = None
    dateOfBirth: Optional[date] = None
    address: Address
    kycStatus: str = Field(..., enum=["PENDING", "VERIFIED", "REJECTED"])
    riskProfile: str = Field(..., enum=["LOW", "MEDIUM", "HIGH"])
    status: str = Field(..., enum=["ACTIVE", "INACTIVE", "BLOCKED"])
    createdAt: Optional[datetime] = None
    updatedAt: Optional[datetime] = None

    class Config:
        schema_extra = {
            "example": {
                "customerType": "PERSONAL",
                "firstName": "John",
                "lastName": "Doe",
                "email": "john.doe@example.com",
                "phone": "+1234567890",
                "dateOfBirth": "1990-01-01",
                "address": {
                    "street": "123 Main St",
                    "city": "New York",
                    "state": "NY",
                    "country": "US",
                    "postalCode": "10001"
                },
                "kycStatus": "VERIFIED",
                "riskProfile": "LOW",
                "status": "ACTIVE"
            }
        }

class Account(BaseModel):
    id: Optional[str] = None
    customerId: str
    accountType: str = Field(..., enum=["CHECKING", "SAVINGS", "INVESTMENT", "FIXED_DEPOSIT"])
    accountNumber: str
    currency: str
    balance: float
    availableBalance: float
    interestRate: float
    status: str = Field(..., enum=["ACTIVE", "FROZEN", "CLOSED"])
    openedDate: date
    lastActivityDate: datetime

    class Config:
        schema_extra = {
            "example": {
                "customerId": "123e4567-e89b-12d3-a456-426614174000",
                "accountType": "CHECKING",
                "accountNumber": "1234567890",
                "currency": "USD",
                "balance": 1000.00,
                "availableBalance": 1000.00,
                "interestRate": 0.01,
                "status": "ACTIVE",
                "openedDate": "2023-01-01",
                "lastActivityDate": "2023-01-01T00:00:00"
            }
        }

class Card(BaseModel):
    id: Optional[str] = None
    customerId: str
    accountId: str
    cardType: str = Field(..., enum=["DEBIT", "CREDIT", "PREPAID"])
    cardNumber: str
    expiryDate: date
    cvv: str
    status: str = Field(..., enum=["ACTIVE", "BLOCKED", "EXPIRED"])
    dailyLimit: float
    monthlyLimit: float
    creditLimit: Optional[float] = None
    issuedDate: date

    class Config:
        schema_extra = {
            "example": {
                "customerId": "123e4567-e89b-12d3-a456-426614174000",
                "accountId": "123e4567-e89b-12d3-a456-426614174001",
                "cardType": "DEBIT",
                "cardNumber": "4111111111111111",
                "expiryDate": "2025-12-31",
                "cvv": "123",
                "status": "ACTIVE",
                "dailyLimit": 1000.00,
                "monthlyLimit": 10000.00,
                "issuedDate": "2023-01-01"
            }
        }

class Transaction(BaseModel):
    id: Optional[str] = None
    accountId: str
    type: str = Field(..., enum=["DEPOSIT", "WITHDRAWAL", "TRANSFER", "PAYMENT"])
    amount: float
    currency: str
    description: Optional[str] = None
    status: str = Field(..., enum=["PENDING", "COMPLETED", "FAILED", "REVERSED"])
    timestamp: datetime
    referenceId: Optional[str] = None
    metadata: Optional[Dict[str, Any]] = None

    class Config:
        schema_extra = {
            "example": {
                "accountId": "123e4567-e89b-12d3-a456-426614174001",
                "type": "DEPOSIT",
                "amount": 100.00,
                "currency": "USD",
                "description": "Initial deposit",
                "status": "COMPLETED",
                "timestamp": "2023-01-01T00:00:00"
            }
        }

class LoanPayment(BaseModel):
    dueDate: date
    amount: float
    status: str = Field(..., enum=["PENDING", "PAID", "OVERDUE"])
    paidDate: Optional[date] = None

    class Config:
        schema_extra = {
            "example": {
                "dueDate": "2023-01-01",
                "amount": 1000.00,
                "status": "PENDING"
            }
        }

class Loan(BaseModel):
    id: Optional[str] = None
    customerId: str
    type: str = Field(..., enum=["PERSONAL", "MORTGAGE", "BUSINESS", "AUTO"])
    amount: float
    currency: str
    interestRate: float
    term: int
    status: str = Field(..., enum=["PENDING", "ACTIVE", "PAID", "DEFAULTED"])
    startDate: date
    endDate: date
    remainingBalance: float
    paymentSchedule: List[LoanPayment]

    class Config:
        schema_extra = {
            "example": {
                "customerId": "123e4567-e89b-12d3-a456-426614174000",
                "type": "PERSONAL",
                "amount": 10000.00,
                "currency": "USD",
                "interestRate": 0.05,
                "term": 12,
                "status": "ACTIVE",
                "startDate": "2023-01-01",
                "endDate": "2023-12-31",
                "remainingBalance": 10000.00,
                "paymentSchedule": [
                    {
                        "dueDate": "2023-01-01",
                        "amount": 1000.00,
                        "status": "PENDING"
                    }
                ]
            }
        }

# Load OpenAPI specification
with open("banking-api.yaml", "r") as f:
    openapi_spec = yaml.safe_load(f)

def custom_openapi():
    if app.openapi_schema:
        return app.openapi_schema
    
    # Get the base OpenAPI schema from FastAPI
    openapi_schema = get_openapi(
        title=openapi_spec["info"]["title"],
        version=openapi_spec["info"]["version"],
        description=openapi_spec["info"]["description"],
        routes=app.routes,
    )
    
    # Merge the components from our YAML specification
    if "components" in openapi_spec:
        if "components" not in openapi_schema:
            openapi_schema["components"] = {}
        
        # Merge schemas
        if "schemas" in openapi_spec["components"]:
            if "schemas" not in openapi_schema["components"]:
                openapi_schema["components"]["schemas"] = {}
            openapi_schema["components"]["schemas"].update(openapi_spec["components"]["schemas"])
        
        # Merge security schemes
        if "securitySchemes" in openapi_spec["components"]:
            if "securitySchemes" not in openapi_schema["components"]:
                openapi_schema["components"]["securitySchemes"] = {}
            openapi_schema["components"]["securitySchemes"].update(openapi_spec["components"]["securitySchemes"])
    
    # Merge paths from YAML if they exist
    if "paths" in openapi_spec:
        openapi_schema["paths"].update(openapi_spec["paths"])
    
    # Merge tags if they exist
    if "tags" in openapi_spec:
        openapi_schema["tags"] = openapi_spec["tags"]
    
    # Merge servers if they exist
    if "servers" in openapi_spec:
        openapi_schema["servers"] = openapi_spec["servers"]
    
    app.openapi_schema = openapi_schema
    return app.openapi_schema

app = FastAPI(
    title=openapi_spec["info"]["title"],
    description=openapi_spec["info"]["description"],
    version=openapi_spec["info"]["version"],
    openapi_url="/openapi.json",
    docs_url="/docs",
)

# Set custom OpenAPI schema
app.openapi = custom_openapi

# Add CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Initialize Faker
fake = Faker()

# Mock database
db = {
    "customers": [],
    "accounts": [],
    "cards": [],
    "transactions": [],
    "loans": []
}

# Generate mock data
def generate_mock_data():
    # Generate customers
    for _ in range(10):
        customer = Customer(
            id=str(uuid.uuid4()),
            customerType=fake.random_element(["PERSONAL", "BUSINESS"]),
            firstName=fake.first_name(),
            lastName=fake.last_name(),
            email=fake.email(),
            phone=fake.phone_number(),
            dateOfBirth=fake.date_of_birth(),
            address=Address(
                street=fake.street_address(),
                city=fake.city(),
                state=fake.state(),
                country=fake.country_code(),
                postalCode=fake.postcode()
            ),
            kycStatus=fake.random_element(["PENDING", "VERIFIED", "REJECTED"]),
            riskProfile=fake.random_element(["LOW", "MEDIUM", "HIGH"]),
            status="ACTIVE",
            createdAt=datetime.now(),
            updatedAt=datetime.now()
        )
        db["customers"].append(customer.dict())

        # Generate accounts for each customer
        for _ in range(fake.random_int(1, 3)):
            account = Account(
                id=str(uuid.uuid4()),
                customerId=customer.id,
                accountType=fake.random_element(["CHECKING", "SAVINGS", "INVESTMENT", "FIXED_DEPOSIT"]),
                accountNumber=fake.credit_card_number(),
                currency="USD",
                balance=float(fake.random_int(1000, 100000)),
                availableBalance=float(fake.random_int(1000, 100000)),
                interestRate=float(fake.random_int(1, 5)),
                status="ACTIVE",
                openedDate=fake.date_this_year(),
                lastActivityDate=datetime.now()
            )
            db["accounts"].append(account.dict())

            # Generate cards for each account
            if account.accountType in ["CHECKING", "SAVINGS"]:
                card = Card(
                    id=str(uuid.uuid4()),
                    customerId=customer.id,
                    accountId=account.id,
                    cardType=fake.random_element(["DEBIT", "CREDIT"]),
                    cardNumber=fake.credit_card_number(),
                    expiryDate=fake.future_date(),
                    cvv=str(fake.random_int(100, 999)),
                    status="ACTIVE",
                    dailyLimit=float(fake.random_int(1000, 5000)),
                    monthlyLimit=float(fake.random_int(10000, 50000)),
                    creditLimit=float(fake.random_int(5000, 20000)) if account.accountType == "CREDIT" else None,
                    issuedDate=fake.date_this_year()
                )
                db["cards"].append(card.dict())

# Generate initial mock data
generate_mock_data()

# Schema exposure endpoints
@app.get("/api/schemas", tags=["Schemas"])
async def get_all_schemas():
    """Get all available schemas"""
    return openapi_spec["components"]["schemas"]

@app.get("/api/schemas/{schema_name}", tags=["Schemas"])
async def get_schema(schema_name: str):
    """Get specific schema by name"""
    if schema_name in openapi_spec["components"]["schemas"]:
        return openapi_spec["components"]["schemas"][schema_name]
    raise HTTPException(status_code=404, detail="Schema not found")

# Customer endpoints
@app.get("/api/customers", response_model=PaginatedResponse[Customer], tags=["Customers"])
async def list_customers(
    page: int = Query(1, ge=1),
    limit: int = Query(20, ge=1, le=100)
):
    """List all customers with pagination"""
    start = (page - 1) * limit
    end = start + limit
    return {
        "data": db["customers"][start:end],
        "total": len(db["customers"]),
        "page": page,
        "limit": limit
    }

@app.post("/api/customers", response_model=Customer, tags=["Customers"])
async def create_customer(customer: Customer):
    """Create a new customer"""
    customer_dict = customer.dict()
    customer_dict["id"] = str(uuid.uuid4())
    customer_dict["createdAt"] = datetime.now()
    customer_dict["updatedAt"] = datetime.now()
    db["customers"].append(customer_dict)
    return customer_dict

@app.get("/api/customers/{customer_id}", response_model=Customer, tags=["Customers"])
async def get_customer(customer_id: str):
    """Get customer details"""
    for customer in db["customers"]:
        if customer["id"] == customer_id:
            return customer
    raise HTTPException(status_code=404, detail="Customer not found")

# Account endpoints
@app.get("/api/accounts", response_model=PaginatedResponse[Account], tags=["Accounts"])
async def list_accounts(
    customer_id: Optional[str] = None,
    page: int = Query(1, ge=1),
    limit: int = Query(20, ge=1, le=100)
):
    """List all accounts with optional customer filter"""
    filtered_accounts = db["accounts"]
    if customer_id:
        filtered_accounts = [acc for acc in filtered_accounts if acc["customerId"] == customer_id]
    
    start = (page - 1) * limit
    end = start + limit
    return {
        "data": filtered_accounts[start:end],
        "total": len(filtered_accounts),
        "page": page,
        "limit": limit
    }

# Card endpoints
@app.get("/api/cards", response_model=PaginatedResponse[Card], tags=["Cards"])
async def list_cards(
    customer_id: Optional[str] = None,
    page: int = Query(1, ge=1),
    limit: int = Query(20, ge=1, le=100)
):
    """List all cards with optional customer filter"""
    filtered_cards = db["cards"]
    if customer_id:
        filtered_cards = [card for card in filtered_cards if card["customerId"] == customer_id]
    
    start = (page - 1) * limit
    end = start + limit
    return {
        "data": filtered_cards[start:end],
        "total": len(filtered_cards),
        "page": page,
        "limit": limit
    }

# Transaction endpoints
@app.get("/api/transactions", response_model=PaginatedResponse[Transaction], tags=["Transactions"])
async def list_transactions(
    account_id: Optional[str] = None,
    start_date: Optional[date] = None,
    end_date: Optional[date] = None,
    page: int = Query(1, ge=1),
    limit: int = Query(20, ge=1, le=100)
):
    """List all transactions with filters"""
    filtered_transactions = db["transactions"]
    if account_id:
        filtered_transactions = [t for t in filtered_transactions if t["accountId"] == account_id]
    
    start = (page - 1) * limit
    end = start + limit
    return {
        "data": filtered_transactions[start:end],
        "total": len(filtered_transactions),
        "page": page,
        "limit": limit
    }

# Loan endpoints
@app.get("/api/loans", response_model=PaginatedResponse[Loan], tags=["Loans"])
async def list_loans(
    customer_id: Optional[str] = None,
    status: Optional[str] = None,
    page: int = Query(1, ge=1),
    limit: int = Query(20, ge=1, le=100)
):
    """List all loans with filters"""
    filtered_loans = db["loans"]
    if customer_id:
        filtered_loans = [loan for loan in filtered_loans if loan["customerId"] == customer_id]
    if status:
        filtered_loans = [loan for loan in filtered_loans if loan["status"] == status]
    
    start = (page - 1) * limit
    end = start + limit
    return {
        "data": filtered_loans[start:end],
        "total": len(filtered_loans),
        "page": page,
        "limit": limit
    }

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000) 