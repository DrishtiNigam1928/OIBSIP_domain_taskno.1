# Train Reservation System (Java Swing + JDBC + SQLite)

A simple GUI-based train reservation system: login, book tickets (with
auto-generated PNR), and cancel bookings by PNR lookup.

## Tech Stack
- Java (Swing) for the GUI
- JDBC + SQLite (file-based DB, no server setup needed)
- Maven (handles the SQLite JDBC driver download for you)

## Folder Structure
```
train-reservation-system/
├── pom.xml
├── README.md
└── src/main/java/com/reservation/
    ├── Main.java
    ├── db/
    │   └── DatabaseManager.java
    ├── model/
    │   └── Booking.java
    └── ui/
        ├── LoginForm.java
        ├── MainMenu.java
        ├── ReservationForm.java
        └── CancellationForm.java
```

## Prerequisites
- Java JDK 11 or later installed (`java -version`)
- Apache Maven installed (`mvn -version`)
- Internet access the FIRST time you build (Maven downloads the
  `sqlite-jdbc` driver automatically — after that it's cached locally)

## How to Build and Run

1. Open a terminal in the `train-reservation-system` folder.
2. Build the project (this also downloads the SQLite driver):
   ```
   mvn clean package
   ```
3. Run the generated jar:
   ```
   java -jar target/train-reservation-system.jar
   ```
4. A `reservation.db` SQLite file will be created automatically in the
   same folder the first time you run it, along with sample data.

### Default Login
```
Username: admin
Password: admin123
```
(This is seeded automatically the first time the app runs. You can add
more rows to the `users` table directly in `reservation.db` using any
SQLite browser if you want more accounts.)

### Sample Train Numbers (pre-seeded, auto-fills train name)
| Train Number | Train Name                |
|---------------|---------------------------|
| 12301         | Howrah Rajdhani Express   |
| 12951         | Mumbai Rajdhani Express   |
| 12259         | Sealdah Duronto Express   |
| 12626         | Kerala Express            |
| 12002         | Bhopal Shatabdi Express   |
| 12909         | Garib Rath Express        |

If you type a train number that isn't in this list, the app will still
let you book — it just labels the train name as "Unknown Train" (since
this is a simple demo project, not a live train database).

## How It Works

- **Login Form** — checks the entered username/password against the
  `users` table using a `PreparedStatement`. Wrong credentials → "Access
  Denied" dialog.
- **Reservation Form** — collects passenger name, train number/name,
  class, date, source and destination. Validates that no field is
  empty, train number is numeric, and the date matches `dd-MM-yyyy`.
  On success it generates a random unique 10-digit PNR, saves the
  booking, and shows a confirmation dialog with all details.
- **Cancellation Form** — enter a PNR and click **Fetch** to pull the
  full booking details from the database. Clicking **Cancel Booking**
  asks "Are you sure?" before deleting the row.

## Notes on Extending This Project
- To switch from SQLite to MySQL: change the `DB_URL` in
  `DatabaseManager.java` to something like
  `jdbc:mysql://localhost:3306/reservation_db` (with a username/password),
  and swap the `sqlite-jdbc` dependency in `pom.xml` for
  `mysql-connector-j`. All the SQL/PreparedStatement code stays the same.
- To add "register new user" functionality, add an `INSERT` method in
  `DatabaseManager` and a simple sign-up form similar to `LoginForm`.
- Passwords are stored in plain text here for simplicity — for anything
  beyond a class project, hash them (e.g., with `BCrypt`) before storing.

## If You Don't Want to Use Maven
You can compile manually if you download `sqlite-jdbc-3.45.1.0.jar`
yourself (e.g. from Maven Central) and place it in a `lib/` folder:
```
javac -cp lib/sqlite-jdbc-3.45.1.0.jar -d out $(find src -name "*.java")
java -cp "out:lib/sqlite-jdbc-3.45.1.0.jar" com.reservation.Main
```
(On Windows, use `;` instead of `:` in the classpath.)
