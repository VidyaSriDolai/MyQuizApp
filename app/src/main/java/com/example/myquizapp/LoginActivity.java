package com.example.myquizapp;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
public class LoginActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 9001;
    private EditText etEmail, etPassword;
    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient googleSignInClient;
    private SignInButton googleSignInButton; // Changed to correct type
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initializeFirebase();
        initializeViews();
        setupClickListeners();
    }
    private void initializeFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }
    private void initializeViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);
        TextView tvCreateAccount = findViewById(R.id.tvCreateAccount);
        googleSignInButton = findViewById(R.id.googleSignInButton); // Correct type
    }
    private void setupClickListeners() {
        findViewById(R.id.btnLogin).setOnClickListener(v -> loginWithEmailPassword());
        findViewById(R.id.tvForgotPassword).setOnClickListener(v -> startActivity(
                new Intent(LoginActivity.this, ForgotPasswordActivity.class)
        ));
        findViewById(R.id.tvCreateAccount).setOnClickListener(v -> startActivity(
                new Intent(LoginActivity.this, RegisterActivity.class)
        ));
        googleSignInButton.setOnClickListener(v -> signInWithGoogle()); // Use the class member
    }
    // Rest of the code remains the same
    private void loginWithEmailPassword() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required!");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required!");
            return;
        }
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        navigateToCategorySelection();
                    } else {
                        Toast.makeText(LoginActivity.this,
                                "Login failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(this, "Google sign-in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        navigateToCategorySelection();
                    } else {
                        Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void navigateToCategorySelection() {
        startActivity(new Intent(LoginActivity.this, CategorySelectionActivity.class));
        finish();
}
    @Override
    protected void onStart() {
        super.onStart();
        if (firebaseAuth.getCurrentUser() != null) {
            navigateToCategorySelection();
        }
    }
}