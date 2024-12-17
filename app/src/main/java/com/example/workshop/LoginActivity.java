package com.example.workshop;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.example.workshop.databinding.ActivityLoginBinding;
import android.content.SharedPreferences;


public class LoginActivity extends BaseActivity {

    ActivityLoginBinding binding;
    DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        databaseHelper = new DatabaseHelper(this);

        binding.loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = binding.loginEmail.getText().toString();
                String password = binding.loginPassword.getText().toString();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "All fields are mandatory", Toast.LENGTH_SHORT).show();
                } else {
                    Boolean checkCredentials = databaseHelper.checkEmailPassword(email, password);
                    Log.d("LoginActivity", "Credentials Valid: " + checkCredentials);

                    if (checkCredentials) {
                        int userType = databaseHelper.getUserType(email);
                        Log.d("LoginActivity", "UserType for " + email + ": " + userType);

                        if (userType != -1) {
                            SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putInt("userType", userType);
                            editor.putString("email", email);
                            editor.apply();

                            Toast.makeText(LoginActivity.this, "Login Successfully!", Toast.LENGTH_SHORT).show();


                            Intent intent;
                            if (userType == 1) {
                                intent = new Intent(LoginActivity.this, DriverDetailsActivity.class);
                            } else {
                                intent = new Intent(LoginActivity.this, PassengerDetailsActivity.class);
                            }
                            intent.putExtra("email", email);
                            startActivity(intent);
                        } else {
                            Toast.makeText(LoginActivity.this, "Invalid user type", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        binding.signupRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });
    }
}

