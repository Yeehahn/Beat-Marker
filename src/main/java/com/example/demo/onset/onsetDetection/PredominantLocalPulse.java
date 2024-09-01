package com.example.demo.onset.onsetDetection;

import java.util.*;

import com.example.demo.onset.FFT.Window.HannWindowFunction;
import com.example.demo.onset.FFT.Window.WindowFunction;

// Constructs a predominant local pulse curve
// PLP curve is the summation of a series of kernels
// Kernels constructed from tempogram
// PLP curve is used to find where beats are in file song
// That's stored in peakTimes

public class PredominantLocalPulse {
    private double[] predominantLocalPulseCurve;
    private int minBPM;
    private WindowFunction windowFunction;
    private double sampleRate;
    private List<Double> peakTimes;

    public PredominantLocalPulse(double[][] tempogram, double[][][] fftMap, int[] strongestTempoIndexArr, int windowSize, int hopSize, int minBPM, double sampleRate, double timeSec){
        predominantLocalPulseCurve = new double[tempogram.length * hopSize];
        this.minBPM = minBPM;
        this.sampleRate = sampleRate;
        windowFunction = new HannWindowFunction(windowSize);

        for(int i = 0; i < tempogram.length; i ++){
            int strongestTempoIndex = strongestTempoIndexArr[i];
            double[] plpKernel = generatePLPKernel(tempogram, fftMap, windowSize, i, strongestTempoIndex);
            sumKernel(plpKernel, predominantLocalPulseCurve, i * hopSize);
        }
        for(int i = 0; i < predominantLocalPulseCurve.length; i++){
            predominantLocalPulseCurve[i] = Math.max(0 , predominantLocalPulseCurve[i]);
        }
        // Technically shouldn't be used on PLP curve
        // But it works
        NoveltyProcessor.threshold(predominantLocalPulseCurve, 35);

        this.peakTimes = new ArrayList<Double>();
        this.peakTimes = findPeaks(predominantLocalPulseCurve);
    }

    // Creates a PLP kernel from the strongest tempo (frequency)
    // Then it phase shifts it at the right place and creates the kernel
    // This kernel is then used to create PLP curve
    private double[] generatePLPKernel(double[][] tempogram, double[][][] fftMap, int windowSize, int index, int strongestTempoIndex){
        double[] plpKernel = new double[windowSize];
        double phaseShiftRad = findPhaseShift(index, strongestTempoIndex, fftMap);
        for(int i = 0; i < plpKernel.length; i++){
            plpKernel[i] = Math.cos(2 * Math.PI * ((strongestTempoIndex + minBPM) / (60.0 * sampleRate)) * i - phaseShiftRad); 
        }
        windowFunction.applyFunction(plpKernel);
        return plpKernel;
    }

    // Value returned is in radians
    // Finds the phase shift of the strongest frequency
    // Applying said phase shift allows for proper allignment of plp curve
    private double findPhaseShift(int index, int strongestTempoIndex, double[][][] fftMap){
        return Math.atan2(fftMap[index][1][strongestTempoIndex], fftMap[index][0][strongestTempoIndex]);
    }

    // Sums the plpKernel to the plpCurve at the given index
    private void sumKernel(double[] plpKernel, double[] plpCurve, int index){
        for(int i = 0; i < plpKernel.length; i++){
            if(index - plpKernel.length / 2 + i > 0 && index + i < plpCurve.length){
                plpCurve[index - plpKernel.length / 2 + i] += plpKernel[i];
            }
        }
    }

    // Goes through the plp curve
    // Whenever it finds a maximum it marks there as being a maximum
    private List<Double> findPeaks(double[] predominantLocalPulseCurve){
        for(int i = 1; i < predominantLocalPulseCurve.length -1 ; i++){
            if(predominantLocalPulseCurve[i-1] < predominantLocalPulseCurve[i] && predominantLocalPulseCurve[i+1] < predominantLocalPulseCurve[i]){
                this.peakTimes.add(((double) i) / sampleRate);
            }
        }
        return this.peakTimes;
    }

    public double[] getPLPCurve(){
        return this.predominantLocalPulseCurve;
    }

    public List<Double> getPeakTimes(){
        return peakTimes;
    }
}
