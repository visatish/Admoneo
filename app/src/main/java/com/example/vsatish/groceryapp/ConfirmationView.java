package com.example.vsatish.groceryapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;

public class ConfirmationView extends AppCompatActivity {
    public ArrayList<Store> stores;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation_view);
        stores = HomePage.stores;

        //display items
        for(int i = 0; i < stores.size(); i++) {
            Store temp = stores.get(i);

        }
    }
}
