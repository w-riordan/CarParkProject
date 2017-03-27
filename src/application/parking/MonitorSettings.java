package application.parking;

import org.opencv.imgproc.Imgproc;

public class MonitorSettings {

	/**
	 * Should be set to either ADAPTIVE_THRESH_MEAN_C or ADAPTIVE_THRESH_GAUSSIAN_C from the Imgproc class
	 */
	public static int thresholdingType = Imgproc.ADAPTIVE_THRESH_MEAN_C;
	
	/**
	 * Should be set to integer that is odd and is greater or equal to 3
	 */
	public static int thresholdingBlockSize = 9;
	
	/**
	 * This is a double that represents the difference required to detect an edge in adaptive thresholding
	 */
	public static double thresholdingChange = 0.0;
	
	/**
	 * Whether or not eroding is enabled
	 */
	public static boolean erodingEnabled = true;
	/**
	 * Defines the shape used for eroding, should be CV_SHAPE_CROSS,CV_SHAPE_ELLIPSE,CV_SHAPE_RECT
	 */
	public static int erodingShape = Imgproc.CV_SHAPE_ELLIPSE;
	
	/**
	 * This is the size of the eroding shape
	 */
	public static int erodingSize = 3;
	
	/**
	 * The number of times to erode the image
	 */
	public static int erodingIterations = 1;
	
	/**
	 * Whether or not dilating is enabled
	 */
	public static boolean dilatingEnabled = true;
	/**
	 * Defines the shape used for dilating, should be CV_SHAPE_CROSS,CV_SHAPE_ELLIPSE,CV_SHAPE_RECT
	 */
	public static int dilatingShape = Imgproc.CV_SHAPE_ELLIPSE;
	
	/**
	 * This is the size of the dilating shape
	 */
	public static int dilatingSize = 3;
	
	/**
	 * The number of times to dilate the image
	 */
	public static int dilatingIterations = 1;
	
	/**TEMP**/
	public static boolean dropChildless = true;
}
