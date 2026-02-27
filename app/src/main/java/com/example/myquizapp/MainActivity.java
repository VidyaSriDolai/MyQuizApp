package com.example.myquizapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // UI Components
    private TextView questionTextView;
    private Button option1Button, option2Button, option3Button, option4Button;
    private TextView scoreTextView;
    private Button logoutButton, retryButton;
    private ProgressBar progressBar;

    // Game State
    private int currentQuestionIndex = 0;
    private int score = 0;
    private List<Question> questionList = new ArrayList<>();

    // Quiz Configuration
    private String selectedCategoryId;
    private String selectedDifficulty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get selected options from CategorySelectionActivity
        Intent intent = getIntent();
        selectedCategoryId = intent.getStringExtra("CATEGORY_ID");
        selectedDifficulty = intent.getStringExtra("DIFFICULTY");

        initializeViews();
        checkAuthentication();
        setupClickListeners();
        fetchNewQuestions();
    }

    private void initializeViews() {
        questionTextView = findViewById(R.id.questionTextView);
        option1Button = findViewById(R.id.option1Button);
        option2Button = findViewById(R.id.option2Button);
        option3Button = findViewById(R.id.option3Button);
        option4Button = findViewById(R.id.option4Button);
        scoreTextView = findViewById(R.id.scoreTextView);
        logoutButton = findViewById(R.id.logoutButton);
        retryButton = findViewById(R.id.retryButton);
        progressBar = findViewById(R.id.progressBar);
    }

    private void checkAuthentication() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    private void setupClickListeners() {
        option1Button.setOnClickListener(v -> checkAnswer(0));
        option2Button.setOnClickListener(v -> checkAnswer(1));
        option3Button.setOnClickListener(v -> checkAnswer(2));
        option4Button.setOnClickListener(v -> checkAnswer(3));

        logoutButton.setOnClickListener(v -> logoutUser());
        retryButton.setOnClickListener(v -> restartQuiz());
    }

    private void fetchNewQuestions() {
        progressBar.setVisibility(View.VISIBLE);
        new Thread(() -> {
            try {
                String apiUrl = buildApiUrl();
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                if (connection.getResponseCode() == 200) {
                    processApiResponse(connection);
                } else {
                    showError("API Error: " + connection.getResponseCode());
                }
            } catch (Exception e) {
                showError("Network Error: " + e.getMessage());
            }
        }).start();
    }

    private String buildApiUrl() {
        String baseUrl = "https://opentdb.com/api.php?amount=10&type=multiple";

        if (!selectedCategoryId.equals("0")) {
            baseUrl += "&category=" + selectedCategoryId;
        }
        if (!selectedDifficulty.equals("Any Difficulty")) {
            baseUrl += "&difficulty=" + selectedDifficulty.toLowerCase();
        }
        return baseUrl;
    }

    private void processApiResponse(HttpURLConnection connection) throws Exception {
        InputStream inputStream = connection.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder response = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            response.append(line);
        }

        JSONObject jsonResponse = new JSONObject(response.toString());
        JSONArray results = jsonResponse.getJSONArray("results");
        List<Question> newQuestions = new ArrayList<>();

        for (int i = 0; i < results.length(); i++) {
            JSONObject questionObj = results.getJSONObject(i);
            newQuestions.add(createQuestionFromJson(questionObj));
        }

        runOnUiThread(() -> {
            questionList = newQuestions;
            currentQuestionIndex = 0;
            score = 0;
            progressBar.setVisibility(View.GONE);
            loadQuestion(currentQuestionIndex);
        });
    }

    private Question createQuestionFromJson(JSONObject questionObj) throws Exception {
        String questionText = Html.fromHtml(questionObj.getString("question")).toString();
        String correctAnswer = Html.fromHtml(questionObj.getString("correct_answer")).toString();
        JSONArray incorrectAnswers = questionObj.getJSONArray("incorrect_answers");

        String[] options = new String[4];
        options[0] = correctAnswer;
        for (int j = 0; j < 3; j++) {
            options[j + 1] = Html.fromHtml(incorrectAnswers.getString(j)).toString();
        }

        return new Question(questionText, options, correctAnswer);
    }

    private void loadQuestion(int questionIndex) {
        if (questionIndex < questionList.size()) {
            Question currentQuestion = questionList.get(questionIndex);
            displayQuestion(currentQuestion);
            updateScoreDisplay();
        } else {
            showFinalResults();
        }
    }

    private void displayQuestion(Question question) {
        questionTextView.setText(question.getQuestion());
        List<String> options = new ArrayList<>(Arrays.asList(question.getOptions()));
        Collections.shuffle(options);

        option1Button.setText(options.get(0));
        option2Button.setText(options.get(1));
        option3Button.setText(options.get(2));
        option4Button.setText(options.get(3));
    }

    private void checkAnswer(int selectedOptionIndex) {
        Button selectedButton = getSelectedButton(selectedOptionIndex);
        String selectedAnswer = selectedButton.getText().toString();
        Question currentQuestion = questionList.get(currentQuestionIndex);

        if (selectedAnswer.equals(currentQuestion.getCorrectAnswer())) {
            handleCorrectAnswer();
        } else {
            handleWrongAnswer(currentQuestion);
        }

        currentQuestionIndex++;
        loadQuestion(currentQuestionIndex);
    }

    private Button getSelectedButton(int index) {
        switch (index) {
            case 0: return option1Button;
            case 1: return option2Button;
            case 2: return option3Button;
            default: return option4Button;
        }
    }

    private void handleCorrectAnswer() {
        score++;
        Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();
    }

    private void handleWrongAnswer(Question question) {
        Toast.makeText(this, "Wrong! Correct answer: " + question.getCorrectAnswer(),
                Toast.LENGTH_LONG).show();
    }

    private void showFinalResults() {
        questionTextView.setText("Quiz Completed!");
        hideOptions();
        retryButton.setVisibility(View.VISIBLE);
        scoreTextView.setText("Final Score: " + score + "/" + questionList.size());
    }

    private void hideOptions() {
        option1Button.setVisibility(View.GONE);
        option2Button.setVisibility(View.GONE);
        option3Button.setVisibility(View.GONE);
        option4Button.setVisibility(View.GONE);
    }

    private void restartQuiz() {
        retryButton.setVisibility(View.GONE);
        showOptions();
        fetchNewQuestions();
    }

    private void showOptions() {
        option1Button.setVisibility(View.VISIBLE);
        option2Button.setVisibility(View.VISIBLE);
        option3Button.setVisibility(View.VISIBLE);
        option4Button.setVisibility(View.VISIBLE);
    }

    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void updateScoreDisplay() {
        scoreTextView.setText("Score: " + score + "/" + questionList.size());
    }

    private void showError(String message) {
        runOnUiThread(() -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
            retryButton.setVisibility(View.VISIBLE);
        });
    }

    // Question model class
    private static class Question {
        private final String question;
        private final String[] options;
        private final String correctAnswer;

        public Question(String question, String[] options, String correctAnswer) {
            this.question = question;
            this.options = options;
            this.correctAnswer = correctAnswer;
        }

        public String getQuestion() { return question; }
        public String[] getOptions() { return options; }
        public String getCorrectAnswer() { return correctAnswer; }
    }
}