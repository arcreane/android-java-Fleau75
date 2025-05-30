package com.memorychallenge;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.Toast;

// Classe qui gère le gyroscope pour détecter les rotations de l'appareil
public class SensorHandler implements SensorEventListener {

    // Interface pour notifier les rotations complètes
    public interface TiltListener {
        void onRotationCompleted(); // Appelé quand les deux rotations sont complétées
    }

    private final SensorManager manager;
    private final Sensor gyroscope;      // Capteur gyroscope
    private TiltListener listener;
    private float rotationAngle = 0;     // Angle de rotation cumulé
    private static final float ROTATION_THRESHOLD = 120f; // Seuil réduit pour une rotation plus facile
    private int rotationCount = 0;        // Nombre de rotations détectées
    private long lastGyroTimestamp = 0;   // Horodatage du dernier événement gyroscope
    private static final String TAG = "SensorHandler";
    private Context context;
    private long lastRotationTime = 0;    // Pour éviter les détections trop rapides
    private static final long MIN_ROTATION_INTERVAL = 300; // Temps minimum réduit entre les rotations
    private boolean lastRotationWasClockwise = false; // Pour suivre le sens de la dernière rotation
    private float lastRotationSpeed = 0;  // Pour le filtrage du bruit

    // Constructeur : initialise le gyroscope
    public SensorHandler(Context ctx) {
        context = ctx;
        manager = (SensorManager) ctx.getSystemService(Context.SENSOR_SERVICE);
        gyroscope = manager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        
        // Vérifie si le gyroscope est disponible
        if (gyroscope == null) {
            Toast.makeText(context, "Attention : Gyroscope non disponible sur cet appareil", Toast.LENGTH_LONG).show();
            Log.w(TAG, "Gyroscope non disponible sur cet appareil");
        } else {
            Log.d(TAG, "Gyroscope trouvé : " + gyroscope.getName());
        }
    }

    // Définit le listener pour les événements de rotation
    public void setListener(TiltListener l) {
        listener = l;
    }

    // Active le gyroscope
    public void register() {
        if (gyroscope != null) {
            boolean success = manager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_GAME);
            if (success) {
                Log.d(TAG, "Gyroscope activé avec succès");
            } else {
                Log.e(TAG, "Erreur lors de l'activation du gyroscope");
                Toast.makeText(context, "Erreur: Impossible d'activer le gyroscope", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Désactive le gyroscope
    public void unregister() {
        if (gyroscope != null) {
        manager.unregisterListener(this);
            Log.d(TAG, "Gyroscope désactivé");
        }
    }

    // Réinitialise le compteur de rotations
    public void resetRotationCount() {
        rotationCount = 0;
        rotationAngle = 0;
        lastGyroTimestamp = 0;
        lastRotationTime = 0;
        lastRotationWasClockwise = false;
        lastRotationSpeed = 0;
        Log.d(TAG, "Compteur de rotations réinitialisé");
    }

    // Gère les événements du gyroscope
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            if (lastGyroTimestamp != 0) {
                // Calcul du temps écoulé entre les événements
                float dt = (event.timestamp - lastGyroTimestamp) * 1.0f / 1000000000.0f;
                
                // Calcul de la rotation autour de l'axe Y (rotation horizontale)
                float rotationSpeed = event.values[1];
                
                // Filtrage simple du bruit
                if (Math.abs(rotationSpeed) < 0.1f) {
                    rotationSpeed = 0;
                }
                
                // Moyenne avec la dernière vitesse pour lisser le mouvement
                rotationSpeed = (rotationSpeed + lastRotationSpeed) / 2.0f;
                lastRotationSpeed = rotationSpeed;
                
                rotationAngle += Math.toDegrees(rotationSpeed) * dt;

                // Log pour le débogage
                if (Math.abs(rotationAngle) > 45) {  // Log seulement les angles significatifs
                    Log.d(TAG, String.format("Angle de rotation : %.1f°", rotationAngle));
                }

                // Vérifie si une rotation est détectée
                if (Math.abs(rotationAngle) >= ROTATION_THRESHOLD) {
                    long currentTime = System.currentTimeMillis();
                    // Vérifie si assez de temps s'est écoulé depuis la dernière rotation
                    if (currentTime - lastRotationTime >= MIN_ROTATION_INTERVAL) {
                        boolean isClockwise = rotationAngle > 0;
                        
                        // Pour la première rotation, on accepte n'importe quel sens
                        if (rotationCount == 0) {
                            lastRotationWasClockwise = isClockwise;
                            rotationCount++;
                            Log.d(TAG, "Première rotation détectée : " + (isClockwise ? "horaire" : "anti-horaire"));
                            Toast.makeText(context, "Maintenant, tournez dans l'autre sens", Toast.LENGTH_SHORT).show();
                        }
                        // Pour la deuxième rotation, on vérifie que le sens est opposé
                        else if (rotationCount == 1 && isClockwise != lastRotationWasClockwise) {
                    rotationCount++;
                            Log.d(TAG, "Deuxième rotation détectée dans le sens opposé !");
                        }
                        // Si même sens que la première, on ignore
                        else if (rotationCount == 1) {
                            Log.d(TAG, "Rotation ignorée : même sens que la première");
                            Toast.makeText(context, "Tournez dans l'autre sens !", Toast.LENGTH_SHORT).show();
                        }
                        
                        lastRotationTime = currentTime;
                    rotationAngle = 0;
                    
                        // Retour haptique
                        if (rotationCount > 0) {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                android.os.VibrationEffect vibe = android.os.VibrationEffect.createOneShot(50, android.os.VibrationEffect.DEFAULT_AMPLITUDE);
                                ((android.os.Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(vibe);
                            }
                        }
                        
                        // Vérifie si les deux rotations sont complétées
                    if (rotationCount >= 2 && listener != null) {
                            Log.d(TAG, "Deux rotations complétées dans des sens opposés !");
                        listener.onRotationCompleted();
                        rotationCount = 0;
                        }
                    }
                }
            }
            lastGyroTimestamp = event.timestamp;
        }
    }

    @Override 
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d(TAG, "Précision du capteur " + sensor.getName() + " changée : " + accuracy);
    }
}
