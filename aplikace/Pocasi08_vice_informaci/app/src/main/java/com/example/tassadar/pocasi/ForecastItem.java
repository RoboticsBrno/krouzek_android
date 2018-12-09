package com.example.tassadar.pocasi;

import java.io.Serializable;
import java.util.Date;

public class ForecastItem implements Serializable {
    Date date;
    double temperature;
    double rain;
    double clouds;
    double wind;
    double pressure;
}
