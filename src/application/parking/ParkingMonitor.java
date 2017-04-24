package application.parking;

import java.awt.Rectangle;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Iterator;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ParkingMonitor implements Runnable {

	boolean running = false; // Variable that states whether or not the monitor
								// is running
	VideoCapture camera;// Holds the VideoCapture device(the camera)

	// ImageView References for camera output
	ImageView objectImageView = null, defaultImageView = null, thresholdImageView = null, spacesImageView = null;

	// Matrices to hold the raw image data
	Mat defaultImage, thresholdImage, objectsImage, spacesImage, objectsThresImage;

	SpaceManager spaceMngr;

	// Constructs a new parking monitor
	public ParkingMonitor(SpaceManager sm) {
		spaceMngr = sm;
		camera = new VideoCapture();
		defaultImage = new Mat();
		thresholdImage = new Mat();
		objectsImage = new Mat();
		spacesImage = new Mat();
		objectsThresImage = new Mat();
	}

	// Main Loop of monitor to be executed as a thread
	@Override
	public void run() {

		camera.open(MonitorSettings.camIndex);
		if (camera.isOpened()) {
			running = true;
			// Repeat while running
			do {
				// Read frame from camera
				camera.read(defaultImage);

				// TODO Apply Smoothing Filters

				// Threshold image
				thresholdFrame();

				// Detect Objects
				detectObjects();

				// TODO Update Parking Information
				checkSpaces();
				drawSpaces();
				// Output images to ImageViews
				updateImageViews();
			} while (running);
			camera.release();
		} else {
			// TODO Could not open camera
		}

	}

	/**
	 * Check if spaces are occupied or not
	 */
	private void checkSpaces() {
		byte[] data = new byte[objectsThresImage.width() * objectsThresImage.height()];
		objectsThresImage.get(0, 0, data);
		for (Space s : spaceMngr.getSpaces()) {
			Rectangle bounds = s.getBounds();
			int areaOccupied = 0;

			for (int y = (int) bounds.getMinY(); y < bounds.getMaxY(); y++) {
				for (int x = (int) bounds.getMinX(); x < bounds.getMaxX(); x++) {
					if (s.isPointInSpace(x, y)) {
						if (data[(y * objectsThresImage.width()) + x] == -1) {
							areaOccupied++;
						}
					}
				}
			}

			if (areaOccupied >= (s.getArea() * MonitorSettings.occupiedPercentage)) {
				s.setOccupied(true);
			} else {
				s.setOccupied(false);
			}
		}
	}

	/**
	 * This method draws the parking spaces from the SpaceManager onto the
	 * spaces view image.
	 */
	private void drawSpaces() {
		defaultImage.copyTo(spacesImage);
		// Imgproc.cvtColor(spacesImage, spacesImage, Imgproc.COLOR_GRAY2BGR);
		System.out.println("Drawing");
		for (Space s : spaceMngr.getSpaces()) {
			ArrayList<MatOfPoint> points = new ArrayList<>();
			Point[] p = new Point[4];
			int[] xpoints = s.getPolygon().xpoints;
			int[] ypoints = s.getPolygon().ypoints;

			for (int i = 0; i < 4; i++) {
				p[i] = new Point(xpoints[i], ypoints[i]);
			}
			Scalar colour = (s.isOccupied()) ? new Scalar(0, 0, 255) : new Scalar(0, 255, 0);
			points.add(new MatOfPoint(p));
			Imgproc.fillPoly(spacesImage, points, colour);
			// Calculate size of text
			Size textSize = Imgproc.getTextSize(s.getName(), Core.FONT_HERSHEY_COMPLEX, 1.0, 1, null);
			// Calculate point to draw text centered
			Rectangle bounds = s.getPolygon().getBounds();
			Point centerPoint = new Point(((bounds.getWidth() / 2) + bounds.getX()) - (textSize.width / 2),
					((bounds.getHeight() / 2) + bounds.getY()) - (textSize.height / 2));
			System.err.println(s.getName());
			Imgproc.putText(spacesImage, s.getName(), centerPoint, Core.FONT_HERSHEY_COMPLEX, 1.0, new Scalar(0, 0, 0));
		}
	}

	private void detectObjects() {
		// copy the default image
		defaultImage.copyTo(objectsImage);
		thresholdImage.copyTo(objectsThresImage);
		objectsThresImage.setTo(new Scalar(0));

		// Mat to store heirachy info
		Mat heirachyFrame = new Mat();

		// Copy of thresholded binary image
		Mat tempThresh = new Mat();
		thresholdImage.copyTo(tempThresh);

		ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		ArrayList<MatOfPoint> objects = new ArrayList<MatOfPoint>();

		// Find contours from threshold image
		Imgproc.findContours(tempThresh, contours, heirachyFrame, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
		if (contours.size() > 0) {
			// Convert heirarchy info from mat to int[]
			int[] heirarchyData = new int[heirachyFrame.width() * heirachyFrame.channels()];
			heirachyFrame.get(0, 0, heirarchyData);

			// Loop through all contours
			for (int c = 0; c < contours.size(); c++) {
				MatOfPoint point = (MatOfPoint) contours.get(c);// Get current
																// contour
				boolean ignoreContour;

				// if drop childless set ignoreContour flag to true if not a
				// parent
				if (MonitorSettings.dropChildless) {
					ignoreContour = heirarchyData[(4 * c) + 2] == -1;
				} else {// otherwise set it to false
					ignoreContour = false;
				}

				// Create a rotated recctangle surronding the contour
				MatOfPoint2f pointf = new MatOfPoint2f(point.toArray());
				RotatedRect r = Imgproc.minAreaRect(pointf);

				// if size or width is less than 5 then set ignoreContour flag
				// to true(Filters out some noise)
				if (r.size.width <= 5 || r.size.height <= 5) {
					ignoreContour = true;
				}

				// If ignoreContour flag is not set
				if (!ignoreContour) {
					// Convert the rectangle to points
					Point points[] = new Point[4];
					r.points(points);

					// Add to objects
					objects.add(point);
					Imgproc.fillConvexPoly(objectsThresImage, point, new Scalar(255));
					// Draw the rectangles lines to the objects image
					for (int i = 0; i < 4; i++)
						// Draw objects
						Imgproc.line(objectsImage, points[i], points[(i + 1) % 4], new Scalar(0, 255, 0), 5);
				}
			}
			// Imgproc.fillPoly(objectsThresImage, objects, new Scalar(255));
		}
	}

	// Outputs the images to the imageviews
	/**
	 * Updates the imageviews (set by setOutputImages()) to the image data
	 * captured by the camera
	 */
	private void updateImageViews() {
		// Output Default Image
		if (defaultImageView != null) {
			defaultImageView.setImage(convertMatToImage(defaultImage));
		}
		// Output Threshold Image
		if (thresholdImageView != null) {
			if (MonitorSettings.objectThresholdView) {
				thresholdImageView.setImage(convertMatToImage(objectsThresImage));
			} else {
				thresholdImageView.setImage(convertMatToImage(thresholdImage));
			}
		}
		// Output Objects Image
		if (objectImageView != null) {
				objectImageView.setImage(convertMatToImage(objectsImage));
			
		}
		// Output Spaces View
		if (spacesImageView != null) {
			spacesImageView.setImage(convertMatToImage(spacesImage));
		}
	}

	/**
	 * Converts the raw image data to a png image.
	 * 
	 * @param mat
	 * @return Image
	 */
	private Image convertMatToImage(Mat mat) {
		MatOfByte buffer = new MatOfByte();
		Imgcodecs.imencode(".png", mat, buffer);
		return new Image(new ByteArrayInputStream(buffer.toArray()));
	}

	/**
	 * Applies adaptive thresholding to the image to get a binary image. Also
	 * performs eroding and dilating to the binary image as needed.
	 */
	private void thresholdFrame() {
		Mat defaultGreyImage = new Mat();
		Imgproc.cvtColor(defaultImage, defaultGreyImage, Imgproc.COLOR_BGR2GRAY);
		Imgproc.adaptiveThreshold(defaultGreyImage, thresholdImage, 255, MonitorSettings.thresholdingType,
				Imgproc.THRESH_BINARY_INV, MonitorSettings.thresholdingBlockSize, MonitorSettings.thresholdingChange);
		if (MonitorSettings.erodingEnabled) {
			Imgproc.erode(thresholdImage, thresholdImage,
					Imgproc.getStructuringElement(MonitorSettings.erodingShape,
							new Size(MonitorSettings.erodingSize, MonitorSettings.erodingSize)),
					new Point(-1, -1), MonitorSettings.erodingIterations);
		}
		if (MonitorSettings.dilatingEnabled) {
			Imgproc.dilate(thresholdImage, thresholdImage,
					Imgproc.getStructuringElement(MonitorSettings.dilatingShape,
							new Size(MonitorSettings.dilatingSize, MonitorSettings.dilatingSize)),
					new Point(-1, -1), MonitorSettings.dilatingIterations);
		}
	}

	/**
	 * Returns whether the parking monitor is currently running
	 * 
	 * @return boolean
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * Set the Imageviews that the default,threshold and object images will be
	 * output too. If an ImageView is set to null they're will be know output
	 * for that image.
	 * 
	 * @param defaultImage
	 * @param thresholdImage
	 * @param objectImage
	 * @Param spacesImage
	 */
	public void setOutputImages(ImageView defaultImage, ImageView thresholdImage, ImageView objectImage,
			ImageView spacesImage) {
		this.defaultImageView = defaultImage;
		this.thresholdImageView = thresholdImage;
		this.objectImageView = objectImage;
		this.spacesImageView = spacesImage;
	}

	/**
	 * Stops the parking monitor
	 */
	public void stop() {
		running = false;
	}

}
