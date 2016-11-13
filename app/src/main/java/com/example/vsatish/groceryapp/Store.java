package com.example.vsatish.groceryapp;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by VSatish on 11/12/16.
 */

public class Store implements Serializable{
    String name;
    ArrayList<String> items = new ArrayList<String>();

    public Store(String name, ArrayList<String> items) {
        this.name = name;
        this.items = items;
    }

    public String toString() {
        return name + " " + items.toString() + "\n";
    }
}
