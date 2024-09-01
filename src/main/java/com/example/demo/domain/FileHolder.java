package com.example.demo.domain;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.onset.onsetDetection.SpectralFluxOnsetDetector;
import com.example.demo.util.ClickedFileCreator;
import com.example.demo.util.FileProcessor;

import lombok.Data;
import lombok.NoArgsConstructor;

// Takes the MultipartFile from the client side and
// Converts it into a proper file to be processed later
// Stores the bpm, file, and a clicked version of the file
// Initiates the onset detection of the file so that it can hold all of the
// Information of the file


@Data
@NoArgsConstructor
public class FileHolder {
    private File file;
    private String bpmInformation;
    private int fileLength;
    private File clickedFile;

    // Given a file, sets the file field with given file
    // Then processes the file and then stores the information in bpmInformation
    public void setAndProcessFile(MultipartFile tempFile) throws UnsupportedAudioFileException, IOException{
        this.file = decodeFile(tempFile);
        this.fileLength = FileProcessor.findFileLength(file);
        this.bpmInformation = getFileInformation(file);

    }

    // Takes a file and decodes it into its raw data
    // Then processes it so that it finds the BPM
    // Then constructs a clicked file on all beats
    // Throws IOException, UnsupportedAudioFileException if frame size is wrong
    // Or is provided incorrect file
    private String getFileInformation(File file) throws UnsupportedAudioFileException, IOException{
        byte[] mp3FileByteArray = FileProcessor.getMp3ByteArray(file);
        double[] soundSignal = FileProcessor.toDoubleArray(mp3FileByteArray);

        SpectralFluxOnsetDetector onsetDetector = new SpectralFluxOnsetDetector(
                                                        soundSignal, 
                                                        2048, 
                                                        4, 
                                                        fileLength, 
                                                 10.0, 
                                                  5.0); // lambda and sigma value subject to change

        List<Double> peakTimes = onsetDetector.getPeakTimes();
        String filePath = "src\\main\\resources\\templates\\tempFiles\\clickedFile.mp3";
        ClickedFileCreator.createClickedFile(file, fileLength / 1000.0, peakTimes, filePath);
        this.clickedFile = new File(filePath);

        String bpm = onsetDetector.getMedianBPM() + "";
 
        return bpm;

    }

    // Since JavaScript returns a fake path for security reasons
    // Need to decode in this manner where it reads all of 
    // the data in the fileItem received by Java
    // Receives a fileItem ultimately from the front end
    // Then reads all of the bytes and stores it into a "working" file 
    // Returns this working file for the AudioInputStream to read
    // Then all processing happens with this newly decoded file
    private File decodeFile(MultipartFile file) throws FileUploadException, IOException{
        try{
        InputStream inputStream = file.getInputStream();
        File workingFile = new File("src\\main\\resources\\templates\\tempFiles\\file.mp3");
        FileOutputStream outputStream = new FileOutputStream(workingFile);

        byte[] buffer = new byte[4096];
        int bytesRead;
        while((bytesRead = inputStream.read(buffer)) != -1){
            outputStream.write(buffer, 0, bytesRead);
        }

        inputStream.close();
        outputStream.close();

        return workingFile;

        } catch (FileUploadException e) {
            throw new FileUploadException("Failed to upload file", e);
        }
    }
}
