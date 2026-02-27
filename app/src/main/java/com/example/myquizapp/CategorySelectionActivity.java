package com.example.myquizapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Button;

public class CategorySelectionActivity extends AppCompatActivity {

    private Spinner categorySpinner, difficultySpinner;

    // Categories with Open Trivia DB IDs
    private final String[][] categories = {
            {"Any Category", "0"},
            {"General Knowledge", "9"},
            {"Science: Computers", "18"},
            {"History", "23"},
            {"Science & Nature", "17"},
            {"Movies", "11"},
            {"Geography", "22"},
            {"Sports", "21"}
    };

    private final String[] difficulties = {"Any Difficulty", "easy", "medium", "hard"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_selection);

        categorySpinner = findViewById(R.id.categorySpinner);
        difficultySpinner = findViewById(R.id.difficultySpinner);
        Button startButton = findViewById(R.id.startButton);

        // Set up category spinner
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                getCategoryNames()
        );
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);

        // Set up difficulty spinner
        ArrayAdapter<CharSequence> difficultyAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.difficulty_options,
                android.R.layout.simple_spinner_item
        );
        difficultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        difficultySpinner.setAdapter(difficultyAdapter);

        startButton.setOnClickListener(v -> startQuiz());
    }

    private String[] getCategoryNames() {
        String[] names = new String[categories.length];
        for (int i = 0; i < categories.length; i++) {
            names[i] = categories[i][0];
        }
        return names;
    }

    private void startQuiz() {
        int selectedCategoryPosition = categorySpinner.getSelectedItemPosition();
        String categoryId = categories[selectedCategoryPosition][1];

        String difficulty = difficulties[difficultySpinner.getSelectedItemPosition()];

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("CATEGORY_ID", categoryId);
        intent.putExtra("DIFFICULTY", difficulty);
        startActivity(intent);
    }
}