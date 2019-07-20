package com.songoda.ultimatecatcher.utils.settings;

import com.songoda.ultimatecatcher.UltimateCatcher;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum Setting {

    USE_CATCHER_RECIPE("Main.Use Catcher Recipe", true,
            "Should egg recipes be enabled."),

    VAULT_ECONOMY("Economy.Use Vault Economy", true,
            "Should Vault be used?"),

    RESERVE_ECONOMY("Economy.Use Reserve Economy", true,
            "Should Reserve be used?"),

    PLAYER_POINTS_ECONOMY("Economy.Use Player Points Economy", false,
            "Should PlayerPoints be used?"),

    LANGUGE_MODE("System.Language Mode", "en_US",
            "The enabled language file.",
            "More language files (if available) can be found in the plugins data folder.");

    private String setting;
    private Object option;
    private String[] comments;

    Setting(String setting, Object option, String... comments) {
        this.setting = setting;
        this.option = option;
        this.comments = comments;
    }

    Setting(String setting, Object option) {
        this.setting = setting;
        this.option = option;
        this.comments = null;
    }

    public static Setting getSetting(String setting) {
        List<Setting> settings = Arrays.stream(values()).filter(setting1 -> setting1.setting.equals(setting)).collect(Collectors.toList());
        if (settings.isEmpty()) return null;
        return settings.get(0);
    }

    public String getSetting() {
        return setting;
    }

    public Object getOption() {
        return option;
    }

    public String[] getComments() {
        return comments;
    }

    public List<Integer> getIntegerList() {
        return UltimateCatcher.getInstance().getConfig().getIntegerList(setting);
    }

    public List<String> getStringList() {
        return UltimateCatcher.getInstance().getConfig().getStringList(setting);
    }

    public boolean getBoolean() {
        return UltimateCatcher.getInstance().getConfig().getBoolean(setting);
    }

    public int getInt() {
        return UltimateCatcher.getInstance().getConfig().getInt(setting);
    }

    public long getLong() {
        return UltimateCatcher.getInstance().getConfig().getLong(setting);
    }

    public String getString() {
        return UltimateCatcher.getInstance().getConfig().getString(setting);
    }

    public char getChar() {
        return UltimateCatcher.getInstance().getConfig().getString(setting).charAt(0);
    }

    public double getDouble() {
        return UltimateCatcher.getInstance().getConfig().getDouble(setting);
    }
}