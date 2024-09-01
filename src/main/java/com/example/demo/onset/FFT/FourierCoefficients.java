package com.example.demo.onset.FFT;

import java.util.stream.IntStream;

import org.apache.commons.math3.complex.Complex;

// This class manually computes the fourier coefficients at set frequencies
// These frequencies correspond to reasonable BPM's in a range
// Slow compared to typical FFT methods but it gives much better resolution at 
// Low frequencies
// Uses multi-threading to process it faster

public class FourierCoefficients {

    // Takes a range of BPM to calculate, 
    // the window that it is finding the fourier coefficients for
    // And then finally the sampleRate which is used in calculations
    // Then uses multiThreading to speed up process
    public static double[][] computeFourierCoefficients(int minBPM, int maxBPM, double[] window, double sampleRate){
        // Index 0 corresponds to real, index 1 corresponds to imaginary
        double[][] fourierCoefficients = new double[2][maxBPM - minBPM];

        IntStream.range(0, fourierCoefficients[0].length).parallel().forEach(i -> {
            double coefficientReal = 0.0;
            double coefficientImaginary = 0.0;
            // Omega is frequency of the sinusoidal wave
            double omega = (i + minBPM) / (60.0 * sampleRate);
            for(int j = 0; j < window.length; j++){
                // Calculations follow standard FT equation
                coefficientReal += window[j] * Math.cos(-2 * Math.PI * omega * j);
                coefficientImaginary += window[j] * Math.sin(-2 * Math.PI * omega * j);
            }
            fourierCoefficients[0][i] = coefficientReal;
            fourierCoefficients[1][i] = coefficientImaginary;
        });

        return fourierCoefficients;
    }

    // Just takes the absolute value of each value in the frequency map
    // stores that into array and returns t
    public static double[] absoluteValue(Complex[] frequencyMap){
        double[] magnitude = new double[frequencyMap.length / 2];
        // Since the FFT mirrors the values in the latter half of the array
        // Only care about the first half so just save some time by taking first half
        for(int i = 0; i < magnitude.length; i++){
            magnitude[i] = frequencyMap[i].abs();
        }
        return magnitude;
    }

    public static double[] absoluteValue(double[][] frequencyMap){
        double[] magnitude = new double[frequencyMap[0].length / 2];
        // Since the FFT mirrors the values in the latter half of the array
        // Only care about the first half so just save some time by taking first half
        for(int i = 0; i < magnitude.length; i++){
            magnitude[i] = Math.sqrt(Math.pow(frequencyMap[0][i], 2) + Math.pow(frequencyMap[1][i], 2));
        }
        return magnitude;
    }
}
