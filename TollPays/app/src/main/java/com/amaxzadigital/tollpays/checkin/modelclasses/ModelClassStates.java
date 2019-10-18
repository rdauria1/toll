package com.amaxzadigital.tollpays.checkin.modelclasses;

/**
 * Created by Hussain Marvi on 28-Mar-17.
 */

public class ModelClassStates {
    private String name, abbreviation;

    public ModelClassStates(String _name, String _abbreviation) {
        setName(_name);
        setAbbreviation(_abbreviation);
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
