# ⚽ Slotit: Sports Venue Booking & Tournament Management System

A DBMS mini-project (24ERJ502, T.K.M. College of Engineering, Kollam) that lets players book hourly slots at football, cricket, badminton, basketball and tennis venues, find squads, and join tournaments. Venue owners get a revenue dashboard.


## Features
- Player / Owner registration and login
- Venue search by city (Kollam, Kochi, Trivandrum, Calicut) and sport
- Court and hourly slot booking with **peak-hour pricing**
- **Wallet payment**, cancellation and refund
- **Double-booking prevention** using transactions and `SELECT ... FOR UPDATE`
- Squad Finder (post "need N players", join, auto-full)
- Tournaments and team registration
- Owner dashboard (bookings, revenue chart)
- Badges and streaks
- Installable mobile web app (PWA)

## Tech Stack
| Layer | Technology |
|---|---|
| Frontend | HTML, CSS, JavaScript (PWA) |
| Backend | Java Servlets (JDK 17+), Apache Tomcat 9 |
| Database | MySQL 8 (JDBC), MongoDB (optional, stats) |
| Build | Maven |

## DBMS Concepts Demonstrated
ACID transactions, commit/rollback, pessimistic row locking, concurrency control, primary/foreign keys, normalization, triggers, PreparedStatement (SQL-injection safe), DAO pattern, ER diagram.

## Project Structure
```
Slotit/
├── database/        schema.sql, seed_data.sql, triggers.sql, queries, mongodb_setup.js
├── src/main/java/   servlets, DAOs, models, utils (DBConnection)
├── src/main/webapp/ HTML, CSS, JS, manifest.json, sw.js
├── screenshots/     UI screenshots
└── pom.xml
```

## Screenshots
| Login | Slot Booking | Checkout |
|---|---|---|
| ![](screenshots/Login%20page.jpeg) | ![](screenshots/Slot%20booking%20page.jpeg) | ![](screenshots/Checkout%20page.jpeg) |

| Booking History & Owner | ER Diagram |
|---|---|
| ![](screenshots/Bookig%20history%20and%20owner%20details.jpeg) | ![](screenshots/ER%20DIagram.jpeg) |

## Documentation
- [Final Project Report (PDF)](docs/Slotit_DBMS_Project_Report_Final.pdf)
- [Presentation (PPTX)](docs/Slotit_Presentation.pptx)
- ER diagrams: [`docs/sports_booking_er_diagram.png`](docs/sports_booking_er_diagram.png), [`docs/sports_booking_chen_erd.png`](docs/sports_booking_chen_erd.png)

## Setup
### Prerequisites
JDK 17+, Maven, MySQL 8, Apache Tomcat 9.

### 1. Database
```sql
CREATE DATABASE slotit;
USE slotit;
SOURCE database/schema.sql;
SOURCE database/triggers.sql;
SOURCE database/seed_data.sql;
```

### 2. Database credentials
Credentials are read from JVM properties or environment variables (never hard-code them):
```powershell
$env:SLOTIT_DB_USER = "root"
$env:SLOTIT_DB_PASSWORD = "your_password"
```
For Tomcat, add `-DSLOTIT_DB_USER=root -DSLOTIT_DB_PASSWORD=your_password` to `CATALINA_OPTS`.

### 3. Build and deploy
```bash
mvn clean package
copy target/Slotit.war <TOMCAT_HOME>/webapps/
<TOMCAT_HOME>/bin/startup.bat
```

### 4. Open the app
- Laptop: `http://localhost:8085/Slotit/splash.html` (port as set in Tomcat `server.xml`)
- Phone (same Wi-Fi): `http://<laptop-ip>:8085/Slotit/splash.html`

Demo logins: `player1@example.com` / `owner1@example.com`, password `password123`.

## Concurrency Demo
Run `database/concurrency_test.sql` in two MySQL sessions to see one booking succeed and the other get blocked by the row lock.

## License
Academic project, for educational use.
