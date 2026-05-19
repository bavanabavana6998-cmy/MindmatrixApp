package com.ksheerasagara.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "income_entries")
public class IncomeEntry {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String cowName;
    public long dateMillis;
    public double liters;
    public double fatPercent;
    public double snfPercent;
    public double ratePerLiter;
    public double amount;
}
