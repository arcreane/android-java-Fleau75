package com.memorychallenge;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// Classe qui gère la génération et le stockage des séquences de couleurs
public class Sequence {
    // Liste qui stocke la séquence de couleurs (0-3)
    private final List<Integer> items = new ArrayList<>();
    // Générateur de nombres aléatoires
    private final Random random = new Random();

    // Génère une nouvelle séquence de couleurs de la longueur spécifiée
    public void generate(int length) {
        items.clear();  // Efface la séquence précédente
        for (int i = 0; i < length; i++) {
            items.add(random.nextInt(4)); // 0-3 pour les 4 couleurs (Rouge, Vert, Bleu, Jaune)
        }
    }

    // Retourne la séquence de couleurs actuelle
    public List<Integer> getItems() {
        return items;
    }
}
