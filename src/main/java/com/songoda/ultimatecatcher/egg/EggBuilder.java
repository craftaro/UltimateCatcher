package com.songoda.ultimatecatcher.egg;

import java.util.List;

public final class EggBuilder {

    private final CEgg egg;

    public EggBuilder(String key) {
        this.egg = new CEgg(key);
    }

    public EggBuilder setName(String name) {
        this.egg.setName(name);
        return this;
    }

    public EggBuilder setRecipe(List<String> recipe) {
        this.egg.setRecipe(recipe);
        return this;
    }

    public EggBuilder setCost(double price) {
        this.egg.setCost(price);
        return this;
    }

    public EggBuilder setChance(int chance) {
        this.egg.setChance(chance);
        return this;
    }

    public CEgg build() {
        return egg;
    }
}
