package com.songoda.ultimatecatcher.egg;

import java.util.ArrayDeque;
import java.util.Deque;

public class EggManager {

    private final Deque<CEgg> registeredEggs = new ArrayDeque<>();

    public CEgg addEgg(CEgg egg) {
        registeredEggs.add(egg);
        return egg;
    }

    public CEgg removeEgg(CEgg egg) {
        registeredEggs.remove(egg);
        return egg;
    }

    public CEgg getEgg(String key) {
        return registeredEggs.stream().filter(egg -> egg.getKey().equalsIgnoreCase(key)).findFirst().orElse(null);
    }

    public CEgg getFirstEgg() {
        return registeredEggs.getFirst();
    }

    public Deque<CEgg> getRegisteredEggs() {
        return new ArrayDeque<>(registeredEggs);
    }
}
