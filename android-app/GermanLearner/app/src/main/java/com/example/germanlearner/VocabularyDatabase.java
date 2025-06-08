package com.example.germanlearner;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Vocabulary.class}, version = 1, exportSchema = false)
public abstract class VocabularyDatabase extends RoomDatabase {
    public abstract VocabularyDao vocabularyDao();
}