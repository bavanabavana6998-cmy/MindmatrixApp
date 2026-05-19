package com.ksheerasagara.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "expense_entries")
public class ExpenseEntry {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String category;
    public String note;
    public String cowName;
    public long dateMillis;
    public double amount;
}
