package com.ksheerasagara.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {IncomeEntry.class, ExpenseEntry.class, Cow.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase instance;

    public abstract IncomeDao incomeDao();
    public abstract ExpenseDao expenseDao();
    public abstract CowDao cowDao();

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "ksheera_sagara.db"
                    ).build();
                }
            }
        }
        return instance;
    }
}
