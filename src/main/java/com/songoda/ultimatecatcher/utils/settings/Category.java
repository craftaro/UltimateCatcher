package com.songoda.ultimatecatcher.utils.settings;

public enum Category {

    MAIN("General settings and options."),

    ECONOMY("Settings regarding economy.",
            "Only one economy option can be used at a time. If you enable more than",
            "one of these the first one will be used."),

    SYSTEM("System related settings.");

    private String[] comments;


    Category(String... comments) {
        this.comments = comments;
    }

    public String[] getComments() {
        return comments;
    }
}