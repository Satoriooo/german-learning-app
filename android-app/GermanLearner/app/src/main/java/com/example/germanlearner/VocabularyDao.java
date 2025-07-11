package com.example.germanlearner;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface VocabularyDao {
    @Insert
    void insert(Vocabulary vocabulary);

    @Delete
    void delete(Vocabulary vocabulary);

    @Query("SELECT * FROM vocabulary_table ORDER BY germanWord ASC")
    List<Vocabulary> getAllVocabulary();
}