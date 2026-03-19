# Personal Finance Manager Pro

A robust, desktop-based personal finance management application built with Java and MySQL. This application features a fully normalized relational database and a modern Java Swing Graphical User Interface (GUI), allowing users to securely track their income and expenses while maintaining strict data integrity.

## ✨ Key Features

* **Multi-User Architecture:** Secure registration and login system.
* 
  <img width="984" height="742" alt="image" src="https://github.com/user-attachments/assets/6901688c-c194-4b41-9754-9de194796b7c" />

* **Interactive Dashboard:** A clean, intuitive Java Swing GUI with a `CardLayout` for seamless screen navigation.
* 
  <img width="983" height="742" alt="image" src="https://github.com/user-attachments/assets/443bcab1-f465-4b68-adf9-b03d66a708b6" />

* **Dynamic Balance Calculation:** Net balances are calculated in real-time directly within the MySQL database using advanced SQL aggregation, preventing data desynchronization.
* **Transaction Ledger:** View a complete, scrollable history of all deposits and expenses.
* **Financial Data Integrity (Soft Deletes):** Transactions are removed from the user's view and balance calculations via a boolean flag (`is_deleted`), preserving the underlying database audit trail.
  

## 🛠️ Tech Stack

* **Language:** Java (JDK 25)
* **GUI Framework:** Java Swing (`javax.swing`)
* **Database:** MySQL (via XAMPP)
* **Driver:** MySQL Connector/J (JDBC)

## 🗄️ Database Schema

The application relies on a strictly typed, normalized relational database to defend against invalid data states.

* `users`: Stores user credentials and profile data.
* `transaction_types`: A lookup table defining valid categories (e.g., 'INCOME', 'EXPENSE').
* `transactions`: The core ledger linking users, types, and monetary amounts via Foreign Keys.

## 🚀 Getting Started

### Prerequisites
1. Install [XAMPP](https://www.apachefriends.org/index.html) to run a local MySQL server.
2. Ensure you have a Java IDE installed (e.g., IntelliJ IDEA).
3. Download the official [MySQL JDBC Driver](https://dev.mysql.com/downloads/connector/j/).

### Installation
1. Clone this repository.
2. Start the **MySQL** module in the XAMPP Control Panel.
3. Open `http://localhost/phpmyadmin` and create a database named `personal_finance`.
4. Run the master SQL initialization script (found in the project notes/documentation) to generate the tables.
5. Open the project in IntelliJ IDEA.
6. Go to **File > Project Structure > Modules > Dependencies**, click the `+` icon, and link the downloaded `mysql-connector-j-x.x.x.jar` file.
7. Run the `FinanceGUI.java` file to launch the application.

