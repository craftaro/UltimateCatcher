package com.songoda.ultimatecatcher;

public class References {

    private String prefix;

    public References() {
        prefix = UltimateCatcher.getInstance().getLocale().getMessage("general.nametag.prefix") + " ";
    }

    public String getPrefix() {
        return this.prefix;
    }
}