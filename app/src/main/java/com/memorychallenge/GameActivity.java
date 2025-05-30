package com.memorychallenge;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.Button;
import android.widget.Toast;
import android.view.View;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

// Activité principale du jeu qui gère la logique du jeu de mémoire
public class GameActivity extends Activity {

    // Variables pour contrôler le timing de la séquence
    private long sequenceInterval, blinkDuration;
    private Sequence sequence;
    private int currentStep = 0;
    private List<Button> buttons = new ArrayList<>();
    private Handler handler = new Handler(Looper.getMainLooper());
    private Vibrator vib;
    private SensorHandler sensorHandler;
    private boolean inputEnabled = false;
    private boolean waitingForRotation = false;
    
    // Variables pour la gestion des joueurs
    private ArrayList<String> playerNames;
    private List<Integer> playerScores = new ArrayList<>();
    private int currentPlayer = 0;
    private String difficulty;
    private TextView messageText;

    // Initialisation de l'activité
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_game);
        
        // Initialisation des composants
        vib = (Vibrator)getSystemService(VIBRATOR_SERVICE);
        messageText = findViewById(R.id.messageText);
        
        // Configuration des boutons de couleur
        buttons.add(findViewById(R.id.btnRed));
        buttons.add(findViewById(R.id.btnGreen));
        buttons.add(findViewById(R.id.btnBlue));
        buttons.add(findViewById(R.id.btnYellow));
        
        // Récupération des paramètres du jeu
        playerNames = getIntent().getStringArrayListExtra("playerNames");
        difficulty = getIntent().getStringExtra("difficulty");
        for(int i = 0; i < playerNames.size(); i++) playerScores.add(0);
        
        // Configuration de la difficulté
        configureTimings(difficulty);
        
        // Configuration des listeners des boutons
        for(int i = 0; i < buttons.size(); i++) {
            final int idx = i;
            buttons.get(i).setOnClickListener(v -> onColorPressed(idx));
        }
        
        // Initialisation de la séquence et des capteurs
        sequence = new Sequence();
        sensorHandler = new SensorHandler(this);
        sensorHandler.setListener(new SensorHandler.TiltListener() {
            public void onTiltRight() { validate(); }
            public void onTiltLeft() { endTurn(); }
            public void onRotationCompleted() {
                if (waitingForRotation) {
                    waitingForRotation = false;
                    handler.post(() -> {
                        messageText.setVisibility(View.GONE);
                        showSequence();
                    });
                }
            }
        });
        startPlayer();
    }

    // Démarre le tour d'un joueur
    private void startPlayer() {
        currentStep = 0;
        Toast.makeText(this, "Tour de " + playerNames.get(currentPlayer), Toast.LENGTH_SHORT).show();
        sequence.generate(playerScores.get(currentPlayer) + 1);
        nextRound();
    }

    // Prépare le prochain round
    private void nextRound() {
        currentStep = 0;
        sequence.generate(playerScores.get(currentPlayer) + 1);
        if (playerNames.size() > 1) {
            // En mode multi-joueurs, attendre la rotation de l'appareil
            waitingForRotation = true;
            sensorHandler.resetRotationCount();
            messageText.setVisibility(View.VISIBLE);
            messageText.setText(playerNames.get(currentPlayer) + ", retourne l'appareil 2 fois pour voir la séquence !");
        } else {
            showSequence();
        }
    }

    // Affiche la séquence de couleurs à mémoriser
    private void showSequence() {
        inputEnabled = false;
        List<Integer> seq = sequence.getItems();
        for(int i = 0; i < seq.size(); i++) {
            int idx = seq.get(i);
            handler.postDelayed(() -> blink(idx), i * sequenceInterval);
        }
        handler.postDelayed(() -> inputEnabled = true, seq.size() * sequenceInterval);
    }

    // Fait clignoter un bouton
    private void blink(int i){
        Button b=buttons.get(i);
        b.setAlpha(0.3f);
        handler.postDelayed(()->b.setAlpha(1f), blinkDuration);
    }

    // Gère l'appui sur un bouton de couleur
    private void onColorPressed(int i){
        if(!inputEnabled) return;
        blink(i);
        if(sequence.getItems().get(currentStep)==i){
            currentStep++;
            if(currentStep==sequence.getItems().size()){
                vib.vibrate(VibrationEffect.createOneShot(100,VibrationEffect.DEFAULT_AMPLITUDE));
                playerScores.set(currentPlayer, playerScores.get(currentPlayer)+1);
                nextRound();
            }
        } else endTurn();
    }

    // Valide la séquence complète
    private void validate(){
        if(inputEnabled&&currentStep==sequence.getItems().size()) nextRound();
    }

    // Termine le tour du joueur actuel
    private void endTurn(){
        vib.vibrate(VibrationEffect.createOneShot(200,VibrationEffect.DEFAULT_AMPLITUDE));
        if(currentPlayer<playerNames.size()-1){
            currentPlayer++;
            startPlayer();
        } else showFinalRanking();
    }

    // Affiche le classement final
    private void showFinalRanking() {
        if (playerNames.size() <= 1) {
            // Mode solo : sauvegarde et affiche le score
            ScoreManager sm = new ScoreManager(this);
            sm.saveScore(playerScores.get(0), playerNames.get(0));
            Intent intent = new Intent(this, ScoreActivity.class);
            intent.putExtra("score", playerScores.get(0));
            intent.putStringArrayListExtra("playerNames", playerNames);
            intent.putExtra("difficulty", difficulty);
            startActivity(intent);
        } else {
            // Mode multi-joueurs : affiche le classement
            Intent i = new Intent(this, MultiScoreActivity.class);
            i.putStringArrayListExtra("playerNames", playerNames);
            i.putIntegerArrayListExtra("playerScores", new ArrayList<>(playerScores));
            i.putExtra("difficulty", difficulty);
            startActivity(i);
        }
        finish();
    }

    // Configure les timings selon le niveau de difficulté
    private void configureTimings(String level){
        switch(level){
            case "Facile": sequenceInterval=1200; blinkDuration=600; break;
            case "Difficile": sequenceInterval=800; blinkDuration=300; break;
            default: sequenceInterval=1000; blinkDuration=500;
        }
        Toast.makeText(this,"Difficulté: "+level,Toast.LENGTH_SHORT).show();
    }

    // Gestion du cycle de vie de l'activité
    @Override protected void onResume(){ super.onResume(); sensorHandler.register(); }
    @Override protected void onPause(){ super.onPause(); sensorHandler.unregister(); }
}
