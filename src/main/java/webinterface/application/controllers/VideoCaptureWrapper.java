package webinterface.application.controllers;

import org.opencv.videoio.VideoCapture;

public class VideoCaptureWrapper {
	
	private VideoCapture videoCapture;
	
	public VideoCapture getVideoCapture() {
		if(this.videoCapture == null) {
			videoCapture = new VideoCapture();
		}
		
		return this.videoCapture;
		
	}

}
