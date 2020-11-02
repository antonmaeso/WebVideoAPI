package opencv;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opencv.videoio.VideoCapture;


public class CameraOperations {
	
	public Boolean startStopCamera(Boolean cameraActive, VideoCapture capture, ScheduledExecutorService timer) {
		if (!cameraActive) {
			capture.open(0);
			return true;
		} else {
			this.stopAcquisition(capture, timer);
			return false;
		}
	}

	
	public void stopAcquisition(VideoCapture capture, ScheduledExecutorService timer) {
		if (timer != null && !timer.isShutdown()) {
			try {
				timer.shutdown();
				timer.awaitTermination(33, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
			}
		}

		if (capture.isOpened()) {
			capture.release();
		}
	}

}
