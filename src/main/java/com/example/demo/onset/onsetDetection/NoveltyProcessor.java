package com.example.demo.onset.onsetDetection;

import java.util.Arrays;

import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

// This class is primarily made to process the novelty curve
// SO that the novelty curve is cleaner
// Primarily removing noise and resampling it
// Also constructs the novelty curve from the spectrogram

public class NoveltyProcessor {
    // Takes a spectrogram and sums up all of the energy at each window
    // Assigns this sum to the noveltyCurve array
    // Also returns the mean of all values
    public static double sumFrequency(double[][] spectrogram, double[] noveltyCurve){
        double mean = 0.0;
        for(int i = 0; i < spectrogram.length; i++){
            for(int j = 0; j < spectrogram[i].length; j++){
                noveltyCurve[i] += spectrogram[i][j];
            }
            mean += noveltyCurve[i] / noveltyCurve.length;
        }
        return mean;
    }


    // Thresholds the novelty curve by thresholding everything below local median
    // Has a window that slides across the novelty curve 
    // Constantly calculates a local median and thresholds it
    public static double[] threshold(double[] noveltyCurve, int arrSize){
        double[] medianArray = new double[arrSize];
        double[] threshold = new double[noveltyCurve.length];

        for(int i = 0; i < arrSize; i++){
            medianArray[i] = noveltyCurve[i];
        }

        Arrays.sort(medianArray);
        // Adds the first element with the median
        for(int i = 0; i < arrSize / 2; i++){
            threshold[i] = medianArray[arrSize / 2];
        }

        for(int i = 0; i < noveltyCurve.length - arrSize; i++){
            for(int j = 0; j < arrSize; j++){
                medianArray[j] = noveltyCurve[i + j];
            }
            Arrays.sort(medianArray);
            threshold[i + arrSize/2] = medianArray[2*arrSize/3];
        }

        for(int i = 0; i < noveltyCurve.length; i++){
            noveltyCurve[i] = Math.max(noveltyCurve[i] - threshold[i], 0);
        }

        return noveltyCurve;
    }

    // Constructs a gaussian kernel to be used for the smoothing
    // Gaussian kernel weighs
    // degree of smoothing decided by sigma
    private static double[] gaussianKernel(int size, double sigma) {
        double[] kernel = new double[size];
        double sum = 0.0;
        int mid = size / 2;
        
        for (int i = 0; i < size; i++) {
            kernel[i] = Math.exp(-0.5 * Math.pow((i - mid) / sigma, 2));
            sum += kernel[i];
        }
        
        // Normalize the kernel
        for (int i = 0; i < size; i++) {
            kernel[i] /= sum;
        }
        
        return kernel;
    }
    
    // Applies the gaussian smooth throughout the entire novelty curve
    // Uses the kernel that is constructed
    // Strength of smoothing decided by sigma
    public static double[] gaussianSmooth(double[] noveltyCurve, int kernelSize, double sigma) {
        double[] kernel = gaussianKernel(kernelSize, sigma);
        
        for (int i = 0; i < noveltyCurve.length; i++) {
            double sum = 0.0;
            for (int j = 0; j < kernelSize; j++) {
                int noveltyCurveIndex = i - (kernelSize / 2) + j;
                // Prevents index out of bounds
                if (noveltyCurveIndex >= 0 && noveltyCurveIndex < noveltyCurve.length) {
                    sum += noveltyCurve[noveltyCurveIndex] * kernel[j];
                }
            }
            noveltyCurve[i] = sum;
        }   
        return noveltyCurve;
    }

    // Resamples the
    public static double[] resampleNoveltyCurve(double[] noveltyCurve, int targetSampleRate, double songDuration){
        double currentSamplerate = noveltyCurve.length / songDuration;
        double resamplingFactor = targetSampleRate / currentSamplerate;
        double[] timeIndices = new double[noveltyCurve.length];
        for (int i = 0; i < timeIndices.length; i++) {
            timeIndices[i] = i * resamplingFactor;
        }
    

        SplineInterpolator interpolator = new SplineInterpolator();
        PolynomialSplineFunction splineFunction = interpolator.interpolate(timeIndices, noveltyCurve);
    
        double[] resampledNoveltyCurve = new double[(int)((noveltyCurve.length - 1) * resamplingFactor )];
        for (int i = 0; i < resampledNoveltyCurve.length; i++) {
            resampledNoveltyCurve[i] = Math.max(0, splineFunction.value(i));
        }
        return resampledNoveltyCurve;
    }


    
}
