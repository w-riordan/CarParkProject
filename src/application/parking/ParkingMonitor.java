package application.parking;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Iterator;

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
	int camIndex = 0;// The index number of the camera to use(-1 opens the first
						// camera found)
	VideoCapture camera;// Holds the VideoCapture device(the camera)

	// ImageView References for camera output
	ImageView objectImageView = null, defaultImageView = null, thresholdImageView = null;

	// Matrices to hold the raw image data
	Mat defaultImage, thresholdImage, objectsImage;

	// Constructs a new parking monitor
	public ParkingMonitor(int camIndex) {
		this.camIndex = camIndex;
		camera = new VideoCapture();
		defaultImage = new Mat();
		thresholdImage = new Mat();
		objectsImage = new Mat();
	}

	// Main Loop of monitor to be executed as a thread
	@Override
	public void run() {

		camera.open(camIndex);
		if (camera.isOpened()) {
			running = true;
			// Repeat while running
			do {
				// Read frame from camera
				camera.read(defaultImage);
				
				//TODO Apply Smoothing Filters
				
				// Threshold image
				thresholdFrame();

				// Detect Objects
				detectObjects();

				//TODO Update Parking Information

				// Output images to ImageViews
				updateImageViews();
			} while (running);
			camera.release();
		} else {
			//TODO Could not open camera
		}

	}

	private void detectObjects() {
		//copy the default image
		defaultImage.copyTo(objectsImage);
		
		//Mat to store heirachy info
		Mat heirachyFrame = new Mat();
		
		//Copy of thresholded binary image
		Mat tempThresh = new Mat();
		thresholdImage.copyTo(tempThresh);
		
		ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		
		//Find contours from threshold image
		Imgproc.findContours(tempThresh, contours, heirachyFrame, Imgproc.RETR_TREE,
				Imgproc.CHAIN_APPROX_SIMPLE);
		
		//Convert heirarchy info from mat to int[]
		int[] heirarchyData =  new int[ heirachyFrame.width() * heirachyFrame.channels()];
		heirachyFrame.get(0, 0, heirarchyData);
		
		//Loop through all contours
		for (int c = 0; c < contours.size();c++) {
			MatOfPoint point = (MatOfPoint) contours.get(c);//Get current contour
			boolean ignoreContour;
			
			//if drop childless set ignoreContour flag to true if not a parent
			if (MonitorSettings.dropChildless){
				ignoreContour = heirarchyData[(4 * c)+ 2] == -1;
			}else{//otherwise set it to false
				ignoreContour = false;
			}

			//Create a rotated recctangle surronding the contour
			MatOfPoint2f pointf = new MatOfPoint2f(point.toArray());
			RotatedRect r = Imgproc.minAreaRect(pointf);
			
			//if size or width is less than 5 then set ignoreContour flag to true(Filters out some noise)
			if(r.size.width <= 5 || r.size.height <= 5){
				ignoreContour = true;
			}
			
			//If ignoreContour flag is not set
			if ( !ignoreContour) {
				//Convert the rectangle to points 
				Point points[] = new Point[4];
				r.points(points);

				//Draw the rectangles lines to the objects image 
				for (int i = 0; i < 4; i++)
					// Draw objects
					Imgproc.line(objectsImage, points[i], points[(i + 1) % 4], new Scalar(0, 255, 0),5);
			}
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
			thresholdImageView.setImage(convertMatToImage(thresholdImage));
		}
		// Output Objects Image
		if (objectImageView != null) {
			objectImageView.setImage(convertMatToImage(objectsImage));
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
		Imgproc.cvtColor(defaultImage, defaultImage, Imgproc.COLOR_BGR2GRAY);
		Imgproc.adaptiveThreshold(defaultImage, thresholdImage, 255, MonitorSettings.thresholdingType,
				Imgproc.THRESH_BINARY_INV, MonitorSettings.thresholdingBlockSize, MonitorSettings.thresholdingChange);
		if (MonitorSettings.erodingEnabled){
			Imgproc.erode(thresholdImage, thresholdImage, Imgproc.getStructuringElement(MonitorSettings.erodingShape,
					new Size(MonitorSettings.erodingSize, MonitorSettings.erodingSize)), new Point(-1, -1), MonitorSettings.erodingIterations);
		}
		if ( MonitorSettings.dilatingEnabled){
		Imgproc.dilate(thresholdImage, thresholdImage, Imgproc.getStructuringElement(MonitorSettings.dilatingShape,
				new Size(MonitorSettings.dilatingSize, MonitorSettings.dilatingSize)), new Point(-1, -1), MonitorSettings.dilatingIterations);
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
	 */
	public void setOutputImages(ImageView defaultImage, ImageView thresholdImage, ImageView objectImage) {
		this.defaultImageView = defaultImage;
		this.thresholdImageView = thresholdImage;
		this.objectImageView = objectImage;
	}

	/**
	 * Stops the parking monitor
	 */
	public void stop() {
		running = false;
	}

}
