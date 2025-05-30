package com.memorychallenge;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

// Activité qui affiche le classement des joueurs en mode multijoueur
public class MultiScoreActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_score);

        // Récupération des données de la partie
        ArrayList<String> names = getIntent().getStringArrayListExtra("playerNames");
        ArrayList<Integer> scores = getIntent().getIntegerArrayListExtra("playerScores");
        String difficulty = getIntent().getStringExtra("difficulty");

        // Configuration du titre avec le niveau de difficulté
        TextView titleText = findViewById(R.id.titleText);
        titleText.setText("Classement - " + difficulty);

        // Trie les joueurs par score décroissant
        ArrayList<Integer> indices = new ArrayList<>();
        for (int i = 0; i < scores.size(); i++) {
            indices.add(i);
        }
        Collections.sort(indices, (a, b) -> scores.get(b) - scores.get(a));

        // Construit la liste d'affichage avec le classement
        ArrayList<String> display = new ArrayList<>();
        for (int i = 0; i < indices.size(); i++) {
            int idx = indices.get(i);
            String rank = (i + 1) + ". ";  // Ajoute le rang (1., 2., etc.)
            display.add(rank + names.get(idx) + " : " + scores.get(idx));
        }

        // Affiche le classement dans la ListView
        ListView listView = findViewById(R.id.playerList);
        listView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, display));

        // Configuration du bouton menu pour retourner à l'écran principal
        Button menuButton = findViewById(R.id.menuButton);
        menuButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        // Configuration du bouton rejouer pour démarrer une nouvelle partie avec les mêmes paramètres
        Button replayButton = findViewById(R.id.replayButton);
        replayButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, GameActivity.class);
            intent.putStringArrayListExtra("playerNames", names);
            intent.putExtra("difficulty", difficulty);
            startActivity(intent);
            finish();
        });
    }
}
