package opencv;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import org.opencv.videoio.VideoCapture;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import opencv.utils.Utils;

public class ImageProcessing {
	
	private int absoluteFaceSize = 0;
	private CascadeClassifier cascadeClassifier;
	private Point faceCoordinates;
	
	public Point getFaceCoordinates() {
		return faceCoordinates;
	}
	
	public ImageProcessing(CascadeClassifier cascadeClassifier) {
		this.cascadeClassifier = cascadeClassifier;
	}
	
	public void processFrame(ScheduledExecutorService timer, VideoCapture capture, ImageView originalFrame) {
		Runnable frameGrabber = getFrame(capture, originalFrame);
		timer = Executors.newSingleThreadScheduledExecutor();
		timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);
	}
	
	private Runnable getFrame(VideoCapture capture, ImageView originalFrame) {
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
	
	private void updateImageView(ImageView view, Image image) {
		Utils.onFXThread(view.imageProperty(), image);
	}

	private void detectAndDisplay(Mat frame) {
		MatOfRect faces = new MatOfRect();
		Mat grayFrame = new Mat();
		Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
		Imgproc.equalizeHist(grayFrame, grayFrame);
		if (this.absoluteFaceSize == 0) {
			int height = grayFrame.rows();
			if (Math.round(height * 0.2f) > 0) {
				this.absoluteFaceSize = Math.round(height * 0.2f);
			}
		}
		
		this.cascadeClassifier.detectMultiScale(grayFrame, faces, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE,
		new Size(this.absoluteFaceSize, this.absoluteFaceSize), new Size());

		Rect[] facesArray = faces.toArray();
		
		for (int i = 0; i < facesArray.length; i++) {
			FaceTrakingHelper face = new FaceTrakingHelper(facesArray[i], frame);
			this.faceCoordinates = face.centreDiff();

//			Imgproc.rectangle(frame, face.centreFace(), face.centreFace(), new Scalar(0, 255, 0), 3);
//			Imgproc.rectangle(frame, face.centerOfFrame(), face.centerOfFrame(), new Scalar(100, 100, 100), 3);
//			Imgproc.rectangle(frame, face.pointDiff(new Point(100,100), face.centreDiff()), face.pointDiff(new Point(200,200),face.centreDiff()), new Scalar(100, 100, 100), 150);
			 
		}
		
		
		
	}

}
