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

public class MultiScoreActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_score);

        ArrayList<String> names = getIntent().getStringArrayListExtra("playerNames");
        ArrayList<Integer> scores = getIntent().getIntegerArrayListExtra("playerScores");
        String difficulty = getIntent().getStringExtra("difficulty");

        TextView titleText = findViewById(R.id.titleText);
        titleText.setText("Classement - " + difficulty);

        // Sort players by score descending
        ArrayList<Integer> indices = new ArrayList<>();
        for (int i = 0; i < scores.size(); i++) {
            indices.add(i);
        }
        Collections.sort(indices, (a, b) -> scores.get(b) - scores.get(a));

        // Build display list with ranking
        ArrayList<String> display = new ArrayList<>();
        for (int i = 0; i < indices.size(); i++) {
            int idx = indices.get(i);
            String rank = (i + 1) + ". ";
            display.add(rank + names.get(idx) + " : " + scores.get(idx));
        }

        // Show in ListView
        ListView listView = findViewById(R.id.playerList);
        listView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, display));

        // Menu button: go back to MainActivity
        Button menuButton = findViewById(R.id.menuButton);
        menuButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        // Replay button: start a new game with same players and difficulty
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
