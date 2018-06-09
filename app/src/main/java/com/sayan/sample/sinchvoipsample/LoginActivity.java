package com.sayan.sample.sinchvoipsample;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void login(View view) {
        EditText userIdTextView = findViewById(R.id.userId);
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("userId", userIdTextView.getText().toString().trim());
        startActivity(intent);
    }
}
