package com.songoda.ultimatecatcher.utils.settings;

public enum Category {

    MAIN("General settings and options."),

    SYSTEM("System related settings.");

    private String[] comments;


    Category(String... comments) {
        this.comments = comments;
    }

    public String[] getComments() {
        return comments;
    }
}