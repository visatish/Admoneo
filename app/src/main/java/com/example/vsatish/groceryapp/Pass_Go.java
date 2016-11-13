package com.example.vsatish.groceryapp;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;

public class Pass_Go extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_pass__go);
        final String name = getIntent().getStringExtra("Name");

        //create AlertDialog
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        // set title
        alertDialogBuilder.setTitle("Admoneo");

        // set dialog message
        alertDialogBuilder
                .setMessage("Pass or Go?")
                .setCancelable(false)
                .setPositiveButton("Go",new DialogInterface.OnClickListener() {
                    ArrayList<Store> stores = read();
                    public void onClick(DialogInterface dialog,int id) {
                        Iterator<Store> iter = stores.iterator();
                        while(iter.hasNext()) {
                            if (iter.next().name.equals(name)) {
                                iter.remove();
                            }
                        }
                        save(stores);
                    }
                })
                .setNegativeButton("Pass",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        dialog.dismiss();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    public ArrayList<Store> read() {
        FileInputStream fis;
        ObjectInputStream ois=null;
        try {
            fis=openFileInput("stores");
            ois=new ObjectInputStream(fis);
            return (ArrayList<Store>)ois.readObject();
        }
        catch(ClassNotFoundException ex)
        {
            Log.e("add customers","serialization problem",ex);
        }
        catch(IOException ex)
        {
            Log.e("add customers","No customers file",ex);
        }
        finally
        {
            try {
                if (ois != null) {
                    ois.close();
                }
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
        return null;
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
}
