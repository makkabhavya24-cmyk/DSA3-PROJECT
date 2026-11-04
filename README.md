# OptiLogix — Logistics Optimization System

A full-stack website and decision-support logistics system built with **Java 17**, **Spring Boot 3**, and modern frontend web interfaces. The application solves network transportation problems using the **Min-Cost Max-Flow (MCMF)** algorithm to route freight, satisfy warehouse demand, and minimize overall shipment costs under route capacity and physical inventory limits.

---

## 🚀 Key Features

### 1. Dual Interfaces & Role-Based Access Control
- **Admin Portal**: Full control over distribution hubs, products, inventories, shipping routes, stock transfers, and automated MCMF order optimization.
- **User Portal**: Streamlined interface for clients and procurement teams to check stock availability, submit delivery orders, and monitor real-time fulfillment status and shipping routes.
- **Quick Demo Switcher**: Instant 1-click portal switching in the navigation header between `admin` (Admin view) and `user` (User view).

### 2. Core Optimization Engine — Min-Cost Max-Flow (MCMF)
- **Algorithm**: Successive Shortest Path algorithm powered by the **Shortest Path Faster Algorithm (SPFA)** to find augmenting paths with minimal unit cost in residual networks containing negative cost reverse edges.
- **Capacity & Inventory Constraints**: Incorporates source warehouse stock limits as capacity on super-source edges, and route throughput limits on transportation edges.
- **Flow Decomposition**: Decomposes aggregate flow into concrete shipping hops with exact units moved, unit costs, distance, travel time, and subtotal costs.
- **Partial Fulfillment & Bottleneck Reporting**: If demand cannot be 100% satisfied due to stock shortages or saturated route bottlenecks, the engine calculates the maximum feasible flow and accurately reports remaining unfulfilled demand and bottleneck causes.

### 3. Alternative Route Comparison & Disruption Analysis
- **1st vs. 2nd Best Route**: Evaluates the cheapest available route against the second-best alternative using Dijkstra and Yen's k-shortest path edge exclusion.
- **Comparative Metrics**: Displays side-by-side comparisons of Total Unit Cost ($/unit), Bottleneck Capacity (units), Total Distance (km), and Estimated Travel Time (hours).
- **Interactive Disruption Simulator**: Admins can toggle route statuses (e.g. block a route due to weather or maintenance) and observe the algorithm recalculate in real-time.

### 4. Inter-Warehouse Stock Transfer
- Admins can request stock transfers between any two warehouses.
- Automatically scans candidate source warehouses holding available inventory for the required product SKU.
- Computes the least-cost transshipment path.
- Automatically deducts physical inventory from the source warehouse on **Dispatch** and adds it to the destination warehouse on **Delivered**.

### 5. Warehouse & Inventory Management
- **Warehouses**: View details, capacities, geographic coordinates, and inspect all incoming and outgoing route connections.
- **Products & Stock**: Track inventory across all hubs, adjust stock levels with quick `+`/`-` or custom quantity adjustments, and automatically trigger **Low-Stock alerts** when units fall below safety thresholds.

---

## 🛠️ Technology Stack

- **Backend**: Java 17, Spring Boot 3.2.5, Spring Data JPA
- **Database**: H2 In-Memory Database (zero external setup required; pre-seeded with realistic logistics data)
- **Frontend**: Clean, responsive SPA (HTML5, modern CSS, Vanilla JavaScript, HTML5 Canvas network visualization)
- **Build Tool**: Apache Maven (Maven Wrapper included)

---

## 📂 Pre-Seeded Dataset

The system starts pre-populated with realistic logistics data:

1. **5 Warehouses (Major Indian Logistics Hubs)**:
   - `WH-MUM`: Mumbai Central Hub (Bhiwandi Logistics Park, 10,000 units capacity)
   - `WH-DEL`: Delhi NCR Hub (NH48 Freight Corridor, Gurugram, 9,500 units capacity)
   - `WH-BLR`: Bengaluru South Hub (Hosur Road Logistics Zone, 8,500 units capacity)
   - `WH-CCU`: Kolkata Eastern Hub (Dankuni Industrial Hub, 7,000 units capacity)
   - `WH-HYD`: Hyderabad Deccan Hub (Shamshabad Cargo Terminal, 8,000 units capacity)

2. **4 Delivery Destinations (Retail & Fulfillment Centers)**:
   - Chennai Retail Center (Chennai, Tamil Nadu)
   - Pune Fulfillment Center (Pune, Maharashtra)
   - Ahmedabad Distribution Depot (Ahmedabad, Gujarat)
   - Jaipur Metro Depot (Jaipur, Rajasthan)

3. **6 Products**:
   - `PRD-ELEC-01`: Industrial Microcontrollers (₹450.0/unit)
   - `PRD-BAT-02`: High-Capacity Li-Ion Batteries (₹850.0/unit)
   - `PRD-MED-03`: Temperature-Controlled Vaccines (₹1,200.0/unit)
   - `PRD-AUTO-04`: EV Power Inverters (₹2,100.0/unit)
   - `PRD-SOLAR-05`: Solar Micro-Inverters (₹1,500.0/unit)
   - `PRD-FOOD-06`: Organic Packaged Rations (₹180.0/unit)

4. **16 Directed Routes across Indian National Corridors**:
   - 8 Inter-Warehouse Transshipment routes (Mumbai–Delhi, Mumbai–Bengaluru, Mumbai–Hyderabad, Delhi–Kolkata, Hyderabad–Bengaluru, etc.)
   - 8 Warehouse-to-Destination delivery routes (Bengaluru–Chennai, Hyderabad–Chennai, Mumbai–Pune, Mumbai–Ahmedabad, Delhi–Jaipur, etc.) with realistic highway distances (km), transit times (hrs), and capacities.

5. **Demo Accounts**:
   - **Admin**: `admin` / `admin123`
   - **User 1**: `user` / `user123` (Aarav Sharma)
   - **User 2**: `sarah` / `sarah123` (Priya Patel)

---

## 🏃 Running the Project

### Quick Start (Windows)
Double-click `run.bat` or run:
```powershell
.\run.bat
```

### Alternatively with Maven Wrapper:
```powershell
.\mvnw.cmd spring-boot:run
```

Once started, open your web browser at:
👉 **[http://localhost:8080](http://localhost:8080)**

H2 Database Console is available at:
👉 **[http://localhost:8080/h2-console](http://localhost:8080/h2-console)** (JDBC URL: `jdbc:h2:mem:logisticsdb`, Username: `sa`, Password: *(empty)*)

---

## 📊 REST API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/network/summary` | High-level network KPIs (stock, hubs, orders, routes) |
| `GET` | `/api/network/graph` | Graph nodes and edge links for canvas visualization |
| `GET` | `/api/warehouses` | List all registered warehouses |
| `GET` | `/api/warehouses/{id}/connections` | Inspect incoming & outgoing routes + stock for warehouse |
| `POST` | `/api/warehouses` | Create new warehouse |
| `PUT` | `/api/warehouses/{id}` | Update warehouse details |
| `GET` | `/api/products` | List catalog products |
| `POST` | `/api/products` | Register new product SKU |
| `GET` | `/api/inventories` | List warehouse inventories (supports `?lowStockOnly=true`) |
| `PUT` | `/api/inventories/{id}/adjust` | Increase or decrease stock quantity |
| `GET` | `/api/routes` | List all transportation routes |
| `PATCH` | `/api/routes/{id}/toggle` | Enable or disable a route |
| `GET` | `/api/routes/compare` | Compare best route vs 2nd best alternative |
| `GET` | `/api/orders` | List orders (filter by `?userId=X`) |
| `POST` | `/api/orders` | Place new order |
| `POST` | `/api/orders/{id}/optimize` | **Run MCMF optimization engine on order** |
| `PATCH` | `/api/orders/{id}/status` | Update status (auto inventory deduction on dispatch & receipt) |
| `GET` | `/api/transfers/candidates` | List warehouses holding available stock for a product |
| `POST` | `/api/transfers/execute` | Optimize & create inter-warehouse stock transfer |
