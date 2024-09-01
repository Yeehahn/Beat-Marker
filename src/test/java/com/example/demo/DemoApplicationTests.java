package com.example.demo;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.tritonus.share.sampled.file.TAudioFileFormat;
import java.io.*;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFrame;

import com.example.demo.domain.FileHolder;
import com.example.demo.onset.FFT.Window.HannWindowFunction;
import com.example.demo.onset.onsetDetection.SpectralFluxOnsetDetector;

import java.util.*;

@SpringBootTest
class DemoApplicationTests {

	@Test
	void contextLoads() {
	}

}
