package com.example.vojta.prevodteploty;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button prevest = findViewById(R.id.prevestBtn);

        prevest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Najdeme si vstupni a vystupni pole
                TextView result = findViewById(R.id.vysledek);
                EditText input = findViewById(R.id.vstup);

                // Ziskame obsah policka “vstup” jako text
                String celsiusStr = input.getText().toString();

                /*
                // Prevedeme ho na desetinne cislo
                double celsius = Double.parseDouble(celsiusStr);

                // Pouzijeme nasi fci na prevod na °F
                double fr = celToFar(celsius);
                // Nastavime vysledek do prvku "vysledek"
                result.setText(fr + " °F");
                */

                try {
                    // Prevedeme ho na desetinne cislo
                    double celsius = Double.parseDouble(celsiusStr);

                    // Pouzijeme nasi fci na prevod na °F
                    double fr = celToFar(celsius);
                    // Nastavime vysledek do prvku "vysledek"
                    result.setText(fr + " °F");
                } catch(NumberFormatException e) {
                    result.setText("Neplatný vstup!");
                }
            }
        });
    }

    double celToFar(double cel) {
        return cel * 9/5 + 32;
    }
}
