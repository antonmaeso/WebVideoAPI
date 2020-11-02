package javafx.application;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import org.opencv.videoio.VideoCapture;


import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import opencv.CameraOperations;
import opencv.FaceTrakingHelper;
import opencv.utils.Utils;

/**
 * The controller associated with the only view of our application. The
 * starting/stopping the camera, the acquired video stream, the relative
 * controls and the face detection/tracking.
 * 
 * @author <a href="mailto:luigi.derussis@polito.it">Luigi De Russis</a>
 * @version 1.1 (2015-11-10)
 * @since 1.0 (2014-01-10)
 * 
 */
public class FaceDetectionController {
	// FXML buttons
	@FXML
	private Button cameraButton;
	// the FXML area for showing the current frame
	@FXML
	private ImageView originalFrame;
	// checkboxes for enabling/disabling a classifier

	// a timer for acquiring the video stream
	private ScheduledExecutorService timer;
	// the OpenCV object that performs the video capture
	private VideoCapture capture;
	// a flag to change the button behavior
	private boolean cameraActive;

	// face cascade classifier
	private CascadeClassifier faceCascade;
	private int absoluteFaceSize;

	/**
	 * Init the controller, at start time
	 */
	protected void init() {
		this.capture = new VideoCapture();
		this.faceCascade = new CascadeClassifier();
		this.absoluteFaceSize = 0;
		// set a fixed width for the frame
		originalFrame.setFitWidth(600);
		// preserve image ratio
		originalFrame.setPreserveRatio(true);
		this.checkboxSelection("resources/lbpcascades/lbpcascade_frontalface.xml");
		// now the video capture can start
		this.startCamera();
	}

	/**
	 * The action triggered by pushing the button on the GUI
	 */
	@FXML
	protected void startCamera() {
		this.cameraActive = new CameraOperations().startStopCamera(this.cameraActive, this.capture, this.timer);
		if(!this.cameraActive) {
			this.cameraButton.setText("Start Camera");
		} else {
			this.cameraButton.setText("Stop Camera");
		}	
		if (this.capture.isOpened()) {
			processFrame(this.timer);
		} else {
			System.err.println("Failed to open the camera connection...");
		}
	}

	private void processFrame(ScheduledExecutorService timer) {
		Runnable frameGrabber = getFrame(this.capture);
		timer = Executors.newSingleThreadScheduledExecutor();
		timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);
	}

	private Runnable getFrame(VideoCapture capture) {
		Runnable frameGrabber = new Runnable() {
			@Override
			public void run() {
				Mat frame = grabFrame(capture);
				Image imageToShow = Utils.mat2Image(frame);
				updateImageView(originalFrame, imageToShow);
			}
		};
		return frameGrabber;
	}

	/**
	 * Get a frame from the opened video stream (if any)
	 * 
	 * @return the {@link Image} to show
	 */
	private Mat grabFrame(VideoCapture capture) {
		Mat frame = new Mat();

		try {
			capture.read(frame);
			if (!frame.empty()) {
				this.detectAndDisplay(frame);
			}
		} catch (Exception e) {
			System.err.println("Exception during the image elaboration: " + e);
		}
		return frame;
	}

	/**
	 * Method for face detection and tracking
	 * 
	 * @param frame it looks for faces in this frame
	 */
	private void detectAndDisplay(Mat frame) {
		MatOfRect faces = new MatOfRect();
		Mat grayFrame = new Mat();
		Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
		//System.out.println("height: "+ frame.height() + " width: " + frame.width());
		Imgproc.equalizeHist(grayFrame, grayFrame);
		if (this.absoluteFaceSize == 0) {
			int height = grayFrame.rows();
			if (Math.round(height * 0.2f) > 0) {
				this.absoluteFaceSize = Math.round(height * 0.2f);
			}
		}
		
		this.faceCascade.detectMultiScale(grayFrame, faces, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE,
		new Size(this.absoluteFaceSize, this.absoluteFaceSize), new Size());

		Rect[] facesArray = faces.toArray();
		
		int distance = 10;
		for (int i = 0; i < facesArray.length; i++) {
            FaceTrakingHelper face = new FaceTrakingHelper(facesArray[i], frame);
            //System.out.println(face.centre().toString());

            Imgproc.rectangle(frame, face.centreFace(), face.centreFace(), new Scalar(0, 255, 0), 3);
//			Imgproc.rectangle(frame, face.centerOfFrame(), face.centerOfFrame(), new Scalar(100, 100, 100), 3);
//			Imgproc.rectangle(frame, face.pointDiff(new Point(100,100), face.centreDiff()), face.pointDiff(new Point(200,200),face.centreDiff()), new Scalar(100, 100, 100), 150);
			 
		}
		

	}

	/**
	 * Method for loading a classifier trained set from disk
	 * 
	 * @param classifierPath the path on disk where a classifier trained set is
	 *                       located
	 */
	private void checkboxSelection(String classifierPath) {
		// load the classifier(s)
		this.faceCascade.load(classifierPath);

		// now the video capture can start
		this.cameraButton.setDisable(false);
	}

	/**
	 * Stop the acquisition from the camera and release all the resources
	 */
	private void stopAcquisition() {
		if (this.timer != null && !this.timer.isShutdown()) {
			try {
				// stop the timer
				this.timer.shutdown();
				this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				// log any exception
				System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
			}
		}

		if (this.capture.isOpened()) {
			// release the camera
			this.capture.release();
		}
	}

	/**
	 * Update the {@link ImageView} in the JavaFX main thread
	 * 
	 * @param view  the {@link ImageView} to update
	 * @param image the {@link Image} to show
	 */
	private void updateImageView(ImageView view, Image image) {
		Utils.onFXThread(view.imageProperty(), image);
	}

	/**
	 * On application close, stop the acquisition from the camera
	 */
	protected void setClosed() {
		this.stopAcquisition();
	}

}
