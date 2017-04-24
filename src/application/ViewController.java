package application;

import java.io.File;

import org.opencv.imgproc.Imgproc;

import application.parking.MonitorSettings;
import application.parking.ParkingMonitor;
import application.parking.Space;
import application.parking.SpaceManager;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

/**
 * The ViewController class handles user interaction from the View
 */
public class ViewController {

	// Hold a refrence to a ParkingMonitor
	static ParkingMonitor monitor = null;
	// Hold a refrence to a SpaceManager
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
	// Define Sliders from view
	@FXML
	Slider threshBlockSlider, threshChangeSlider, erodeSizeSlider, erodeIterSlider, dilateSizeSlider, dilateIterSlider;

	@FXML
	Slider ocupiedTimeSlider;

	@FXML
	Slider occupiedPercentageSlider;

	// Define Checkboxes from view
	@FXML
	CheckBox erodingCheck, dilatingCheck, dropCheck, thresholdViewCheck;

	// Define Textfield from view
	@FXML
	TextField camIndexTextField;

	/**
	 * The initialize method is called when the view is created and sets up the
	 * listeners for all the components. it also creates the SpaceManager and
	 * PArkingMonitor
	 */
	@FXML
	void initialize() {
		// Register change listener on camIndexTextField
		camIndexTextField.textProperty().addListener((ov, old_val, new_val) -> onCamIndexChange(ov, old_val, new_val));

		// Set up Slider listeners
		threshBlockSlider.valueProperty().addListener((ov, old_val, new_val) -> setThreshBlockSize(new_val.intValue()));
		threshChangeSlider.valueProperty()
				.addListener((ov, old_val, new_val) -> setThreshChange(new_val.doubleValue()));
		erodeSizeSlider.valueProperty().addListener((ov, old_val, new_val) -> setErodeSize(new_val.intValue()));
		erodeIterSlider.valueProperty().addListener((ov, old_val, new_val) -> setErodeIterations(new_val.intValue()));
		dilateSizeSlider.valueProperty().addListener((ov, old_val, new_val) -> setDilateSize(new_val.intValue()));
		dilateIterSlider.valueProperty().addListener((ov, old_val, new_val) -> setDilateIterations(new_val.intValue()));
		ocupiedTimeSlider.valueProperty().addListener((ov, old_val, new_val) -> setMinOccupiedTime(new_val.intValue()));
		occupiedPercentageSlider.valueProperty()
				.addListener((ov, old_val, new_val) -> setOccupiedPercentage(new_val.doubleValue()));

		// Register MouseClick Event handler on spacesImageView
		spacesImageView.setOnMouseClicked(e -> spacesClicked(e));

		// Refresh the components with the default settings
		refreshGuiComponents();

		// Create parking monitor
		spaceMgr = new SpaceManager();
		monitor = new ParkingMonitor(spaceMgr);
		monitor.setOutputImages(defaultImageView, thresholdImageView, objectImageView, spacesImageView);
	}

	/**
	 * This function is called when the user clicks the start button. It starts
	 * the Parkingmonitor.
	 */
	@FXML
	void startMonitor() {

		// Make sure parking monitor isn't running already
		if (!monitor.isRunning()) {
			// Start the parking monitor in a new thread
			new Thread(monitor).start();
			camIndexTextField.setEditable(false);
		}

	}

	/**
	 * This function is called when the user clicks the stop button or when the
	 * application is closed. It stops the parking monitor thread
	 */
	@FXML
	void stopMonitor() {
		monitor.stop();
		if (camIndexTextField != null) {
			camIndexTextField.setEditable(true);
		}
	}

	/**
	 * This function is called when the user changes the thresholding method. It
	 * updates the thresholding method in the MonitorSettings.
	 */
	@FXML
	void setThreshMethod() {
		if (threshGausianRadio.isSelected()) {
			MonitorSettings.thresholdingType = Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C;
		} else if (threshMeanRadio.isSelected()) {
			MonitorSettings.thresholdingType = Imgproc.ADAPTIVE_THRESH_MEAN_C;
		}
	}

	/**
	 * Updates the thresholding block size used when the user changes it in the
	 * settings.
	 * 
	 * @param value
	 *            - the new block size.
	 */
	void setThreshBlockSize(int value) {
		value = (value % 2 == 0) ? --value : value;
		value = (value < 3) ? 3 : value;
		MonitorSettings.thresholdingBlockSize = value;
	}

	/**
	 * Updates the change value in the thresholding settings when the user
	 * changes it.
	 * 
	 * @param value
	 *            - the new change value
	 */
	void setThreshChange(double value) {
		MonitorSettings.thresholdingChange = value;
	}

	/**
	 * Called when user enables/disables eroding. Updates the MonitorSettings
	 * with the current value;
	 */
	@FXML
	void setErodingEnabled() {
		MonitorSettings.erodingEnabled = erodingCheck.isSelected();
	}

	/**
	 * Called when the user changes the eroding shape. Updates monitor settings
	 * with the new shape.
	 */
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

	/**
	 * Called when the user changes the eroding size. Updates the
	 * MonitorSettings with the new size.
	 * 
	 * @param value
	 *            - the new size
	 */
	void setErodeSize(int value) {
		// If value is even, decrement so it is odd
		value = (value % 2 == 0) ? --value : value;
		// if less than 3 set to 3
		value = (value < 3) ? 3 : value;
		MonitorSettings.erodingSize = value;
	}

	/**
	 * Called when the user changes the erode iterations. Updates
	 * MonitorSettings with the new number of iterations
	 * 
	 * @param value
	 *            - the number of iterations
	 */
	void setErodeIterations(int value) {
		MonitorSettings.erodingIterations = value;
	}

	/**
	 * Called when the user changes the clossed only value Updates the
	 * MonitorSettings with the new value
	 */
	@FXML
	void setDropCheck() {
		MonitorSettings.dropChildless = dropCheck.isSelected();
	}

	/**
	 * Called when the user enables/disables dilating. Updates the
	 * MonitorSettings with the new value
	 */
	@FXML
	void setDilatingEnabled() {
		MonitorSettings.dilatingEnabled = dilatingCheck.isSelected();
	}

	/**
	 * Called when the user changes the dilating shape. Updates the
	 * MonitorSettings with the new value
	 */
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

	/**
	 * Called when the user changes the dilating size.Updates the
	 * MonitorSettings with the new value.
	 * 
	 * @param value
	 *            - the new dilating size
	 */
	void setDilateSize(int value) {
		value = (value % 2 == 0) ? --value : value;
		value = (value < 3) ? 3 : value;
		MonitorSettings.dilatingSize = value;
	}

	/**
	 * Called when the user changes the dilating iterations. Updates the
	 * MonitorSettings with the new value.
	 * 
	 * @param value
	 *            - the number of iterations
	 */
	void setDilateIterations(int value) {
		MonitorSettings.dilatingIterations = value;
	}

	/**
	 * This method handles MouseEvents on the spaces image. Left Click to add
	 * points/Spaces Right Click to remove Spaces
	 * 
	 * @param e
	 *            - The MouseEvent that occured
	 */
	private void spacesClicked(MouseEvent e) {
		double wratio = spacesImageView.getImage().getWidth() / spacesImageView.getFitWidth();
		double hratio = spacesImageView.getImage().getHeight() / spacesImageView.getFitHeight();
		// TODO USE CORRECT RATIO BASE ON CAMERA TYPE(LANSCAPE OR PORTRAIT)
		double xCord = e.getX() * wratio;
		double yCord = e.getY() * wratio;
		if (e.getButton() == MouseButton.PRIMARY) {
			// Add point for new Space
			spaceMgr.addPoint((int) (xCord), (int) (yCord));
		} else if (e.getButton() == MouseButton.SECONDARY) {
			// Test spaces to find one to remove
			for (Space s : spaceMgr.getSpaces()) {
				if (s.isPointInSpace((int) xCord, (int) yCord)) {
					spaceMgr.deleteSpace(s);
					break;
				}
			}
		}
	}

	/**
	 * Updates the required percentage for a space to be occupied when the user
	 * changes it.
	 * 
	 * @param val
	 *            - the new percentage
	 */
	private void setOccupiedPercentage(double val) {
		MonitorSettings.occupiedPercentage = (val / 100.0);
	}

	/**
	 * Updates the min number of frames the must be equal before a space state
	 * changes.
	 * 
	 * @param new_val
	 *            - the new occupied time
	 */
	private void setMinOccupiedTime(int new_val) {
		MonitorSettings.occupiedTime = new_val;
	}

	/**
	 * Handles on change events for the camIndexTextField. It checks if the new
	 * value is valid and if it is it updates the monitor settings.
	 * 
	 * @param ov
	 *            - a String ObservableValue
	 * @param old_val
	 *            - the old value( before it was changed)
	 * @param new_val
	 *            - the new value ( after it was change)
	 */
	private void onCamIndexChange(ObservableValue<? extends String> ov, String old_val, String new_val) {
		if (!new_val.matches("")) {
			if (!new_val.matches("-?[0-9]+")) {
				camIndexTextField.textProperty().setValue(old_val);
			} else {
				int value = Integer.parseInt(new_val);
				value = (value >= -1) ? value : -1;
				MonitorSettings.camIndex = value;
			}
		}

	}

	/**
	 * Updates the value of which threshold view should be shown.
	 */
	@FXML
	void setThreshView() {
		MonitorSettings.objectThresholdView = thresholdViewCheck.isSelected();
	}

	/**
	 * Opens a dialog and saves the settings to the file chosen.
	 */
	@FXML
	void saveSettings() {
		FileChooser fc = new FileChooser();
		fc.setInitialDirectory(new File(System.getProperty("user.dir")));
		fc.setInitialFileName("settings.cms");
		fc.setTitle("Save Monitor Settings");
		fc.setSelectedExtensionFilter(new ExtensionFilter("Carpark Monitor Settings", ".cms"));
		File file = fc.showSaveDialog(startButton.getScene().getWindow());
		if (file != null) {
			MonitorSettings.saveSettings(file);
		}
	}

	/**
	 * Opens a dialog and loads the settings file chosen.
	 */
	@FXML
	void loadSettings() {
		FileChooser fc = new FileChooser();
		fc.setInitialDirectory(new File(System.getProperty("user.dir")));
		fc.setInitialFileName("settings.cms");
		fc.setTitle("Load Monitor Settings");
		fc.setSelectedExtensionFilter(new ExtensionFilter("Carpark Monitor Settings", ".cms"));
		File file = fc.showOpenDialog(startButton.getScene().getWindow());
		if (file != null) {
			MonitorSettings.loadSettings(file);
			refreshGuiComponents();
		}

	}

	/**
	 * Updates all the gui components with the values stored in the MonitorSettings Class
	 */
	void refreshGuiComponents() {
		camIndexTextField.textProperty().set("" + MonitorSettings.camIndex);
		// Refresh Thresholding Components
		if (MonitorSettings.thresholdingType == Imgproc.ADAPTIVE_THRESH_MEAN_C) {
			threshGausianRadio.selectedProperty().set(false);
			threshMeanRadio.selectedProperty().set(true);
		} else {
			threshGausianRadio.selectedProperty().set(true);
			threshMeanRadio.selectedProperty().set(false);
		}
		threshBlockSlider.valueProperty().set(MonitorSettings.thresholdingBlockSize);
		threshChangeSlider.valueProperty().set(MonitorSettings.thresholdingChange);
		thresholdViewCheck.selectedProperty().set(MonitorSettings.objectThresholdView);
		// Morphology Settings
		erodingCheck.selectedProperty().set(MonitorSettings.erodingEnabled);
		dilatingCheck.selectedProperty().set(MonitorSettings.dilatingEnabled);
		erodeSizeSlider.valueProperty().set(MonitorSettings.erodingSize);
		dilateSizeSlider.valueProperty().set(MonitorSettings.dilatingSize);
		erodeIterSlider.valueProperty().set(MonitorSettings.erodingIterations);
		dilateIterSlider.valueProperty().set(MonitorSettings.dilatingIterations);
		switch (MonitorSettings.erodingShape) {
		case Imgproc.CV_SHAPE_ELLIPSE:
			erodeCrossRadio.selectedProperty().set(false);
			erodeEllipseRadio.selectedProperty().set(true);
			erodeRectRadio.selectedProperty().set(false);
			break;
		case Imgproc.CV_SHAPE_RECT:
			erodeCrossRadio.selectedProperty().set(false);
			erodeEllipseRadio.selectedProperty().set(false);
			erodeRectRadio.selectedProperty().set(true);
			break;
		case Imgproc.CV_SHAPE_CROSS:
		default:
			erodeCrossRadio.selectedProperty().set(true);
			erodeEllipseRadio.selectedProperty().set(false);
			erodeRectRadio.selectedProperty().set(false);
			break;
		}
		switch (MonitorSettings.dilatingShape) {
		case Imgproc.CV_SHAPE_ELLIPSE:
			dilateCrossRadio.selectedProperty().set(false);
			dilateEllipseRadio.selectedProperty().set(true);
			dilateRectRadio.selectedProperty().set(false);
			break;
		case Imgproc.CV_SHAPE_RECT:
			dilateCrossRadio.selectedProperty().set(false);
			dilateEllipseRadio.selectedProperty().set(false);
			dilateRectRadio.selectedProperty().set(true);
			break;
		case Imgproc.CV_SHAPE_CROSS:
		default:
			dilateCrossRadio.selectedProperty().set(true);
			dilateEllipseRadio.selectedProperty().set(false);
			dilateRectRadio.selectedProperty().set(false);
			break;
		}
		//Detection Settings
		dropCheck.selectedProperty().set(MonitorSettings.dropChildless);
		ocupiedTimeSlider.valueProperty().set(MonitorSettings.occupiedTime);
		occupiedPercentageSlider.valueProperty().set(MonitorSettings.occupiedPercentage);
		
	}
}
