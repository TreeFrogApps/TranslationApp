package com.treefrogapps.translationapp;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Translations {

    @SerializedName("translations")
    private ArrayList<Languages> languagesArrayList;

    public void setLanguagesArrayList(ArrayList<Languages> languagesArrayList) {
        this.languagesArrayList = languagesArrayList;
    }

    public ArrayList<Languages> getLanguagesArrayList() {
        return languagesArrayList;


    }

    public static class Languages {

        @SerializedName("language")
        private String language;

        @SerializedName("translation")
        private String translation;

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
