package com.treefrogapps.translationapp;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Translations {

    // GSON will use the JSON serialized name and connect it with this variable
    // I want a Array of languages ( ArrayList<Languages> ) based on the JSON data
    @SerializedName("translations")
    private ArrayList<Languages> languagesArrayList;

    // Setter & Getter for ArrayList
    public void setLanguagesArrayList(ArrayList<Languages> languagesArrayList) {
        this.languagesArrayList = languagesArrayList;
    }

    public ArrayList<Languages> getLanguagesArrayList() {
        return languagesArrayList;
    }

    // Inner class 'Languages' (what we want an ArrayList of) - make GSON inner classes static
    public static class Languages {

        // GSON will get serialized name and put content into String language
        @SerializedName("language")
        private String language;

        // GSON will get serialized name and put content into String translation
        @SerializedName("translation")
        private String translation;

        // Setter and Getters - setters shouldn't be necessary
        // as handles by GSON (apparently) - added anyway!
        public void setLanguage(String language) {
            this.language = language;
        }

        public void setTranslation(String translation) {
            this.translation = translation;
        }

        public String getTranslation() {
            return translation;
        }

        public String getLanguage() {
            return language;
        }

    }


}
