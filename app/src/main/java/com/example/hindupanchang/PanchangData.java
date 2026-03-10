package com.example.hindupanchang;

public class PanchangData {
    public final String tithi;
    public final String nakshatra;
    public final String maas;
    public final String ritu;
    public final String samvatsar;
    public final int vikramSamvat;
    public final String sunrise;
    public final String sunset;
    public final String moonrise;
    public final String moonset;
    public final String hinduTime;

    public PanchangData(String tithi, String nakshatra, String maas, String ritu, String samvatsar,
                       int vikramSamvat, String sunrise, String sunset, String moonrise,
                       String moonset, String hinduTime) {
        this.tithi = tithi;
        this.nakshatra = nakshatra;
        this.maas = maas;
        this.ritu = ritu;
        this.samvatsar = samvatsar;
        this.vikramSamvat = vikramSamvat;
        this.sunrise = sunrise;
        this.sunset = sunset;
        this.moonrise = moonrise;
        this.moonset = moonset;
        this.hinduTime = hinduTime;
    }
}
