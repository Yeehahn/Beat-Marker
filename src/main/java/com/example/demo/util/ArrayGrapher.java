package com.example.demo.util;

import java.awt.Color;
import java.awt.Dimension;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.tritonus.share.sampled.file.TAudioFileFormat;

import com.example.demo.onset.onsetDetection.SpectralFluxOnsetDetector;
// Class made by Chat GPT
public class ArrayGrapher extends JFrame {
    public ArrayGrapher(double[] data, String xAx, String yAx, double xAxCoef) {
        super("Array Graph");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Create a dataset containing the array data
        XYSeries series = new XYSeries("Data");
        for (int i = 0; i < data.length; i++) {
            series.add(i * xAxCoef, data[i]);
        }
        XYSeriesCollection dataset = new XYSeriesCollection(series);
        
        // Create the chart
        JFreeChart chart = ChartFactory.createXYLineChart(
            "Array Graph",    // Chart title
            xAx,          // X-axis label
            yAx,          // Y-axis label
            dataset,          // Dataset
            PlotOrientation.VERTICAL,  // Plot orientation
            true,             // Include legend
            true,             // Include tooltips
            false 
        );
        
        // Customize the chart (optional)
        chart.setBackgroundPaint(Color.white);
        
        // Create and customize the panel that will hold the chart
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(800, 600));
        
        // Add the chart panel to the frame
        getContentPane().add(chartPanel);
        

        pack();
        setLocationRelativeTo(null); 
        setVisible(true);
    }

    public static void main(String[] args) throws IOException, UnsupportedAudioFileException{
        try{
            File file = new File("src\\test\\java\\com\\example\\demo\\The White Stripes - Seven Nation Army (Audio).mp3");
            AudioInputStream baseInput = AudioSystem.getAudioInputStream(file);
            AudioFormat baseFormat = baseInput.getFormat();
            AudioFormat decodedFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                baseFormat.getSampleRate(),
                16,
                baseFormat.getChannels(),
                baseFormat.getChannels() * 2,
                baseFormat.getSampleRate(),
                false
            );
            AudioInputStream input = AudioSystem.getAudioInputStream(decodedFormat, baseInput);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            
            byte[] buffer = new byte[4096];
            int byteArrayRead; 
            while((byteArrayRead = input.read(buffer)) != -1){
                output.write(buffer, 0, byteArrayRead);
            }

            byte[] byteArray = output.toByteArray();
            double[] doubles = new double[byteArray.length / 2];
            for (int i = 0; i < doubles.length; i++) {
                doubles[i] = ((byteArray[2 * i + 1] << 8) | (byteArray[2 * i] & 0xFF)) / 32768.0;
            }
            AudioFileFormat fileFormat = AudioSystem.getAudioFileFormat(file);
            long mili = 0;
            try{
                Map<?, ?> properties = ((TAudioFileFormat) fileFormat).properties();
                String key = "duration";
                Long microseconds = (Long) properties.get(key);
                mili = (microseconds / 1000);
            } catch(Exception e) {
                throw new UnsupportedAudioFileException();
            }

            SpectralFluxOnsetDetector onsetDetection = new SpectralFluxOnsetDetector(doubles, 2048, 4, (int)mili, 10.0, 5.0);
            ClickedFileCreator.createClickedFile(file, mili / 1000.0, onsetDetection.getPeakTimes(), "src\\main\\resources\\templates\\tempFiles\\clickedFile.mp3");
            // mili / ((double)onsetDetection.getNoveltyCurve().length * 1000)
            new ArrayGrapher(onsetDetection.getNoveltyCurve(), "time", "amplitude", mili / ((double)onsetDetection.getNoveltyCurve().length * 1000));
            new ArrayGrapher(onsetDetection.getplpCurve(), "time", "amplitude", mili / ((double)onsetDetection.getplpCurve().length * 1000));
            System.out.println(onsetDetection.getMedianBPM());
        } catch(Exception e){
            e.printStackTrace();
        }
    }

}