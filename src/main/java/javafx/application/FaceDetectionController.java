package javafx.application;

import java.util.concurrent.ScheduledExecutorService;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import opencv.CameraOperations;
import opencv.ImageProcessing;

public class FaceDetectionController {
	@FXML
	private Button cameraButton;
	@FXML
	private ImageView originalFrame;
	private ScheduledExecutorService timer;
	private VideoCapture capture;
	private CascadeClassifier faceCascade;
	private boolean cameraActive;

	protected void init() {
		this.capture = new VideoCapture();
		this.faceCascade = new CascadeClassifier();
		// set a fixed width for the frame
		originalFrame.setFitWidth(600);
		// preserve image ratio
		originalFrame.setPreserveRatio(true);
		this.faceCascade.load("resources/lbpcascades/lbpcascade_frontalface.xml");
		// now the video capture can start
		this.startCamera();
	}

	@FXML
	protected void startCamera() {
		this.cameraActive = new CameraOperations().startStopCamera(this.cameraActive, this.capture, this.timer);
		if(!cameraActive) {
			this.cameraButton.setText("Start Camera");
		} else {
			this.cameraButton.setText("Stop Camera");
		}	
		if (this.capture.isOpened()) {
			ImageProcessing imageProcessing = new ImageProcessing(faceCascade);
			imageProcessing.processFrame(this.timer, capture, originalFrame);
		} else {
			System.err.println("Failed to open the camera connection...");
		}
	}

	protected void setClosed() {
		new CameraOperations().stopAcquisition(this.capture, this.timer);
	}

}
