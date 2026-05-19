package com.ksheerasagara.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface IncomeDao {
    @Insert
    void insert(IncomeEntry entry);

    @Query("SELECT * FROM income_entries WHERE dateMillis BETWEEN :start AND :end ORDER BY dateMillis DESC")
    List<IncomeEntry> getForPeriod(long start, long end);

    @Query("SELECT COALESCE(SUM(amount), 0) FROM income_entries WHERE dateMillis BETWEEN :start AND :end")
    double totalIncome(long start, long end);

    @Query("SELECT COALESCE(SUM(liters), 0) FROM income_entries WHERE dateMillis BETWEEN :start AND :end")
    double totalLiters(long start, long end);
}
