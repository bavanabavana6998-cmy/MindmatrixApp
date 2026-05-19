package com.ksheerasagara;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.ksheerasagara.data.AppDatabase;
import com.ksheerasagara.data.ExpenseDao;
import com.ksheerasagara.data.ExpenseEntry;
import com.ksheerasagara.data.IncomeEntry;
import com.ksheerasagara.ui.ExpensePieChartView;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final String[] EXPENSE_CATEGORIES = {"Fodder", "Medical", "Labor", "Electricity", "Other"};

    private final DecimalFormat money = new DecimalFormat("0.00");
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private AppDatabase db;

    private TextView healthLabel;
    private TextView incomeValue;
    private TextView expenseValue;
    private TextView profitValue;
    private TextView profitPerLiterValue;
    private TextView suggestionValue;
    private TextView cowAnalysisValue;
    private ExpensePieChartView pieChartView;

    private EditText cowInput;
    private EditText litersInput;
    private EditText fatInput;
    private EditText snfInput;
    private EditText rateInput;
    private EditText expenseAmountInput;
    private EditText expenseNoteInput;
    private EditText expenseCowInput;
    private Spinner expenseCategorySpinner;

    private double currentIncome;
    private double currentExpense;
    private double currentLiters;
    private String currentSummaryText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = AppDatabase.getInstance(this);
        buildUi();
        refreshDashboard();
    }

    private void buildUi() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(Color.rgb(248, 250, 247));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(28, 28, 28, 40);
        scrollView.addView(root);

        TextView title = text("Ksheera-Sagara", 30, Color.rgb(13, 63, 45), true);
        root.addView(title);
        root.addView(text("Dairy Profit/Loss Calculator", 16, Color.rgb(66, 82, 76), false));

        healthLabel = text("Financial Health", 20, Color.WHITE, true);
        healthLabel.setGravity(Gravity.CENTER);
        healthLabel.setPadding(18, 18, 18, 18);
        root.addView(withTopMargin(healthLabel, 24));

        LinearLayout metrics = new LinearLayout(this);
        metrics.setOrientation(LinearLayout.VERTICAL);
        root.addView(withTopMargin(metrics, 14));
        incomeValue = metric(metrics, "Monthly Income");
        expenseValue = metric(metrics, "Monthly Expenses");
        profitValue = metric(metrics, "Net Profit");
        profitPerLiterValue = metric(metrics, "Profit per Liter");

        suggestionValue = text("", 15, Color.rgb(42, 52, 48), false);
        suggestionValue.setPadding(0, 14, 0, 14);
        root.addView(suggestionValue);

        root.addView(section("Income Log"));
        cowInput = input("Cow name, e.g. Lakshmi");
        litersInput = input("Liters from milk slip");
        fatInput = input("Fat %, e.g. 4.2");
        snfInput = input("SNF %, e.g. 8.5");
        rateInput = input("Rate per liter");
        root.addView(cowInput);
        root.addView(litersInput);
        root.addView(fatInput);
        root.addView(snfInput);
        root.addView(rateInput);
        Button saveIncome = button("Save Income");
        saveIncome.setOnClickListener(v -> saveIncome());
        root.addView(withTopMargin(saveIncome, 12));

        root.addView(section("Expense Log"));
        expenseCategorySpinner = new Spinner(this);
        expenseCategorySpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, EXPENSE_CATEGORIES));
        root.addView(expenseCategorySpinner);
        expenseAmountInput = input("Amount spent");
        expenseNoteInput = input("Note, e.g. feed bag or vet visit");
        expenseCowInput = input("Cow name if cow-specific, optional");
        root.addView(expenseAmountInput);
        root.addView(expenseNoteInput);
        root.addView(expenseCowInput);
        Button saveExpense = button("Save Expense");
        saveExpense.setOnClickListener(v -> saveExpense());
        root.addView(withTopMargin(saveExpense, 12));

        root.addView(section("Expense Analytics"));
        pieChartView = new ExpensePieChartView(this);
        root.addView(withHeight(pieChartView, 380));

        root.addView(section("Cow-wise Analysis"));
        cowAnalysisValue = text("No cow data yet.", 15, Color.rgb(42, 52, 48), false);
        root.addView(cowAnalysisValue);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setGravity(Gravity.CENTER);
        Button shareText = button("Share Summary");
        shareText.setOnClickListener(v -> shareTextSummary());
        Button sharePdf = button("Share PDF");
        sharePdf.setOnClickListener(v -> sharePdfSummary());
        actions.addView(shareText, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        actions.addView(sharePdf, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        root.addView(withTopMargin(actions, 26));

        setContentView(scrollView);
    }

    private void saveIncome() {
        double liters = number(litersInput);
        double fat = number(fatInput);
        double snf = number(snfInput);
        double rate = number(rateInput);
        if (liters <= 0 || rate <= 0) {
            toast("Enter valid liters and rate.");
            return;
        }

        IncomeEntry entry = new IncomeEntry();
        entry.cowName = cowInput.getText().toString().trim();
        entry.dateMillis = System.currentTimeMillis();
        entry.liters = liters;
        entry.fatPercent = fat;
        entry.snfPercent = snf;
        entry.ratePerLiter = rate;
        entry.amount = liters * rate;

        executor.execute(() -> {
            db.incomeDao().insert(entry);
            runOnUiThread(() -> {
                clear(cowInput, litersInput, fatInput, snfInput, rateInput);
                toast("Income saved.");
                refreshDashboard();
            });
        });
    }

    private void saveExpense() {
        double amount = number(expenseAmountInput);
        if (amount <= 0) {
            toast("Enter a valid expense amount.");
            return;
        }

        ExpenseEntry entry = new ExpenseEntry();
        entry.category = expenseCategorySpinner.getSelectedItem().toString();
        entry.amount = amount;
        entry.note = expenseNoteInput.getText().toString().trim();
        entry.cowName = expenseCowInput.getText().toString().trim();
        entry.dateMillis = System.currentTimeMillis();

        executor.execute(() -> {
            db.expenseDao().insert(entry);
            runOnUiThread(() -> {
                clear(expenseAmountInput, expenseNoteInput, expenseCowInput);
                toast("Expense saved.");
                refreshDashboard();
            });
        });
    }

    private void refreshDashboard() {
        long[] range = currentMonthRange();
        executor.execute(() -> {
            List<IncomeEntry> incomes = db.incomeDao().getForPeriod(range[0], range[1]);
            List<ExpenseEntry> expenses = db.expenseDao().getForPeriod(range[0], range[1]);
            List<ExpenseDao.CategoryTotal> categoryTotals = db.expenseDao().totalsByCategory(range[0], range[1]);

            double income = 0;
            double liters = 0;
            for (IncomeEntry entry : incomes) {
                income += entry.amount;
                liters += entry.liters;
            }

            double expense = 0;
            for (ExpenseEntry entry : expenses) {
                expense += entry.amount;
            }

            Map<String, Double> pieValues = new LinkedHashMap<>();
            for (ExpenseDao.CategoryTotal total : categoryTotals) {
                pieValues.put(total.category, total.total);
            }

            String cowAnalysis = buildCowAnalysis(incomes, expenses);
            double finalIncome = income;
            double finalExpense = expense;
            double finalLiters = liters;
            runOnUiThread(() -> updateDashboard(finalIncome, finalExpense, finalLiters, pieValues, cowAnalysis));
        });
    }

    private void updateDashboard(double income, double expense, double liters, Map<String, Double> pieValues, String cowAnalysis) {
        currentIncome = income;
        currentExpense = expense;
        currentLiters = liters;
        double profit = income - expense;
        double profitPerLiter = liters > 0 ? profit / liters : 0;

        incomeValue.setText("Rs " + money.format(income));
        expenseValue.setText("Rs " + money.format(expense));
        profitValue.setText("Rs " + money.format(profit));
        profitPerLiterValue.setText("Rs " + money.format(profitPerLiter));

        if (profit >= 0) {
            healthLabel.setText("GREEN: Profitable this month");
            healthLabel.setBackgroundColor(Color.rgb(29, 143, 82));
        } else {
            healthLabel.setText("RED: Loss this month");
            healthLabel.setBackgroundColor(Color.rgb(192, 57, 43));
        }

        suggestionValue.setText(buildSuggestion(income, expense, profitPerLiter, pieValues));
        cowAnalysisValue.setText(cowAnalysis);
        pieChartView.setValues(pieValues);
        currentSummaryText = buildSummaryText(income, expense, liters, cowAnalysis, pieValues);
    }

    private String buildCowAnalysis(List<IncomeEntry> incomes, List<ExpenseEntry> expenses) {
        Map<String, Double> incomeByCow = new LinkedHashMap<>();
        Map<String, Double> expenseByCow = new LinkedHashMap<>();

        for (IncomeEntry entry : incomes) {
            String cow = cleanCowName(entry.cowName);
            incomeByCow.put(cow, incomeByCow.getOrDefault(cow, 0d) + entry.amount);
        }
        for (ExpenseEntry entry : expenses) {
            if (entry.cowName == null || entry.cowName.trim().isEmpty()) {
                continue;
            }
            String cow = cleanCowName(entry.cowName);
            expenseByCow.put(cow, expenseByCow.getOrDefault(cow, 0d) + entry.amount);
        }

        if (incomeByCow.isEmpty()) {
            return "No cow data yet.";
        }

        StringBuilder builder = new StringBuilder();
        String bestCow = "";
        double bestProfit = Double.NEGATIVE_INFINITY;
        for (String cow : incomeByCow.keySet()) {
            double profit = incomeByCow.get(cow) - expenseByCow.getOrDefault(cow, 0d);
            if (profit > bestProfit) {
                bestProfit = profit;
                bestCow = cow;
            }
            builder.append(cow).append(": Rs ").append(money.format(profit)).append(" net\n");
        }
        builder.append("Most profitable: ").append(bestCow).append(" (Rs ").append(money.format(bestProfit)).append(")");
        return builder.toString();
    }

    private String buildSuggestion(double income, double expense, double profitPerLiter, Map<String, Double> pieValues) {
        if (income <= 0 && expense <= 0) {
            return "Add today's milk slip and expenses to see financial health.";
        }

        String biggestCategory = "";
        double biggestValue = 0;
        for (Map.Entry<String, Double> entry : pieValues.entrySet()) {
            if (entry.getValue() > biggestValue) {
                biggestCategory = entry.getKey();
                biggestValue = entry.getValue();
            }
        }

        if (profitPerLiter < 0) {
            return "Loss warning: input cost is higher than milk income. Review feed quantity, medical spending, and low-yield cows.";
        }
        if ("Fodder".equals(biggestCategory)) {
            return "Fodder is the biggest cost. Try home-grown fodder, bulk purchase, or comparing feed brands.";
        }
        if ("Medical".equals(biggestCategory)) {
            return "Medical cost is high. Track repeat illnesses cow-wise and discuss preventive care with a vet.";
        }
        return "Good start. Keep entering daily slips so profit per liter becomes more accurate.";
    }

    private String buildSummaryText(double income, double expense, double liters, String cowAnalysis, Map<String, Double> pieValues) {
        double profit = income - expense;
        double profitPerLiter = liters > 0 ? profit / liters : 0;
        String month = new SimpleDateFormat("MMMM yyyy", Locale.US).format(Calendar.getInstance().getTime());
        StringBuilder builder = new StringBuilder();
        builder.append("Ksheera-Sagara Monthly Financial Summary\n");
        builder.append(month).append("\n\n");
        builder.append("Total Income: Rs ").append(money.format(income)).append("\n");
        builder.append("Total Expenses: Rs ").append(money.format(expense)).append("\n");
        builder.append("Net Profit: Rs ").append(money.format(profit)).append("\n");
        builder.append("Total Liters: ").append(money.format(liters)).append("\n");
        builder.append("Profit per Liter: Rs ").append(money.format(profitPerLiter)).append("\n\n");
        builder.append("Expense Categories\n");
        if (pieValues.isEmpty()) {
            builder.append("No expenses recorded.\n");
        } else {
            for (Map.Entry<String, Double> entry : pieValues.entrySet()) {
                builder.append(entry.getKey()).append(": Rs ").append(money.format(entry.getValue())).append("\n");
            }
        }
        builder.append("\nCow-wise Analysis\n").append(cowAnalysis);
        return builder.toString();
    }

    private void shareTextSummary() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Ksheera-Sagara Monthly Summary");
        intent.putExtra(Intent.EXTRA_TEXT, currentSummaryText);
        startActivity(Intent.createChooser(intent, "Share monthly summary"));
    }

    private void sharePdfSummary() {
        try {
            File directory = new File(getCacheDir(), "summaries");
            if (!directory.exists()) {
                directory.mkdirs();
            }
            File file = new File(directory, "ksheera_sagara_monthly_summary.pdf");

            PdfDocument document = new PdfDocument();
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
            PdfDocument.Page page = document.startPage(pageInfo);
            Canvas canvas = page.getCanvas();
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(Color.rgb(13, 63, 45));
            paint.setTextSize(24f);
            canvas.drawText("Ksheera-Sagara Monthly Summary", 42, 54, paint);
            paint.setColor(Color.rgb(36, 48, 42));
            paint.setTextSize(14f);

            int y = 92;
            for (String line : currentSummaryText.split("\n")) {
                canvas.drawText(line, 42, y, paint);
                y += 22;
                if (y > 800) {
                    break;
                }
            }

            document.finishPage(page);
            FileOutputStream outputStream = new FileOutputStream(file);
            document.writeTo(outputStream);
            outputStream.close();
            document.close();

            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("application/pdf");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Share PDF summary"));
        } catch (Exception exception) {
            toast("Could not create PDF: " + exception.getMessage());
        }
    }

    private long[] currentMonthRange() {
        Calendar start = Calendar.getInstance();
        start.set(Calendar.DAY_OF_MONTH, 1);
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);

        Calendar end = (Calendar) start.clone();
        end.add(Calendar.MONTH, 1);
        end.add(Calendar.MILLISECOND, -1);
        return new long[]{start.getTimeInMillis(), end.getTimeInMillis()};
    }

    private TextView text(String value, int sp, int color, boolean bold) {
        TextView textView = new TextView(this);
        textView.setText(value);
        textView.setTextSize(sp);
        textView.setTextColor(color);
        textView.setIncludeFontPadding(true);
        if (bold) {
            textView.setTypeface(textView.getTypeface(), android.graphics.Typeface.BOLD);
        }
        return textView;
    }

    private TextView section(String value) {
        TextView textView = text(value, 20, Color.rgb(13, 63, 45), true);
        textView.setPadding(0, 28, 0, 12);
        return textView;
    }

    private TextView metric(LinearLayout parent, String label) {
        TextView view = text(label + "\nRs 0.00", 18, Color.rgb(36, 48, 42), true);
        view.setPadding(20, 18, 20, 18);
        view.setBackgroundColor(Color.WHITE);
        parent.addView(withTopMargin(view, 10));
        return view;
    }

    private EditText input(String hint) {
        EditText editText = new EditText(this);
        editText.setHint(hint);
        editText.setSingleLine(true);
        editText.setPadding(16, 10, 16, 10);
        return editText;
    }

    private Button button(String label) {
        Button button = new Button(this);
        button.setText(label);
        button.setAllCaps(false);
        button.setTextColor(Color.WHITE);
        button.setBackgroundColor(Color.rgb(23, 107, 77));
        return button;
    }

    private View withTopMargin(View view, int top) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = top;
        view.setLayoutParams(params);
        return view;
    }

    private View withHeight(View view, int height) {
        view.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                height
        ));
        return view;
    }

    private double number(EditText input) {
        try {
            return Double.parseDouble(input.getText().toString().trim());
        } catch (Exception ignored) {
            return 0;
        }
    }

    private String cleanCowName(String cowName) {
        if (cowName == null || cowName.trim().isEmpty()) {
            return "Unassigned Cow";
        }
        return cowName.trim();
    }

    private void clear(EditText... inputs) {
        for (EditText input : inputs) {
            input.setText("");
        }
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        executor.shutdown();
        super.onDestroy();
    }
}
