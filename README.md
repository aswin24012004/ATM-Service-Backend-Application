Here’s a polished **README.md** you can drop into your project root. It documents your ATM Management System, its features, configuration, and API endpoints — making it presentation‑ready and easy to share.

---

```markdown
# ATM Management System
```
A **Java Servlet-based ATM Management System** with JWT authentication, role-based access, transaction handling, email notifications, logging, and Swagger API documentation. This project simulates core ATM functionalities with secure backend services.

---

## 🚀 Features
- **Authentication & JWT**: Secure login/logout with token validation
- **Admin Controls**:
  - Create users
  - View all users
  - Monitor ATM balance
- **User Operations**:
  - Deposit and withdraw funds
  - Check balance
  - View transaction history
- **Regex Validation**: Phone number & email validation
- **Email Notifications**:
  - Welcome email on registration
  - Transaction alerts (deposit/withdraw)
  - ATM low balance alerts (< 10k) sent to admin
- **Logging**: Structured logging with SLF4J
- **Swagger/OpenAPI**: Interactive API documentation

---

## 🛠️ Tech Stack
- **Java Servlets (Jakarta EE)**
- **Spring JDBC (JdbcTemplate)**
- **MySQL Database**
- **JWT (io.jsonwebtoken)**
- **SLF4J Logging**
- **JavaMail API for Email**
- **Swagger/OpenAPI** for API docs
- **Postman** for API testing

---

## ⚙️ Configuration
All settings are managed in `application.properties`:

```properties
# Database
db.url=jdbc:mysql://localhost:3306/atmdb
db.username=root
db.password=root
db.driver=com.mysql.cj.jdbc.Driver

# JWT
SECRET=12345678901234567890123456789012
EXPIRATION_MS=3600000

# Email
mail.host=smtp.gmail.com
mail.port=587
mail.username=your-email@gmail.com
mail.password=your-app-password

# Logging
org.slf4j.simpleLogger.defaultLogLevel=info
```

---

## 📂 API Endpoints

### 🔑 Authentication
- `POST /api/login` → Login with username & PIN, returns JWT
- `GET /api/login` → Test endpoint (returns dummy token)

### 👤 User
- `GET /api/balance` → Get user balance
- `POST /api/deposit` → Deposit money
- `POST /api/withdraw` → Withdraw money
- `GET /api/transactions` → Get user’s transaction history

### 🛡️ Admin
- `GET /api/admin?path=users` → List all users
- `GET /api/admin?path=transactions` → List all transactions
- `POST /api/admin?path=insert` → Create new user
- `GET /api/atm` → Get ATM balance
- `POST /api/atm` → Add funds to ATM

---

## 📧 Email Notifications
- **Welcome Email** → Sent when a new user registers
- **Transaction Email** → Sent on deposit/withdraw
- **ATM Low Balance Alert** → Sent to admin when ATM balance < 10k

---

## 📜 Logging
- All actions are logged with SLF4J
- Logs include authentication attempts, transactions, and admin actions
- Configurable via `application.properties`

---

## 📖 API Documentation
Swagger/OpenAPI is integrated:
- `/openapi` → JSON spec
- `/swagger-ui` → Interactive API docs

---

## 🧪 Testing
You can test all endpoints using the provided **Postman Collection**:  
[ATM API Postman Collection](https://web.postman.co/workspace/My-Workspace~dca9610a-765b-491d-83e0-c9307ef09811/collection/41402726-2ffc2c0b-7ca2-491f-b5e8-77112faae289?action=share&source=copy-link&creator=41402726)

---

## 📦 Setup & Run
1. Clone the repository
2. Configure `application.properties`
3. Create MySQL database `atmdb` and run schema scripts
4. Deploy on Tomcat/Jetty
5. Access APIs via Postman or Swagger UI

---

## 👨‍💻 Author
Developed by **Aswin C**  
ATM Management System Project (Java Servlets + MySQL + JWT )

