package com.cps.legendofbounca;

public class Config {
    public static double DISSIPATION_COEFFICIENT = .1;
    public static double MASS = 0.1;
    public static double MU_S = .15;
    public static double MU_K = .07;

    public static float NS2US = 1.0f / 1000.0f; // ns to microsecond
    public static float US2S = 1.0f / 1000000.0f; // microsecond to second
    public static int READ_SENSOR_RATE = 20; // sensor read rate in microsecond
    public static int UPDATE_VIEW_RATE = 15 * 1000; // refresh View rate in microsecond
    public static double GRAVITY_CONSTANT = 9.80665;
}
