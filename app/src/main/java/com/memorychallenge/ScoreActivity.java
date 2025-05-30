package com.memorychallenge;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

// Activité qui affiche les scores en mode solo et le tableau des meilleurs scores
public class ScoreActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);

        // Détermine si l'activité est ouverte en mode visualisation uniquement
        boolean viewOnly = getIntent().getBooleanExtra("viewOnly", false);
        TextView finalScoreView = findViewById(R.id.finalScore);
        
        if (!viewOnly) {
            // Affiche le score final du joueur en mode solo
            int score = getIntent().getIntExtra("score", 0);
            String playerName = getIntent().getStringArrayListExtra("playerNames").get(0);
            finalScoreView.setText("Score de " + playerName + ": " + score);

            // Sauvegarde le score avec le nom du joueur
            ScoreManager sm = new ScoreManager(this);
            sm.saveScore(score, playerName);
        } else {
            // Cache l'affichage du score final en mode visualisation
            finalScoreView.setVisibility(View.GONE);
        }

        // Charge et affiche les meilleurs scores
        ScoreManager sm = new ScoreManager(this);
        ArrayList<ScoreManager.ScoreEntry> highscores = sm.getAllScoresWithNames();

        // Prépare la liste des scores pour l'affichage
        ArrayList<String> list = new ArrayList<>();
        for (ScoreManager.ScoreEntry entry : highscores) {
            list.add(entry.playerName + " - " + entry.score);
        }

        // Configure la liste des scores
        ListView lv = findViewById(R.id.scoreList);
        lv.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list));

        // Configuration des boutons
        Button menuButton = findViewById(R.id.menuButton);
        Button replayButton = findViewById(R.id.replayButton);

        if (viewOnly) {
            // Mode visualisation : affiche uniquement le bouton retour
            menuButton.setText("Retour");
            replayButton.setVisibility(View.GONE);
        } else {
            // Mode fin de partie : affiche le bouton rejouer
            replayButton.setVisibility(View.VISIBLE);
            ArrayList<String> playerNames = getIntent().getStringArrayListExtra("playerNames");
            String difficulty = getIntent().getStringExtra("difficulty");
            
            // Configure le bouton rejouer pour relancer une partie
            replayButton.setOnClickListener(v -> {
                Intent intent = new Intent(this, GameActivity.class);
                intent.putStringArrayListExtra("playerNames", playerNames);
                intent.putExtra("difficulty", difficulty);
                startActivity(intent);
                finish();
            });
        }

        // Configure le bouton menu pour retourner à l'écran principal
        menuButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }
}
