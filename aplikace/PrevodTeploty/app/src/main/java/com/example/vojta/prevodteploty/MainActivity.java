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
                TextView result = findViewById(R.id.vysledek);
                EditText input = findViewById(R.id.vstup);
                try {
                    double cel = Double.parseDouble(input.getText().toString());
                    result.setText(celToFar(cel) + " °F");
                } catch(NumberFormatException e) {
                    result.setText("Neplatný vstup!");
                } catch(ArithmeticException e) {
                    result.setText(e.toString());
                }
            }
        });
    }

    double celToFar(double cel) throws ArithmeticException {
        if(cel <= -273.15) {
            throw new ArithmeticException("Zadana teplota je pod absolutni nulou!");
        }

        return cel * 9/5 + 32;
    }
}
