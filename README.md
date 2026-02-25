# Yoga App — Full-Stack Testing Project

A yoga session management application built with **Angular 14** (front-end) and **Spring Boot / Java 11** (back-end), using a **MySQL** database.

This README explains how to install, run the application and execute all tests (unit, integration and end-to-end).

---

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Database Setup](#database-setup)
3. [Application Installation](#application-installation)
4. [Running the Application](#running-the-application)
5. [Back-End Tests (JUnit + Mockito)](#back-end-tests--junit--mockito)
6. [Front-End Tests (Jest)](#front-end-tests--jest)
7. [End-to-End Tests (Cypress)](#end-to-end-tests--cypress)
8. [Coverage Reports](#coverage-reports)

---

## Prerequisites

Make sure the following tools are installed on your machine:

| Tool | Minimum Version | Check |
|---|---|---|
| Java (JDK) | 11 | `java -version` |
| Maven | 3.6+ | `mvn -version` |
| Node.js | 16.x | `node -version` |
| npm | 8.x | `npm -version` |
| Angular CLI | 14 | `ng version` |
| MySQL | 8.x | `mysql --version` |

>  MySQL must be running on the **default port 3306**.

---

## Database Setup

### 1. Connect to MySQL

```bash
mysql -u root -p
```

### 2. Create the database

```sql
CREATE DATABASE yoga_app;
```

### 3. Run the provided SQL script

The initialization script is located in `ressources/sql/`:

```bash
mysql -u root -p yoga_app < ressources/sql/script.sql
```

This script creates the required tables and inserts a default admin account:

| Field | Value |
|---|---|
| Email | `yoga@studio.com` |
| Password | `test!1234` |

---

## Application Installation

### Clone the repository

```bash
git clone https://github.com/hafidaAssendal/Testez-une-application-full-stack.git
cd Testez-une-application-full-stack
```

### Back-end

```bash
cd back
mvn clean install
```

### Front-end

```bash
cd ../front
npm install
```

---

##  Running the Application

>  The **back-end must be started before the front-end**.

### 1. Start the back-end (Spring Boot API)

```bash
cd back
mvn spring-boot:run
```

The API will be available at: **http://localhost:8080**

### 2. Start the front-end (Angular)

In a new terminal:

```bash
cd front
npm start
```

The application will be available at: **http://localhost:4200**

### 3. Log in to the application

Open your browser at `http://localhost:4200` and sign in with the **admin** account:

- **Email:** `yoga@studio.com`
- **Password:** `test!1234`

---

##  Back-End Tests — JUnit + Mockito

Back-end tests cover the **service**, **controller** and **repository** layers, excluding the `dto`, `models`, `mapper`, `payload`, `exception`, `repository` packages and `*Application.class` .

### Run all tests

```bash
cd back
mvn clean test
```

### Run a specific test

```bash
mvn test -Dtest=SessionServiceTest
```

### Generate the coverage report (JaCoCo)

```bash
mvn clean test jacoco:report
```

The HTML report is generated at:

```
back/target/site/jacoco/index.html
```

Open this file in a browser to view detailed coverage by class, method and line.

---

##  Front-End Tests — Jest

Front-end tests cover Angular **components** and **services**, including both unit and integration tests.

### Run all tests

```bash
cd front
npm run test
```

### Run tests with coverage report

```bash
npm run test -- --coverage
```

The HTML report is generated at:

```
front/coverage/jest/lcov-report/index.html
```

Open this file in a browser to view coverage by file, function and line.

---

## End-to-End Tests — Cypress

E2E tests simulate real user behavior in the browser. API calls are mocked using `cy.intercept()`.

> The front-end **does not need to be running** if all data is fully mocked. However, starting it is recommended for certain scenarios.


### Run Cypress in headless mode (command line)

```bash
npx cypress run
```

Then select `E2E Testing`, choose your browser, and launch test files from the interface.

> Run the all.cy.ts file because it groups all Cypress E2E test files and generates a global test report at:
> ```
front/coverage/lcov-report/index.html
>```

### Run E2E tests with coverage report

```bash
npm run e2e:coverage
```

The E2E coverage report is generated at:

```
front/coverage/lcov-report/index.html
```

---

##  Coverage Reports

| Part | Command | Report Location |
|---|---|---|
| **Back-end** | `mvn clean test jacoco:report` | `back/target/site/jacoco/index.html` |
| **Front-end** | `npm run test -- --coverage` | `front/coverage/jest/lcov-report/index.html` |
| **E2E** | `npm run e2e:coverage` | `front/coverage/lcov-report/index.html` |

> The coverage target is **at least 80%** on every indicator (statements, branches, lines, functions) for front-end, back-end and e2e.

---

---

##  Test Accounts

| Role | Email | Password |
|---|---|---|
| Admin | `yoga@studio.com` | `test!1234` |
