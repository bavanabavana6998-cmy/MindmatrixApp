package com.ksheerasagara.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ExpenseDao {
    @Insert
    void insert(ExpenseEntry entry);

    @Query("SELECT * FROM expense_entries WHERE dateMillis BETWEEN :start AND :end ORDER BY dateMillis DESC")
    List<ExpenseEntry> getForPeriod(long start, long end);

    @Query("SELECT COALESCE(SUM(amount), 0) FROM expense_entries WHERE dateMillis BETWEEN :start AND :end")
    double totalExpense(long start, long end);

    @Query("SELECT category, COALESCE(SUM(amount), 0) AS total FROM expense_entries WHERE dateMillis BETWEEN :start AND :end GROUP BY category")
    List<CategoryTotal> totalsByCategory(long start, long end);

    class CategoryTotal {
        public String category;
        public double total;
    }
}
