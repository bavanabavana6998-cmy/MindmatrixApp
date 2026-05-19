package com.ksheerasagara.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "cows")
public class Cow {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String name;
    public String breed;
}
