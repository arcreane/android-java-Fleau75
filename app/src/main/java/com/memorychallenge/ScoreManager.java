package com.memorychallenge;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;

// Classe qui gère la persistance et la récupération des scores du jeu
public class ScoreManager {

    // Constantes pour le stockage des scores
    private static final String PREFS_NAME = "MemoryChallenge";  // Nom du fichier de préférences
    private static final String KEY_SCORES = "scores";           // Clé pour stocker les scores
    private static final String ENTRY_DELIMITER = ";";           // Délimiteur entre les entrées
    private static final String FIELD_DELIMITER = ",";          // Délimiteur entre les champs d'une entrée
    private static final String TAG = "ScoreManager";

    private SharedPreferences prefs;  // Objet pour accéder aux préférences

    // Classe interne représentant une entrée de score
    public static class ScoreEntry {
        public final String playerName;  // Nom du joueur
        public final int score;          // Score obtenu

        public ScoreEntry(String playerName, int score) {
            this.playerName = playerName != null ? playerName : "Anonyme";
            this.score = score;
        }
    }

    // Constructeur : initialise l'accès aux préférences
    public ScoreManager(Context ctx) {
        prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /** 
     * Sauvegarde un score avec le nom du joueur dans la liste, et ne garde que les 5 meilleurs.
     * Le score est ajouté à la liste existante, puis la liste est triée et tronquée si nécessaire.
     */
    public void saveScore(int score, String playerName) {
        try {
            // Récupère tous les scores existants
            ArrayList<ScoreEntry> entries = getAllScoresWithNames();
            entries.add(new ScoreEntry(playerName, score));
            
            // Trie par score décroissant
            Collections.sort(entries, (a, b) -> Integer.compare(b.score, a.score));
            
            // Garde les 5 meilleurs scores
            if (entries.size() > 5) {
                entries = new ArrayList<>(entries.subList(0, 5));
            }

            // Convertit les scores en chaîne et les enregistre
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < entries.size(); i++) {
                if (i > 0) sb.append(ENTRY_DELIMITER);
                sb.append(entries.get(i).playerName).append(FIELD_DELIMITER).append(entries.get(i).score);
            }
            prefs.edit().putString(KEY_SCORES, sb.toString()).apply();
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la sauvegarde du score", e);
        }
    }

    /** 
     * Retourne la liste des meilleurs scores avec les noms (au plus 5), triés du plus grand au plus petit.
     * Les scores sont stockés sous forme de chaîne et sont convertis en objets ScoreEntry.
     */
    public ArrayList<ScoreEntry> getAllScoresWithNames() {
        ArrayList<ScoreEntry> entries = new ArrayList<>();
        try {
            String raw = prefs.getString(KEY_SCORES, "");
            if (!raw.isEmpty()) {
                // Parse chaque entrée de score
                for (String entry : raw.split(ENTRY_DELIMITER)) {
                    try {
                        String[] parts = entry.split(FIELD_DELIMITER);
                        if (parts.length == 2) {
                            String name = parts[0].trim();
                            int score = Integer.parseInt(parts[1].trim());
                            if (name.isEmpty()) name = "Anonyme";
                            entries.add(new ScoreEntry(name, score));
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "Erreur lors de la lecture d'une entrée: " + entry, e);
                        // Continue avec l'entrée suivante
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la lecture des scores", e);
        }
        return entries;
    }

    /** 
     * Pour la compatibilité avec l'ancien format : retourne uniquement les scores sans les noms
     */
    public ArrayList<Integer> getAllScores() {
        ArrayList<ScoreEntry> entries = getAllScoresWithNames();
        ArrayList<Integer> scores = new ArrayList<>();
        for (ScoreEntry entry : entries) {
            scores.add(entry.score);
        }
        return scores;
    }
}
