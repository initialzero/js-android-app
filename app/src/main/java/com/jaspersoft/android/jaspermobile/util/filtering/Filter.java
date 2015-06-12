package com.jaspersoft.android.jaspermobile.util.filtering;

import java.util.ArrayList;

/**
 * @author Andrew Tivodar
 * @since 2.0
 */
public class Filter {

    private String mName;
    private ArrayList<String> mValues;

    public Filter(String name, ArrayList<String> values) {
        this.mName = name;
        this.mValues = values;
    }

    public ArrayList<String> getValues() {
        return mValues;
    }

    public String getName() {
        return mName;
    }
}
