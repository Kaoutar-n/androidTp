package com.example.myapplication1;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class NotesManager {
    private static final String PREFS_NAME = "NotesPreferences";
    private static final String NOTES_KEY = "LocalNotes";
    private static final String SERVER_NOTES_KEY = "ServerNotes"; // New key for server notes
    private static final List<Note> localNotes = new ArrayList<>();
    private static final List<Note> serverNotes = new ArrayList<>(); // New list for server notes
    private static boolean isInitialized = false;

    // Initialize notes from SharedPreferences
    public static void initialize(Context context) {
        if (!isInitialized) {
            loadNotesFromPreferences(context);
            isInitialized = true;
        }
    }

    // Add a note locally
    public static void addNote(Context context, Note note) {
        localNotes.add(note);
        saveNotesToPreferences(context);
    }

    // Get all local notes
    public static List<Note> getLocalNotes() {
        return localNotes;
    }

    // Get all server notes (new method)
    public static List<Note> getServerNotes() {
        return serverNotes;
    }

    // Update server notes from JSON array (new method)
    public static void updateServerNotes(Context context, JSONArray notesArray) {
        try {
            serverNotes.clear();

            // Process server notes
            if (notesArray != null) {
                for (int i = 0; i < notesArray.length(); i++) {
                    JSONObject noteObj = notesArray.getJSONObject(i);
                    String label = noteObj.getString("label");
                    double score = noteObj.getDouble("score");
                    serverNotes.add(new Note(label, score));
                }
            }

            // Save to preferences for offline access
            saveServerNotesToPreferences(context);

            Log.d("NotesManager", "Server notes updated: " + serverNotes.size());
        } catch (JSONException e) {
            Log.e("NotesManager", "Error updating server notes: " + e.getMessage());
        }
    }

    // Clear local notes
    public static void clearLocalNotes(Context context) {
        localNotes.clear();
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .remove(NOTES_KEY)
                .apply();
    }

    // Save local notes to SharedPreferences
    private static void saveNotesToPreferences(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        try {
            JSONArray jsonArray = new JSONArray();
            for (Note note : localNotes) {
                JSONObject jsonNote = new JSONObject();
                jsonNote.put("label", note.getLabel());
                jsonNote.put("score", note.getScore());
                jsonArray.put(jsonNote);
            }

            editor.putString(NOTES_KEY, jsonArray.toString());
            editor.apply();

            Log.d("NotesManager", "Local notes saved to SharedPreferences: " + jsonArray.toString());
        } catch (JSONException e) {
            Log.e("NotesManager", "Error saving local notes: " + e.getMessage());
        }
    }

    // Save server notes to SharedPreferences (new method)
    private static void saveServerNotesToPreferences(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        try {
            JSONArray jsonArray = new JSONArray();
            for (Note note : serverNotes) {
                JSONObject jsonNote = new JSONObject();
                jsonNote.put("label", note.getLabel());
                jsonNote.put("score", note.getScore());
                jsonArray.put(jsonNote);
            }

            editor.putString(SERVER_NOTES_KEY, jsonArray.toString());
            editor.apply();

            Log.d("NotesManager", "Server notes saved to SharedPreferences: " + jsonArray.toString());
        } catch (JSONException e) {
            Log.e("NotesManager", "Error saving server notes: " + e.getMessage());
        }
    }

    // Load notes from SharedPreferences
    private static void loadNotesFromPreferences(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Load local notes
        String localNotesJson = prefs.getString(NOTES_KEY, "");
        if (!localNotesJson.isEmpty()) {
            try {
                JSONArray jsonArray = new JSONArray(localNotesJson);
                localNotes.clear();

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonNote = jsonArray.getJSONObject(i);
                    String label = jsonNote.getString("label");
                    double score = jsonNote.getDouble("score");
                    localNotes.add(new Note(label, score));
                }

                Log.d("NotesManager", "Local notes loaded from SharedPreferences: " + localNotes.size());
            } catch (JSONException e) {
                Log.e("NotesManager", "Error loading local notes: " + e.getMessage());
            }
        }

        // Load server notes
        String serverNotesJson = prefs.getString(SERVER_NOTES_KEY, "");
        if (!serverNotesJson.isEmpty()) {
            try {
                JSONArray jsonArray = new JSONArray(serverNotesJson);
                serverNotes.clear();

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonNote = jsonArray.getJSONObject(i);
                    String label = jsonNote.getString("label");
                    double score = jsonNote.getDouble("score");
                    serverNotes.add(new Note(label, score));
                }

                Log.d("NotesManager", "Server notes loaded from SharedPreferences: " + serverNotes.size());
            } catch (JSONException e) {
                Log.e("NotesManager", "Error loading server notes: " + e.getMessage());
            }
        }
    }

    // Get all notes (both server and local combined)
    public static List<Note> getAllNotes() {
        List<Note> allNotes = new ArrayList<>();
        allNotes.addAll(serverNotes);
        allNotes.addAll(localNotes);
        return allNotes;
    }
}