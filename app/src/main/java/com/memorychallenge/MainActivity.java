package com.memorychallenge;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Button;
import java.util.ArrayList;

// Activité principale qui gère l'écran d'accueil du jeu
public class MainActivity extends Activity {

    // Initialisation de l'interface utilisateur
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Configuration du bouton pour commencer une partie
        Button playButton = findViewById(R.id.playButton);
        playButton.setOnClickListener(v -> showModeDialog());

        // Configuration du bouton pour voir les scores
        Button leaderboardButton = findViewById(R.id.leaderboardButton);
        leaderboardButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ScoreActivity.class);
            intent.putExtra("viewOnly", true);
            startActivity(intent);
        });
    }

    // Affiche la boîte de dialogue pour choisir le mode de jeu (Solo ou Multi-joueurs)
    private void showModeDialog() {
        String[] modes = {"Solo", "Multi-joueurs"};
        new AlertDialog.Builder(this)
            .setTitle("Choisissez le mode")
            .setItems(modes, (dialog, which) -> {
                if (which == 0) {
                    promptNames(1, new ArrayList<>(), 1);
                } else {
                    promptPlayerCount();
                }
            })
            .setCancelable(false)
            .show();
    }

    // Affiche la boîte de dialogue pour choisir le nombre de joueurs en mode multi
    private void promptPlayerCount() {
        String[] options = {"1 Joueur","2 Joueurs","3 Joueurs","4 Joueurs"};
        new AlertDialog.Builder(this)
            .setTitle("Nombre de joueurs")
            .setItems(options, (dialog, which) -> {
                int count = which + 1;
                promptNames(count, new ArrayList<>(), 1);
            })
            .setCancelable(false)
            .show();
    }

    // Demande le nom de chaque joueur de manière récursive
    private void promptNames(int total, ArrayList<String> names, int current) {
        final EditText nameInput = new EditText(this);
        nameInput.setHint("Nom joueur " + current);
        new AlertDialog.Builder(this)
            .setTitle("Nom du joueur " + current)
            .setView(nameInput)
            .setPositiveButton("OK", (dialog, which) -> {
                names.add(nameInput.getText().toString().trim());
                if (current < total) {
                    promptNames(total, names, current + 1);
                } else {
                    promptDifficulty(names);
                }
            })
            .setCancelable(false)
            .show();
    }

    // Affiche la boîte de dialogue pour choisir la difficulté du jeu
    private void promptDifficulty(ArrayList<String> names) {
        String[] levels = {"Facile","Normal","Difficile"};
        new AlertDialog.Builder(this)
            .setTitle("Choisissez la difficulté")
            .setItems(levels, (dialog, which) -> {
                Intent intent = new Intent(MainActivity.this, GameActivity.class);
                intent.putStringArrayListExtra("playerNames", names);
                intent.putExtra("difficulty", levels[which]);
                startActivity(intent);
            })
            .setCancelable(false)
            .show();
    }
}
