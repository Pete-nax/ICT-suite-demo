# KenIT — ICT Helpdesk & Network Monitor

A production-grade ICT management suite built for Kenyan organizations.
Handles helpdesk tickets, IT asset inventory, and live network monitoring — all in one dashboard.

Built with Java 21 (Virtual Threads), Python 3.12, and Vanilla JS.

---

## Features

| Module | What it does |
|---|---|
| 🎫 Helpdesk | Raise, assign, and resolve support tickets |
| 🖥️ Asset Inventory | Track laptops, printers, routers, licenses |
| 📡 Network Scanner | Discover all devices on the LAN via Python |
| 🔔 Ping Monitor | Live up/down status for critical hosts |
| 📊 Dashboard | Summary stats — open tickets, offline devices, assets |

---

## Tech Stack

- **Backend**: Java 21 + Spring Boot 3.3 (Virtual Threads enabled)
- **Database**: H2 (dev) → PostgreSQL (prod)
- **Network Tools**: Python 3.12 + socket + scapy
- **Frontend**: HTML5 / CSS3 / Vanilla JS
- **Auth**: Spring Security (Basic Auth, upgradeable to JWT)

---

## Project Structure

```
kenit/
├── backend/                    # Spring Boot API
│   └── src/main/java/ke/co/kenit/
│       ├── model/              # JPA Entities
│       ├── dto/                # Request/Response shapes
│       ├── repository/         # Spring Data JPA
│       ├── service/            # Business logic lives here
│       ├── controller/         # REST endpoints
│       └── config/             # CORS, Security, Thread config
├── network/
│   ├── scanner.py              # LAN device discovery
│   ├── ping_monitor.py         # Continuous host monitoring
│   └── requirements.txt
├── frontend/
│   ├── index.html              # Dashboard
│   ├── css/style.css           # 2026 design palette
│   └── js/app.js               # Fetch API calls
├── docker-compose.yml
└── README.md
```

---

## Quick Start

### 1. Run the backend
```bash
cd backend
./mvnw spring-boot:run
# API available at http://localhost:8080
```

### 2. Run the network scanner (needs sudo for raw sockets)
```bash
cd network
pip install -r requirements.txt
sudo python3 scanner.py --subnet 192.168.1.0/24
```

### 3. Open the dashboard
```bash
# Just open frontend/index.html in your browser
# Or serve it:
cd frontend && python3 -m http.server 3000
```

---

## Deploy to Render (Free Tier)

1. Push to GitHub
2. New Web Service → connect repo
3. Build command: `cd backend && ./mvnw package -DskipTests`
4. Start command: `java -jar backend/target/kenit-1.0.jar`
5. Add env var: `SPRING_PROFILES_ACTIVE=prod`

---

## API Endpoints

```
GET    /api/tickets              # All tickets
POST   /api/tickets              # Raise new ticket
PATCH  /api/tickets/{id}/assign  # Assign to technician
PATCH  /api/tickets/{id}/resolve # Mark resolved

GET    /api/assets               # All assets
POST   /api/assets               # Add asset
PUT    /api/assets/{id}          # Update asset
DELETE /api/assets/{id}          # Decommission

GET    /api/network/devices      # Last scan results
POST   /api/network/scan         # Trigger new scan
GET    /api/dashboard/stats      # Summary numbers
```

---

## Kenyan Context

- Department names default to Kenyan Gov/NGO structure (ICT, Finance, HR, Registry)
- Asset tags use `KEN-YYYY-XXX` format
- Time zone: Africa/Nairobi (EAT UTC+3)
- Currency: KES for license/asset cost tracking

---

## Author

Built as a portfolio project demonstrating ICT Officer competencies:
network administration, helpdesk operations, and systems management.
