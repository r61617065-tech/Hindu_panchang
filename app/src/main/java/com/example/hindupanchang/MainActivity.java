package com.example.hindupanchang;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText inputEditor;
    private EditText outputEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputEditor = findViewById(R.id.etInputCode);
        outputEditor = findViewById(R.id.etOutputCode);

        Button btnJavaToSmali = findViewById(R.id.btnJavaToSmali);
        Button btnSmaliToJava = findViewById(R.id.btnSmaliToJava);
        Button btnSwap = findViewById(R.id.btnSwap);
        Button btnClear = findViewById(R.id.btnClear);

        btnJavaToSmali.setOnClickListener(v -> convertJavaToSmali());
        btnSmaliToJava.setOnClickListener(v -> convertSmaliToJava());
        btnSwap.setOnClickListener(v -> swapEditors());
        btnClear.setOnClickListener(v -> clearEditors());
    }

    private void convertJavaToSmali() {
        String source = inputEditor.getText().toString().trim();
        if (source.isEmpty()) {
            Toast.makeText(this, "Paste Java code first", Toast.LENGTH_SHORT).show();
            return;
        }
        outputEditor.setText(ConverterEngine.javaToSmali(source));
    }

    private void convertSmaliToJava() {
        String source = inputEditor.getText().toString().trim();
        if (source.isEmpty()) {
            Toast.makeText(this, "Paste Smali code first", Toast.LENGTH_SHORT).show();
            return;
        }
        outputEditor.setText(ConverterEngine.smaliToJava(source));
    }

    private void swapEditors() {
        String input = inputEditor.getText().toString();
        String output = outputEditor.getText().toString();
        inputEditor.setText(output);
        outputEditor.setText(input);
    }

    private void clearEditors() {
        inputEditor.setText("");
        outputEditor.setText("");
    }
}
