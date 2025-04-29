package com.example.myapplication1;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AddNoteActivity extends AppCompatActivity {

    private EditText editTextSubject;
    private EditText editTextScore;
    private TextView textViewStudentName;
    private Button buttonSave;
    private Button buttonCancel;

    private int studentId;
    private String studentName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        // Initialize UI components
        editTextSubject = findViewById(R.id.edit_subject);
        editTextScore = findViewById(R.id.edit_score);
        textViewStudentName = findViewById(R.id.text_student_name);
        buttonSave = findViewById(R.id.button_save);
        buttonCancel = findViewById(R.id.button_cancel);

        // Get data from intent
        if (getIntent() != null) {
            studentId = getIntent().getIntExtra("student_id", -1);
            studentName = getIntent().getStringExtra("student_name");

            // Display student name
            textViewStudentName.setText(studentName);
        }

        // Set up save button click listener
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNote();
            }
        });

        // Set up cancel button click listener
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
    }

    /**
     * Validate and save the note
     */
    private void saveNote() {
        // Validate inputs
        String subject = editTextSubject.getText().toString().trim();
        String scoreText = editTextScore.getText().toString().trim();

        if (subject.isEmpty()) {
            editTextSubject.setError("Le sujet est requis");
            return;
        }

        double score;
        try {
            score = Double.parseDouble(scoreText);
            // Validate score range (0-20 for French grading system)
            if (score < 0 || score > 20) {
                editTextScore.setError("La note doit être entre 0 et 20");
                return;
            }
        } catch (NumberFormatException e) {
            editTextScore.setError("Veuillez entrer une note valide");
            return;
        }

        // Create a new note object
        Note newNote = new Note(subject, score);

        try {
            // Save note using the NotesManager
            NotesManager.addNote(this, newNote);

            Log.d("AddNoteActivity", "Note added: " + subject + " - " + score);

            // Show success message
            Toast.makeText(this, "Note ajoutée avec succès", Toast.LENGTH_SHORT).show();

            // Set result and finish
            setResult(RESULT_OK);
            finish();
        } catch (Exception e) {
            Log.e("AddNoteActivity", "Error saving note: " + e.getMessage());
            Toast.makeText(this, "Erreur lors de l'ajout de la note", Toast.LENGTH_SHORT).show();
        }
    }
}