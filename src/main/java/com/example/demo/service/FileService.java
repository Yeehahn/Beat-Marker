package com.example.demo.service;

import java.io.*;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;


import com.example.demo.domain.FileHolder;

// Service layer between the controller and the fileHolder
// Converts the information stored in the fileHolder into
// Objects that the rest controller can send back to the client

@Service
public class FileService {
    
    private FileHolder fileHolder;

    public FileService(FileHolder fileHolder){
        this.fileHolder = fileHolder;
    }

    // Receives a file and creates a FileHolder object that holds the file and processes it
    // Then returns the file that the fileHolder processed from the MultipartFile
    public File processFile(MultipartFile file) throws UnsupportedAudioFileException, IOException{
        fileHolder.setAndProcessFile(file);
        return fileHolder.getFile();
    }
    
    // Gets the clickedFile from the fileHolder
    // Returns a UrlResource to the clickedFile.mp3 so that the <a> tag
    // Allows the user to download the file
    public Resource getClickedFile(){
        try{
        File clickedFile = fileHolder.getClickedFile();
        Resource clickedResource = new UrlResource(clickedFile.toURI());
        return clickedResource;
        } catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public String getBpmInformation(){
        return fileHolder.getBpmInformation();
    }

    public File getFile(){
        return fileHolder.getFile();
    }
}
