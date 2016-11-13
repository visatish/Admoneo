package com.example.vsatish.groceryapp;

import java.util.Comparator;

/**
 * Created by VSatish on 11/12/16.
 */

public class PlaceComparator implements Comparator<Store> {
    @Override
    public int compare(Store s1, Store s2) {
        return s1.items.size() - s2.items.size(); // sort descending
    }
}
