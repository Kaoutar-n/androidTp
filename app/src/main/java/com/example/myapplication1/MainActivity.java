package com.example.myapplication1;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String HTTP_URL = "https://belatar.name/rest/profile.php?login=test&passwd=test&id=9998&notes=true";
    private static final String HTTP_FETCH_NOTES_URL = "https://belatar.name/rest/notes.php?login=test&passwd=test&student_id=";
    private static final String HTTP_IMAGES = "https://belatar.name/images/";
    private static final int REQUEST_ADD_NOTE = 100;

    private Etudiant etd;
    private List<Note> notesList = new ArrayList<>();
    private NoteAdapter noteAdapter;
    private EditText txtNom, txtPrenom, txtClasse, txtRemarques;
    private ListView listView;

    // Activity result launcher for AddNoteActivity
    private ActivityResultLauncher<Intent> addNoteLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // Initialize NotesManager to load saved notes
        NotesManager.initialize(this);

        // Initialize the ActivityResultLauncher
        addNoteLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        // Refresh notes if a note was added - keep both server and local notes
                        if (etd != null) {
                            fetchStudentNotes(etd.getId());
                        }
                    }
                });


        // Set appropriate layout based on orientation
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.activity_main_land);

            // Initialize UI elements
            txtNom = findViewById(R.id.edit_nom);
            txtPrenom = findViewById(R.id.edit_prenom);
            txtClasse = findViewById(R.id.edit_classe);

            // Initialize the ListView and adapter in landscape mode
            listView = findViewById(R.id.notes_list);
            noteAdapter = new NoteAdapter(this, notesList);
            listView.setAdapter(noteAdapter);

            // Add text change listeners to update notes when student data changes
            txtNom.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus && etd != null) {
                    etd.setNom(txtNom.getText().toString());
                    fetchStudentNotes(etd.getId());
                }
            });

            txtPrenom.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus && etd != null) {
                    etd.setPrénom(txtPrenom.getText().toString());
                    fetchStudentNotes(etd.getId());
                }
            });

            txtClasse.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus && etd != null) {
                    etd.setClasse(txtClasse.getText().toString());
                    fetchStudentNotes(etd.getId());
                }
            });

            // Set up add note button click listener with proper error handling
            FloatingActionButton fabAddNote = findViewById(R.id.fab_add_note);
            fabAddNote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openAddNoteActivity();
                }
            });

        } else {
            setContentView(R.layout.activity_main);
            txtNom = findViewById(R.id.edit_nom);
            txtPrenom = findViewById(R.id.edit_prenom);
            txtClasse = findViewById(R.id.edit_classe);
            txtRemarques = findViewById(R.id.description);

            // Add text change listeners in portrait mode too
            txtNom.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus && etd != null) {
                    etd.setNom(txtNom.getText().toString());
                }
            });

            txtPrenom.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus && etd != null) {
                    etd.setPrénom(txtPrenom.getText().toString());
                }
            });

            txtClasse.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus && etd != null) {
                    etd.setClasse(txtClasse.getText().toString());
                }
            });
        }

        // Set up call button functionality for both orientations
        ImageButton callButton = findViewById(R.id.btn_call);
        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etd != null && etd.getPhone() != null && !etd.getPhone().isEmpty()) {
                    // Create an intent to dial the phone number
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + etd.getPhone()));

                    // Check if there's an app that can handle this intent
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    } else {
                        Toast.makeText(MainActivity.this, "Aucune application de téléphone trouvée", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Numéro de téléphone non disponible", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Log.d("CycleVie", "onCreate() appelé");
    }

    private void openAddNoteActivity() {
        try {
            if (etd != null) {
                Intent intent = new Intent(MainActivity.this, AddNoteActivity.class);
                intent.putExtra("student_id", etd.getId());
                intent.putExtra("student_name", etd.getNom() + " " + etd.getPrénom());

                // Launch the activity with our registered launcher
                addNoteLauncher.launch(intent);
            } else {
                Toast.makeText(this, "Données de l'étudiant non disponibles", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Error launching AddNoteActivity: " + e.getMessage());
            Toast.makeText(this, "Erreur lors de l'ouverture de l'activité d'ajout de note", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("CycleVie", "onStart() appelé");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("CycleVie", "onResume() appelé");

        // Clear the notes list before fetching new data
        notesList.clear();

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, HTTP_URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(MainActivity.class.getSimpleName(), response.toString());
                        try {
                            etd = new Etudiant(
                                    response.getInt("id"),
                                    response.getString("nom"),
                                    response.getString("prenom"),
                                    response.getString("classe"),
                                    null,
                                    response.getString("phone")
                            );

                            // Load student profile image
                            VolleySingleton.getInstance(getApplicationContext()).getImageLoader()
                                    .get(HTTP_IMAGES + response.getString("photo"), new ImageLoader.ImageListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            Log.e(MainActivity.class.getSimpleName(), "Error loading image: " + error.getMessage());
                                        }

                                        @Override
                                        public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                                            if (response.getBitmap() != null) {
                                                etd.setPhoto(response.getBitmap());
                                                ImageView img = findViewById(R.id.imageProfile);
                                                img.setImageBitmap(etd.getPhoto());
                                            }
                                        }
                                    });

                            // Set student details in the form
                            txtNom.setText(etd.getNom());
                            txtPrenom.setText(etd.getPrénom());
                            txtClasse.setText(etd.getClasse());

                            // Handle notes if in landscape mode
                            int orientation = getResources().getConfiguration().orientation;
                            if (orientation == Configuration.ORIENTATION_LANDSCAPE && response.has("notes")) {
                                JSONArray notesArray = response.getJSONArray("notes");
                                processNotesArray(notesArray);
                            }

                        } catch (JSONException e) {
                            Log.e(MainActivity.class.getSimpleName(), "JSON parsing error: " + e.getMessage());
                            Toast.makeText(MainActivity.this, "Erreur lors du chargement des données", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(MainActivity.class.getSimpleName(), "Network error: " + error.getMessage());
                Toast.makeText(MainActivity.this, "Erreur de connexion au serveur", Toast.LENGTH_SHORT).show();
            }
        });

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }

    private void processNotesArray(JSONArray notesArray) throws JSONException {
        // Clear previous notes
        notesList.clear();

        // Update server notes in NotesManager
        NotesManager.updateServerNotes(this, notesArray);

        // Add all notes (server + local) to the list
        notesList.addAll(NotesManager.getAllNotes());

        // Update the adapter with new data
        if (noteAdapter != null) {
            noteAdapter.notifyDataSetChanged();
        }
    }

    // Method to fetch notes for a specific student
    public void fetchStudentNotes(int studentId) {
        String url = HTTP_FETCH_NOTES_URL + studentId;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Clear the notes list before updating
                            notesList.clear();

                            // Process and save server notes
                            if (response.has("notes")) {
                                JSONArray notesArray = response.getJSONArray("notes");
                                // Update server notes in NotesManager
                                NotesManager.updateServerNotes(MainActivity.this, notesArray);
                            }

                            // Get all notes (both server and local) and add to the list
                            notesList.addAll(NotesManager.getAllNotes());

                            // Update the adapter
                            if (noteAdapter != null) {
                                noteAdapter.notifyDataSetChanged();
                            }
                        } catch (JSONException e) {
                            Log.e(MainActivity.class.getSimpleName(), "JSON parsing error: " + e.getMessage());

                            // In case of error, display all available notes
                            notesList.clear();
                            notesList.addAll(NotesManager.getAllNotes());
                            if (noteAdapter != null) {
                                noteAdapter.notifyDataSetChanged();
                            }

                            Toast.makeText(MainActivity.this, "Erreur lors du chargement des notes du serveur", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(MainActivity.class.getSimpleName(), "Network error: " + error.getMessage());

                // In case of connection error, display all available notes
                notesList.clear();
                notesList.addAll(NotesManager.getAllNotes());
                if (noteAdapter != null) {
                    noteAdapter.notifyDataSetChanged();
                }

                Toast.makeText(MainActivity.this, "Erreur de connexion au serveur - Affichage des notes disponibles uniquement", Toast.LENGTH_SHORT).show();
            }
        });

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }
    @Override
    protected void onPause() {
        super.onPause();
        Log.d("CycleVie", "onPause() appelé");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("CycleVie", "onStop() appelé");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("CycleVie", "onDestroy() appelé");
    }

    public void OnClickHandler(View view) {
        // Update student data and fetch notes
        if (etd != null) {
            etd.setNom(txtNom.getText().toString());
            etd.setPrénom(txtPrenom.getText().toString());
            etd.setClasse(txtClasse.getText().toString());

            // If in landscape mode, fetch notes
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                fetchStudentNotes(etd.getId());
            }

            Toast.makeText(this, "Données enregistrées", Toast.LENGTH_SHORT).show();
            Log.d("CycleVie", "Bouton Enregistrer cliqué");
        }
    }

}