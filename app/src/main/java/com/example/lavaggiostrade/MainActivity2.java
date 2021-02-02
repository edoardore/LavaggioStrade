package com.example.lavaggiostrade;

import androidx.appcompat.app.AppCompatActivity;

import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class MainActivity2 extends AppCompatActivity {
    private FirebaseDatabase rootNode;
    private DatabaseReference reference;
    private Button submitBtn;
    private EditText start, end;
    private CheckBox first, second, third, forth;
    private Switch lun, mar, mer, gio, ven, sab, dom;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        first = findViewById(R.id.checkBox17);
        second = findViewById(R.id.checkBox18);
        third = findViewById(R.id.checkBox19);
        forth = findViewById(R.id.checkBox20);

        lun = findViewById(R.id.switch9);
        mar = findViewById(R.id.switch10);
        mer = findViewById(R.id.switch11);
        gio = findViewById(R.id.switch12);
        ven = findViewById(R.id.switch13);
        sab = findViewById(R.id.switch14);
        dom = findViewById(R.id.switch15);

        start = findViewById(R.id.editTextTime5);
        end = findViewById(R.id.editTextTime7);

        submitBtn = findViewById(R.id.button7);


        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((lun.isChecked() || mar.isChecked() || mer.isChecked() || gio.isChecked() || ven.isChecked() || sab.isChecked() || dom.isChecked()) && !start.getText().toString().equals("") && !end.getText().toString().equals("")) {
                    Location l = MainActivity.getLocation();
                    double lat = l.getLatitude();
                    double lon = l.getLongitude();
                    String pos = (String) (lat + "-" + lon);
                    pos = pos.replace(".", ",");
                    rootNode = FirebaseDatabase.getInstance();
                    reference = rootNode.getReference(pos);
                    String week = "";
                    if (first.isChecked()) {
                        week = week + "1";
                    }
                    if (second.isChecked()) {
                        week = week + "2";
                    }
                    if (third.isChecked()) {
                        week = week + "3";
                    }
                    if (forth.isChecked()) {
                        week = week + "4";
                    }
                    reference.child("Week").setValue(week);
                    String day = "";
                    if (lun.isChecked()) {
                        day = day + "Lun";
                    }
                    if (mar.isChecked()) {
                        day = day + "Mar";
                    }
                    if (mer.isChecked()) {
                        day = day + "Mer";
                    }
                    if (gio.isChecked()) {
                        day = day + "Gio";
                    }
                    if (ven.isChecked()) {
                        day = day + "Ven";
                    }
                    if (sab.isChecked()) {
                        day = day + "Sab";
                    }
                    if (dom.isChecked()) {
                        day = day + "Dom";
                    }
                    reference.child("Day").setValue(day);
                    String inizio = start.getText().toString();
                    reference.child("Start").setValue(inizio);
                    String fine = end.getText().toString();
                    reference.child("End").setValue(fine);
                    showToast();
                    finish();
                } else {
                    showErrorToast();
                }
            }
        });
    }

    private void showToast() {
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(this, "Done!", duration);
        toast.show();
    }

    private void showErrorToast() {
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(this, "Compile all fields!", duration);
        toast.show();
    }
}