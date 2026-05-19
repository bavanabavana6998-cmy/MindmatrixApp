# Ksheera-Sagara Dairy Profit/Loss Calculator

Ksheera-Sagara is an Android application for dairy farmers to track daily milk income, monthly expenses, cow-wise profitability, and financial health.

## Features

- Daily milk slip entry with liters, fat percentage, SNF percentage, rate, and cow name.
- Expense logging with categories: Fodder, Medical, Labor, Electricity, and Other.
- Monthly dashboard showing total income, total expenses, net profit, and profit per liter.
- Green/red financial health indicator.
- Cow-wise profit analysis.
- Expense category pie chart.
- Monthly financial summary sharing as text or generated PDF.
- Local Room database storage for year-long offline usage.

## Tech Stack

- Java
- Android SDK
- Room Database
- Material-free native Android UI
- Android `PdfDocument` for report generation
- FileProvider for sharing PDFs

## How to Run

1. Open this folder in Android Studio.
2. Let Android Studio sync Gradle dependencies.
3. Run the app on an emulator or Android phone.

## Project Structure

```text
app/src/main/java/com/ksheerasagara/
  MainActivity.java
  data/
    AppDatabase.java
    Cow.java
    CowDao.java
    ExpenseEntry.java
    ExpenseDao.java
    IncomeEntry.java
    IncomeDao.java
  ui/
    ExpensePieChartView.java
```

## Student Success Criteria Covered

- Monthly financial summary can be shared as PDF/text.
- Expense categories include Fodder, Medical, Labor, Electricity, and Other.
- UI focuses on money metrics and clear financial health.
- Room DB stores daily entries locally.
- Pie chart shows expense distribution.
