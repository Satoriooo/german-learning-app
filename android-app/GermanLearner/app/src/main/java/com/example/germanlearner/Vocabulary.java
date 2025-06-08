package com.example.germanlearner;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "vocabulary_table")
public class Vocabulary {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String germanWord;
    private String englishTranslation;
    private String germanSentence;
    private String englishSentence;

    // --- GETTERS AND SETTERS ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getGermanWord() { return germanWord; }
    public void setGermanWord(String germanWord) { this.germanWord = germanWord; }
    public String getEnglishTranslation() { return englishTranslation; }
    public void setEnglishTranslation(String englishTranslation) { this.englishTranslation = englishTranslation; }
    public String getGermanSentence() { return germanSentence; }
    public void setGermanSentence(String germanSentence) { this.germanSentence = germanSentence; }
    public String getEnglishSentence() { return englishSentence; }
    public void setEnglishSentence(String englishSentence) { this.englishSentence = englishSentence; }
}