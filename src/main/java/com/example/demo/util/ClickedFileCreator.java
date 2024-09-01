package com.example.demo.util;

import java.io.File;
import java.util.List;


// Takes all of the information gathered through the PLPCurve
// Creates a file with the designated filePath where every detected onset
// has a pop sound effect

public class ClickedFileCreator {

    // Takes the file that the user sent, the fileLengthTime, 
    // and the found times where an onset occurs
    // Goes to each of those times and overlaps the pop sound effect at that time
    // Then encodes the PCM data to MP3 data and stores the file
    public static void createClickedFile(File mp3File, double fileLengthTime, List<Double> peakTimesSeconds, String filePath) {
        try{
            double[] mp3FileSignal = FileProcessor.toDoubleArray(FileProcessor.getMp3ByteArray(mp3File));
            double fileLengthToTimeRatio = mp3FileSignal.length / fileLengthTime;
            File clickSoundEffect = new File("src\\main\\resources\\templates\\happy-pop-3-185288.mp3");
            double[] clickFileSignal = FileProcessor.toDoubleArray(FileProcessor.getMp3ByteArray(clickSoundEffect));

            for(int i = 0; i < peakTimesSeconds.size(); i++){
                int index = (int)(peakTimesSeconds.get(i) * fileLengthToTimeRatio);
                for(int j = 0; j < clickFileSignal.length 
                                && (j + index) < mp3FileSignal.length; j++){
                    mp3FileSignal[j + index] += clickFileSignal[j];
                }
            }
            FileProcessor.encodePCMToMP3(FileProcessor.toByteArray(mp3FileSignal), filePath);
        } catch(Exception e){
            e.printStackTrace();
        }
    }
}
