package com.example.demo.controllers;

import java.io.*;


import javax.sound.sampled.UnsupportedAudioFileException;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.service.FileService;

// The rest controller that interacts with the client side
// Is capable of returning:
// BPM and ClickedFile as an MP3
// Receives a file from the client and sends it to service layer

@RestController
public class FileController {
    private FileService fileService;

    public FileController(FileService fileService){
        this.fileService = fileService;

    }


    // Receives a file form the client side and processes it
    @RequestMapping(method = RequestMethod.POST, consumes = {
                    MediaType.MULTIPART_FORM_DATA_VALUE
                    })
    public File processFile(@RequestPart MultipartFile file) throws UnsupportedAudioFileException, IOException{
        System.out.println("Request received");
        fileService.processFile(file);
        return fileService.getFile();
    }

    // Returns the calculated BPM
    @GetMapping(path = "/bpm")
    public ResponseEntity<String> getFileBpm(){
        return new ResponseEntity<String>(fileService.getBpmInformation(), HttpStatus.OK);
    }

    // Returns the clicked through file
    @GetMapping(path = "/clickedFile")
    public ResponseEntity<Resource> getClickedFile(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("audio/mpeg"));
        return new ResponseEntity<Resource>(fileService.getClickedFile(), headers, HttpStatus.OK);
    }

    
}
