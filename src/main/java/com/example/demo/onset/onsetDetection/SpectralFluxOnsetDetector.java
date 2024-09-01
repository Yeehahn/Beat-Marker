package com.example.demo.onset.onsetDetection;


import com.example.demo.onset.FFT.FourierCoefficients;
import com.example.demo.onset.FFT.Window.HannWindowFunction;
import com.example.demo.onset.FFT.Window.WindowFunction;

import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import java.util.*;


import org.apache.commons.math3.transform.DftNormalization;

// This object finds and estimates the bpm of the song sent to the fileHolder
// Constructs a novelty curve, tempogram, and predominant local pulse curve 
// Also uses the predominant local pulse curve to find where all beats are

public class SpectralFluxOnsetDetector {
    private double[] signal;
    private int windowSize; // Should be a power of 2, is also FFT size
    private int hopSize;
    // x-axis is time; y-axis is frequencies but groups frequencies by binsize
    private double[][] spectrogram; 
    private WindowFunction windowFunction;
    private int songLengthMili; // Time is in miliseconds
    private double lambda;
    private double[] noveltyCurve;
    private double sigma;
    private double medianBPMFromTempogram;
    private PredominantLocalPulse plpCurve;
    private List<Double> peakTimes;



    public SpectralFluxOnsetDetector(double[] soundSignal, int windowSize, int overlapFactor, int songLengthMili, double lambda, double sigma){
        this.windowSize = windowSize;
        this.hopSize = windowSize / overlapFactor;
        this.songLengthMili = songLengthMili;
        this.signal = soundSignal; 
        this.windowFunction = new HannWindowFunction(windowSize);
        this.lambda = lambda;
        this.spectrogram = new double[soundSignal.length / hopSize][windowSize / 2];
        this.sigma = sigma;
        process();
    }

    // Processes the signal and stores it into the spectrogram
    // Reference Weekly Checkpoint 4 for algorithm
    private void process(){
        createSpectrogram();
        differentiate(this.spectrogram);
        double[] tempNoveltyCurve = new double[this.spectrogram.length];
        createnoveltyCurve(this.spectrogram, tempNoveltyCurve);
        Tempogram tempogram = new Tempogram(noveltyCurve, songLengthMili / 1000.0, 4, 6, noveltyCurve.length / (songLengthMili / 1000.0), songLengthMili / 1000.0);
        this.plpCurve = tempogram.createPLPCurve();
        this.medianBPMFromTempogram = tempogram.getMedianBPM();
        this.peakTimes = plpCurve.getPeakTimes();
    }
    

    // Creates a spectrogram out of small windows from the signal
    // Stores the absolute value of the fourier coefficient into the spectrogram
    private void createSpectrogram(){
        FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);
        for(int i = 0; i * hopSize + windowSize < signal.length; i ++){
            double[] window = selectWindow(i * hopSize);
            double[] magnitudeFrequency = FourierCoefficients.absoluteValue(fft.transform(window, TransformType.FORWARD));
            assignFrequencyMapToSpectogram(i, magnitudeFrequency);
        }
    }
    
    // Creates the window array to be FFT'd since it has to be an array of size 2048
    private double[] selectWindow(int index){
        double[] window = new double[windowSize];
        for(int i = 0; i < window.length; i++){
            window[i] = this.signal[i + index];
        }

        windowFunction.applyFunction(window);
        return window;
    }




    // Algorithm based on this
    // https://musicinformationretrieval.com/magnitude_scaling.html
    // Assigns the frequency map to the spectogram using a log compression
    private void assignFrequencyMapToSpectogram(int index, double[] frequencyMap){
        for(int i = 0; i < frequencyMap.length; i++){
            spectrogram[index][i] = Math.log10(1 + lambda * frequencyMap[i]);
        }
    }

    // Calculates an estimate of the derivative of each frequency band of the spectrogram
    // in a row wise fashion
    // The last window  derivative is kept the same as the previous window
    private void differentiate(double[][] spectrogram){
        for(int i = 0; i < spectrogram[0].length ; i++){
            for(int j = 0; j < spectrogram.length - 1; j++){
                spectrogram[j][i] = Math.max(spectrogram[j + 1][i] - spectrogram[j][i], 0);
            }
        }
        spectrogram[spectrogram.length - 1] = spectrogram[spectrogram.length - 2];
    }

    // Creates the frequency energy array by summing up the energy in a given spectrogram
    // Then thresholds it, smooths it, thresholds it, resamples it
    // That becomes the novelty curve
    // Done in order to minimize noise
    private void createnoveltyCurve(double[][] spectrogram, double[] tempNoveltyCurve){
        NoveltyProcessor.sumFrequency(spectrogram, tempNoveltyCurve);
        NoveltyProcessor.threshold(tempNoveltyCurve, 35);
        NoveltyProcessor.gaussianSmooth(tempNoveltyCurve, 11, sigma);
        noveltyCurve = NoveltyProcessor.threshold(tempNoveltyCurve, 35);
        // noveltyCurve = NoveltyProcessor.resampleNoveltyCurve(tempNoveltyCurve, 256, songLengthMili / 1000.0);
    }

    public double[][] getSpectrogram(){
        return spectrogram;
    }

    public double[] getNoveltyCurve(){
        return this.noveltyCurve;
    }

    public double[] getplpCurve(){
        return plpCurve.getPLPCurve();
    }
    
    public double getMedianBPM(){
        return this.medianBPMFromTempogram;
    }

    public List<Double> getPeakTimes(){
        return peakTimes;
    }
}
