
package com.memorychallenge;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Sequence {
    private final List<Integer> items = new ArrayList<>();
    private final Random random = new Random();

    public void generate(int length) {
        items.clear();
        for (int i = 0; i < length; i++) {
            items.add(random.nextInt(4)); // 0-3 pour 4 couleurs
        }
    }

    public List<Integer> getItems() {
        return items;
    }
}
