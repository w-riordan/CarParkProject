package application;

import org.opencv.imgproc.Imgproc;

import application.parking.MonitorSettings;
import application.parking.ParkingMonitor;
import application.parking.Space;
import application.parking.SpaceManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public class ViewController {

	static ParkingMonitor monitor = null;
	static SpaceManager spaceMgr = null;

	// Define ImageViews from View
	@FXML
	ImageView objectImageView, thresholdImageView, defaultImageView, spacesImageView;
	// Define Buttons from View
	@FXML
	Button startButton, stopButton;
	// Define radioButtons from view
	@FXML
	RadioButton threshGausianRadio, threshMeanRadio, erodeCrossRadio, erodeRectRadio, erodeEllipseRadio,
			dilateCrossRadio, dilateRectRadio, dilateEllipseRadio;
	@FXML
	Slider threshBlockSlider, threshChangeSlider, erodeSizeSlider, erodeIterSlider, dilateSizeSlider, dilateIterSlider;
	@FXML
	CheckBox erodingCheck, dilatingCheck, dropCheck;

	// Called when window is created
	@FXML
	void initialize() {
		// Set up Slider listeners
		threshBlockSlider.valueProperty().addListener((ov, old_val, new_val) -> setThreshBlockSize(new_val.intValue()));
		threshChangeSlider.valueProperty()
				.addListener((ov, old_val, new_val) -> setThreshChange(new_val.doubleValue()));
		erodeSizeSlider.valueProperty().addListener((ov, old_val, new_val) -> setErodeSize(new_val.intValue()));
		erodeIterSlider.valueProperty().addListener((ov, old_val, new_val) -> setErodeIterations(new_val.intValue()));
		dilateSizeSlider.valueProperty().addListener((ov, old_val, new_val) -> setDilateSize(new_val.intValue()));
		dilateIterSlider.valueProperty().addListener((ov, old_val, new_val) -> setDilateIterations(new_val.intValue()));
		
		spacesImageView.setOnMouseClicked( e -> spacesClicked(e));
		
		// Create parking monitor
		spaceMgr = new SpaceManager();
		monitor = new ParkingMonitor(spaceMgr);
		monitor.setOutputImages(defaultImageView, thresholdImageView, objectImageView, spacesImageView);
	}


	// Starts the ParkingMonitor
	@FXML
	void startMonitor() {

		// Make sure parking monitor isn't running already
		if (!monitor.isRunning()) {
			new Thread(monitor).start();
		}

	}

	// Stops the Parkingmonitor
	@FXML
	void stopMonitor() {
		monitor.stop();
	}

	@FXML
	void setThreshMethod() {
		if (threshGausianRadio.isSelected()) {
			MonitorSettings.thresholdingType = Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C;
		} else if (threshMeanRadio.isSelected()) {
			MonitorSettings.thresholdingType = Imgproc.ADAPTIVE_THRESH_MEAN_C;
		}
	}

	void setThreshBlockSize(int value) {
		value = (value % 2 == 0) ? --value : value;
		value = (value < 3) ? 3 : value;
		MonitorSettings.thresholdingBlockSize = value;
	}

	void setThreshChange(double value) {
		MonitorSettings.thresholdingChange = value;
	}

	@FXML
	void setErodingEnabled() {
		MonitorSettings.erodingEnabled = erodingCheck.isSelected();
		System.err.println("Changed Eroding Setting");
	}

	@FXML
	void setErodingShape() {
		if (erodeCrossRadio.isSelected()) {
			MonitorSettings.erodingShape = Imgproc.CV_SHAPE_CROSS;
		} else if (erodeRectRadio.isSelected()) {
			MonitorSettings.erodingShape = Imgproc.CV_SHAPE_RECT;
		} else if (erodeEllipseRadio.isSelected()) {
			MonitorSettings.erodingShape = Imgproc.CV_SHAPE_ELLIPSE;
		}
	}

	void setErodeSize(int value) {
		value = (value % 2 == 0) ? --value : value;
		value = (value < 3) ? 3 : value;
		MonitorSettings.erodingSize = value;
	}

	void setErodeIterations(int value) {
		MonitorSettings.erodingIterations = value;
	}

	@FXML
	void setDropCheck() {
		MonitorSettings.dropChildless = dropCheck.isSelected();
	}

	@FXML
	void setDilatingEnabled() {
		MonitorSettings.dilatingEnabled = dilatingCheck.isSelected();
		System.err.println("Changed Dilating Setting");
	}

	@FXML
	void setDilatingShape() {
		if (dilateCrossRadio.isSelected()) {
			MonitorSettings.dilatingShape = Imgproc.CV_SHAPE_CROSS;
		} else if (dilateRectRadio.isSelected()) {
			MonitorSettings.dilatingShape = Imgproc.CV_SHAPE_RECT;
		} else if (dilateEllipseRadio.isSelected()) {
			MonitorSettings.dilatingShape = Imgproc.CV_SHAPE_ELLIPSE;
		}
	}

	void setDilateSize(int value) {
		value = (value % 2 == 0) ? --value : value;
		value = (value < 3) ? 3 : value;
		MonitorSettings.dilatingSize = value;
	}

	void setDilateIterations(int value) {
		MonitorSettings.dilatingIterations = value;
	}
	
	/**
	 * This method handles MouseEvents on the spaces image.
	 * Left Click to add points/Spaces 
	 * Right Click to remove Spaces
	 * @param e - The MouseEvent that occured
	 */
	private void spacesClicked(MouseEvent e) {
		double wratio=spacesImageView.getImage().getWidth()/spacesImageView.getFitWidth();
		double hratio=spacesImageView.getImage().getHeight()/spacesImageView.getFitHeight();
		//TODO USE CORRECT RATIO BASE ON CAMERA TYPE(LANSCAPE OR PORTRAIT)
		double xCord =e.getX()*wratio;
		double yCord =e.getY()*wratio;
		if(e.getButton()== MouseButton.PRIMARY){
			//Add point for new Space
			spaceMgr.addPoint((int)(xCord), (int)(yCord));
		}else if (e.getButton()==MouseButton.SECONDARY){
			//Test spaces to find one to remove
			for (Space s: spaceMgr.getSpaces()){
				if (s.isPointInSpace((int)xCord, (int)yCord)){
					spaceMgr.deleteSpace(s);
					break;
				}
			}
		}
	}
}
