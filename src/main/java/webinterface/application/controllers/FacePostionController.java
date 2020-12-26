
package webinterface.application.controllers;

import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;

import javax.annotation.PostConstruct;

import org.opencv.core.Point;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javafx.scene.image.ImageView;
import opencv.CameraOperations;
import opencv.ImageProcessing;

@Controller
public class FacePostionController {
	
	@Autowired
	SimpMessagingTemplate template;
	private ScheduledExecutorService timer;
	private VideoCapture capture;
	private CascadeClassifier faceCascade;
	private boolean cameraActive;
	private ImageView originalFrame;
	private ImageProcessing imageProcessing;
	
	@PostConstruct
	public void init() {
		this.capture = new VideoCaptureWrapper().getVideoCapture();
		this.faceCascade = new CascadeClassifier();
		this.faceCascade.load("resources/lbpcascades/lbpcascade_frontalface.xml");
		this.imageProcessing = new ImageProcessing(faceCascade);
		this.cameraActive = false;
	
	}

	@Scheduled(fixedDelay = 200L)
	public void greeting() {
		if (this.capture.isOpened()) {
			imageProcessing.processFrame(this.timer, capture, originalFrame);
			Point headPositioning = imageProcessing.getFaceCoordinates();
			template.convertAndSend("/headposition",headPositioning);
		} else {
			System.err.println("Failed to open the camera connection...");
		}
		
	}
	
	@GetMapping("/startCamera")
	public boolean startCamera() {
		this.cameraActive = new CameraOperations().startStopCamera(this.cameraActive, this.capture, this.timer);
		return this.cameraActive ;
	}

}
