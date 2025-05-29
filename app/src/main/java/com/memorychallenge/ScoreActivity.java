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

public class ScoreActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);

        boolean viewOnly = getIntent().getBooleanExtra("viewOnly", false);
        TextView finalScoreView = findViewById(R.id.finalScore);
        
        if (!viewOnly) {
            // Display final solo score
            int score = getIntent().getIntExtra("score", 0);
            String playerName = getIntent().getStringArrayListExtra("playerNames").get(0);
            finalScoreView.setText("Score de " + playerName + ": " + score);

            // Save score with player name
            ScoreManager sm = new ScoreManager(this);
            sm.saveScore(score, playerName);
        } else {
            finalScoreView.setVisibility(View.GONE);
        }

        // Load and display high scores
        ScoreManager sm = new ScoreManager(this);
        ArrayList<ScoreManager.ScoreEntry> highscores = sm.getAllScoresWithNames();

        ArrayList<String> list = new ArrayList<>();
        for (ScoreManager.ScoreEntry entry : highscores) {
            list.add(entry.playerName + " - " + entry.score);
        }

        ListView lv = findViewById(R.id.scoreList);
        lv.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list));

        Button menuButton = findViewById(R.id.menuButton);
        Button replayButton = findViewById(R.id.replayButton);

        if (viewOnly) {
            menuButton.setText("Retour");
            replayButton.setVisibility(View.GONE);
        } else {
            // Show replay button
            replayButton.setVisibility(View.VISIBLE);
            ArrayList<String> playerNames = getIntent().getStringArrayListExtra("playerNames");
            String difficulty = getIntent().getStringExtra("difficulty");
            
            replayButton.setOnClickListener(v -> {
                Intent intent = new Intent(this, GameActivity.class);
                intent.putStringArrayListExtra("playerNames", playerNames);
                intent.putExtra("difficulty", difficulty);
                startActivity(intent);
                finish();
            });
        }

        menuButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }
}
