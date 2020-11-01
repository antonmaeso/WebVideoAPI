package cv;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;

/**
 * @author anton
 *
 */
public class FaceTrakingHelper {
	private Rect _rect;
	private Mat _frame;
	
	public FaceTrakingHelper(Rect rect, Mat frame) {
		this._rect = rect;
		this._frame = frame;
	}

	public Point centreFace() {
		return new Point(_rect.x + (_rect.width/2), _rect.y + (_rect.height/2));	
	}
	
	public Point centreDiff() {
		return this.pointDiff(this.centreFace(), this.centerOfFrame());
	}
	
	public Point centerOfFrame() {
		return new Point(_frame.width()/2, _frame.height()/2);
	}
	
	public Point pointDiff(Point point1, Point point2) {
		return new Point((point1.x - point2.x), (point1.y - point2.y));
	}
	
	
}
