package com.example.demo.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.Map;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.AudioFileFormat;

import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.tritonus.share.sampled.file.TAudioFileFormat;


// This object performs the typical processes necesarry
// to convert a file into various data forms and vice versa
// With the primary purpose being to get the corresponding
// Bytearray from a file
// Is also capable of finding the length in time of a file

public class FileProcessor {

    // Takes an AudioInputStream
    // Returns the corresponding byte array
    // Throws IOException if issues involving reading and writing
    // the streams occur
    public static byte[] getByteArrayFromInputStream(AudioInputStream input) throws IOException{
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int bytesRead; 
        while((bytesRead = input.read(buffer)) != -1){
            output.write(buffer, 0, bytesRead);
        }
        input.close();
        output.close();
        return output.toByteArray();
    }

    // From MP3Spi's documentation on how to get a byte array from an mp3 file
    // Takes a file and then returns an AudioInputStream where the data in
    // the stream is encoded in PCM
    // Throws unsupportedAudioFileException if the File isn't in a proper audio file
    // Throws IOException if issues occur involving reading the AudioInputStream
    public static AudioInputStream getAudioInputStreamFromFile(File file) throws UnsupportedAudioFileException, IOException{
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
        return AudioSystem.getAudioInputStream(decodedFormat, baseInput);
    }

    // Takes a file 
    public static byte[] getMp3ByteArray(File file) throws UnsupportedAudioFileException, IOException{
        AudioInputStream input = FileProcessor.getAudioInputStreamFromFile(file);
        return FileProcessor.getByteArrayFromInputStream(input);
    }

    // Converts the byteArray into a double array
    // returns said double array
    // Estimates the value using an algorithm that assumes 16 bit
    // 16 bit is the standard for mp3 files and I force it to be that way
    //  With the audio format
    public static double[] toDoubleArray(byte[] byteArray){
        double[] signal = new double[byteArray.length / 2];
        // Written by chatGPT
        for (int i = 0; i < signal.length; i++) {
            signal[i] = ((byteArray[2 * i + 1] << 8) | (byteArray[2 * i] & 0xFF)) / 32768.0;
        }
        return signal;
    }

    // Takes a doubleArray and converts it back into a byte array
    // Assumes that the double array was encoded in 16 bit depth
    // Returns the corresponding bytearray
    public static byte[] toByteArray(double[] doubleArray) {
        byte[] byteArray = new byte[doubleArray.length * 2];
        for (int i = 0; i < doubleArray.length; i++) {
            int intVal = (int) (doubleArray[i] * 32768.0);
            
            // Ensure the value is within the range of a 16-bit signed integer
            if (intVal > 32767) {
                intVal = 32767;
            } else if (intVal < -32768) {
                intVal = -32768;
            }
            
            // Written by chatGPT
            byteArray[2 * i] = (byte) (intVal & 0xFF);
            byteArray[2 * i + 1] = (byte) ((intVal >> 8) & 0xFF);
        }
        return byteArray;
    }

    // Takes a byte array PCM encoded and a location to encode the file
    // Then converts the PCM data into data suitable for MP3
    // Encodes and creates 
    public static String encodePCMToMP3(byte[] byteArray, String filePath) throws IOException, InterruptedException, UnsupportedAudioFileException{
        File tempPCMFile = new File("src\\main\\resources\\templates\\tempFiles\\temp.pcm");
        FileOutputStream output = new FileOutputStream(tempPCMFile);
        output.write(byteArray);
        output.close();

        File clickedFile = new File(filePath);
        File lame = new File("src\\main\\resources\\templates\\lib\\lame.exe");

        String lameFilePath = lame.getAbsolutePath();
        String tempPCMFilePath = tempPCMFile.getAbsolutePath();
        String clickedFilePath = clickedFile.getAbsolutePath();

        // LAME command, states that it is PCM encoded, declares sample rate as 44.1k,
        // Declares bit depth as 16 bit
        // Declares the PCM file to get the data from
        // Declares the clickedFile that it will encode information
        ProcessBuilder pb = new ProcessBuilder(lameFilePath, "-r", "-s", "44.1", 
                                                "--bitwidth", "16",
                                                tempPCMFilePath, clickedFilePath);
        Process process = pb.redirectOutput(Redirect.INHERIT)
                            .redirectError(Redirect.INHERIT)
                            .start();
        process.waitFor();

        tempPCMFile.delete();
        return filePath;
    }

    // Got this code from:
    // https://stackoverflow.com/questions/3046669/how-do-i-get-a-mp3-files-total-time-in-java
    // Takes a file and returns the length of the time in miliseconds
    // Throws UnsupportedAudioFileException if the file is in wrong format
    // Throws IOException if it can't get the AudioFileFormat from file
    public static int findFileLength(File file) throws UnsupportedAudioFileException, IOException{
        AudioFileFormat fileFormat = AudioSystem.getAudioFileFormat(file);
        try{
            Map<?, ?> properties = ((TAudioFileFormat) fileFormat).properties();
            String key = "duration";
            Long microseconds = (Long) properties.get(key);
            long mili = (microseconds / 1000);
            return (int)mili;
        } catch(Exception e) {
            throw new UnsupportedAudioFileException();
        }
    }    
}
