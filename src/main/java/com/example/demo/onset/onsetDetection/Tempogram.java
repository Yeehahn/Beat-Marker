package com.example.demo.onset.onsetDetection;
import java.util.Arrays;

import com.example.demo.onset.FFT.FourierCoefficients;
import com.example.demo.onset.FFT.Window.HannWindowFunction;
import com.example.demo.onset.FFT.Window.WindowFunction;

// This object is a tempogram of the song file
// The x-axis represents the time through the song
// THe y-axis represents the tempo
// And the size of each value is the "strength"
// of that tempo
// Construction of this object allows for the finding of bpm
// Via finding the median strongest bpm

public class Tempogram {

    private double[][] tempogram;
    private int windowSize;
    private double[][][] fftMap;
    private int hopSize;
    private int minBPM;
    private int maxBPM;
    private double sampleRate;
    private double timeSec;
    private double medianBPM;
    private int[] strongestTempoIndexArr;


    // Constructor constructs the tempogram from the novelty curve 
    // Uses a Fourier based approach by calculating fourier coefficients
    public Tempogram(double[] noveltyCurve, double songLengthSec, int hopSize, int windowSizeTime, double sampleRate, double timeSec){
        this.windowSize = (int)(windowSizeTime * sampleRate);
        this.hopSize = hopSize;
        this.sampleRate = sampleRate;
        this.minBPM = 50;
        this.maxBPM = 400;
        tempogram = new double[noveltyCurve.length / hopSize][maxBPM - minBPM];
        this.fftMap = new double[tempogram.length][2][maxBPM - minBPM];
        for(int i = 0; i < tempogram.length; i ++){
            double[] window = selectWindow(i * hopSize, noveltyCurve);
            fftMap[i] = FourierCoefficients.computeFourierCoefficients(minBPM, maxBPM, window, sampleRate);
            assignFrequencyMap(i, fftMap);
        }
        this.strongestTempoIndexArr = createStrongestTempoIndexArr(tempogram);
        this.medianBPM = findMedianBPM(strongestTempoIndexArr.clone());
    }
    
    // Selects a proper window of the windowSize
    // Makes sure that the window is centered properly
    // Is given an index to start creating a window from given novelty curve
    // Returns the window
    private double[] selectWindow(int index, double[] noveltyCurve){
        double[] window;
        // If window is at start of novelty curve
        if(index + windowSize / 2 < noveltyCurve.length && index - windowSize / 2 > 0){
            window = new double[windowSize];
            for(int i = 0; i < windowSize; i++){
                window[i] = noveltyCurve[i + index - windowSize / 2];
            }
        // If window is at the middle of the novelty curve
        } else if(index - windowSize / 2 > 0){
            window = new double[windowSize / 2 + noveltyCurve.length - index];
            for(int i = 0; i < window.length; i++){
                window[i] = noveltyCurve[noveltyCurve.length - 1 - window.length + i];
            }
        // If the window is at the end of the novlety curve
        } else{ // index + windowSize / 2 > noveltyCurve.length
            window = new double[windowSize / 2 + index];
            for(int i = 0; i < window.length; i++){
                window[i] = noveltyCurve[i];
            }
        }
        WindowFunction windowFunction = new HannWindowFunction(window.length);
        windowFunction.applyFunction(window);
        return window;
    }

    // Assigns the magnitudes of the coefficients in the fftMap to the tempogram
    private void assignFrequencyMap(int index, double[][][] fftMap){
        double[] magnitude = FourierCoefficients.absoluteValue(fftMap[index]);
        tempogram[index] = magnitude;
    }

    // Goes through the tempogram and stores the strongest tempo
    // At each time in the tempogram
    // Used to find the median bpm
    private int[] createStrongestTempoIndexArr(double[][] tempogram){
        int[] strongestTempoIndexArr = new int[tempogram.length];
        for(int i = 0; i < strongestTempoIndexArr.length; i++){
            strongestTempoIndexArr[i] = findIndexOfStrongestTempo(i, tempogram);
        }
        return strongestTempoIndexArr;
    }

    // Goes through the tempogram at the given time index
    // Finds the strongest tempo
    // Returns that tempo
    private int findIndexOfStrongestTempo(int index, double[][] tempogram){
        int maxIndex = -1;
        double max = Integer.MIN_VALUE;
        for(int i = 0; i < tempogram[index].length; i++){
            if(tempogram[index][i] > max){
                maxIndex = i;
                max = tempogram[index][i];
            }
        }
        return maxIndex;
    }

    // Finds the medianBPM from the strongestTempoIndexArr
    // Since to do so it must sort the array
    // Should send a copy in so that the strongest tempo index arr
    // can be used to construct the PLP curve
    private double findMedianBPM(int[] strongestTempoIndexArr){
        Arrays.sort(strongestTempoIndexArr);
        if(strongestTempoIndexArr.length % 2 == 1){
            return strongestTempoIndexArr[strongestTempoIndexArr.length / 2] + minBPM;
        } else{
            return (strongestTempoIndexArr[strongestTempoIndexArr.length / 2] + strongestTempoIndexArr[strongestTempoIndexArr.length / 2]) / 2.0 + minBPM;
        }
    }

    public double[][] getTempogram(){
        return tempogram;
    }

    public PredominantLocalPulse createPLPCurve(){
        return new PredominantLocalPulse(tempogram, fftMap, strongestTempoIndexArr, windowSize, hopSize, minBPM, sampleRate, timeSec);
    }

    public double getMedianBPM(){
        return medianBPM;
    }


}
