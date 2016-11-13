package com.example.vsatish.groceryapp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Collections;

public class HomePage extends AppCompatActivity {
    public static ArrayList<Store> stores = new ArrayList<Store>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {

            ActivityCompat.requestPermissions( this, new String[] {  Manifest.permission.ACCESS_FINE_LOCATION},
                    0 );
        }
    }

    public void save(ArrayList<Store> stores) {
        FileOutputStream fos;
        ObjectOutputStream ous=null;
        try {
            fos=openFileOutput("stores", Activity.MODE_PRIVATE);
            ous=new ObjectOutputStream(fos);
            ous.writeObject(stores);
        }

        catch(IOException ex)
        {
            Log.e("add customers","some problem",ex);
        }
        finally
        {
            try {
                if(ous != null) {
                    ous.close();
                }
            } catch (IOException e) {

            }
        }
    }

    public void parse_text(View view) {
        String text = ((EditText)findViewById(R.id.stores_items)).getText().toString();
        Log.d("HomePage", text);
        parse(text);
        save(stores);

        Intent intent = new Intent(this, CustomService.class);
        startService(intent);
    }

    private void parse(String text) {
        StringTokenizer myToken = new StringTokenizer(text, "\n");
//        Log.d("HomePage", myToken.toString());
        ArrayList<String> items = new ArrayList<String>();;
        String name = null;
        while (myToken.hasMoreTokens()) {
            String token = myToken.nextToken();
            Log.d("HomePage", token);
            String firstChar = token.substring(0, 1);
            if (!firstChar.equals("-")) {
                stores.add(new Store(name, items));
                items = new ArrayList<String>();
                name = token;
            }
            else {
                items.add(token.substring(1, token.length()));
            }

        }
        stores.add(new Store(name, items));
        stores.remove(0);
        Collections.sort(stores, new PlaceComparator());
        Collections.reverse(stores);
        Log.d("HomePage", stores.toString());
    }
}
