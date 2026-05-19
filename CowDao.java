package com.ksheerasagara.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CowDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Cow cow);

    @Query("SELECT DISTINCT cowName FROM income_entries WHERE cowName IS NOT NULL AND cowName != '' ORDER BY cowName")
    List<String> cowNamesFromIncome();
}
