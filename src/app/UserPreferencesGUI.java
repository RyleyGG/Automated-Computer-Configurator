//**********************************************************
// Class: UserPreferencesGUI
// Author: Ryley G.
// Date Modified: April 4, 2020
//
// Purpose: Handles various pieces of input from the user pertaining to product preferences, budget, and other items.
//
//
//************************************************************

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.text.*;
import javafx.scene.control.TextField;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.Region;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.collections.FXCollections;
import javafx.scene.control.TextFormatter;
import javafx.util.converter.IntegerStringConverter;


class UserPreferencesGUI extends VBox
{
    public UserPreferencesGUI(Scene scene, Configurator configurator)
    {
        //GUI node creation
        Text overallInformationText = new Text("Go through each of the following options and enter the information that applies to you. You can right click the configuration options to see more details. Once you are done, click the submit button at the bottom of the window.\n");
        Button submitButton = new Button("Submit");

        Text budgetText = new Text("Overall budget (including no peripherals [i.e. pointer mouse, keyboard, monitor, etc.]):");
        TextField budgetField = new TextField();
        HBox budgetForm = new HBox();

        Text preferredMonitorResText = new Text("Preferred monitor resolution:");
        String[] resolutionList =  new String[3];
        resolutionList[0] = "1920 x 1080 (1080p)";
        resolutionList[1] = "2560 x 1440 (1440p)";
        resolutionList[2] = "4096 x 2160 (4K)";
        ComboBox<String> preferredMonitorResField = new ComboBox<String>(FXCollections.observableArrayList(resolutionList));
        HBox preferredMonitorResForm = new HBox();

        Text configurationVariationText = new Text("Select the configuration(s) you are interested in:");
        VBox configurationVariationOptions = new VBox();
        CheckBox optimalConfigCB = new CheckBox("Optimal Configuration");
        CheckBox budgetOrientedConfigCB = new CheckBox("Budget-oriented configuration");
        CheckBox performanceOrientedConfigCB = new CheckBox("Performance-oriented configuration");
        
        Alert moreInfoAlert = new Alert(AlertType.INFORMATION);
        moreInfoAlert.setTitle("Configuration Information");
        moreInfoAlert.setHeaderText("Detailed configuration information");
        moreInfoAlert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE); //Ensures alert window always fits the full text

        //GUI node setup
        overallInformationText.wrappingWidthProperty().bind(scene.widthProperty());
        budgetForm.prefWidthProperty().bind(scene.widthProperty());
        budgetForm.spacingProperty().bind(scene.widthProperty().multiply(0.007));
        preferredMonitorResForm.prefWidthProperty().bind(scene.widthProperty());
        preferredMonitorResForm.spacingProperty().bind(scene.widthProperty().multiply(0.007));
        configurationVariationOptions.spacingProperty().bind(scene.heightProperty().multiply(0.007));
        submitButton.translateYProperty().bind(scene.heightProperty().subtract(submitButton.layoutYProperty()).subtract(submitButton.heightProperty()).multiply(0.95));
        this.spacingProperty().bind(scene.widthProperty().multiply(0.015));
        this.translateXProperty().bind(scene.widthProperty().multiply(0.01));


        budgetField.setTextFormatter(new TextFormatter<>(new IntegerStringConverter()));
        budgetField.setOnKeyReleased(e ->
        {
            //The text formatter ensures that only integers are typed into the budget field.
            //However, it only applies when the field itself loses focus. So, another node was arbitrarily chosen to receive focus before the budget field gets it back.
            //The general speed of this operation coupled with the selectEnd() method means that the user will never tell that this switch of GUI focus ever occurs.
            optimalConfigCB.requestFocus();
            budgetField.requestFocus();
            budgetField.selectEnd();
        });
        optimalConfigCB.setOnContextMenuRequested(e ->
        {
            moreInfoAlert.getDialogPane().setContentText("The optimal configuration represents the best balance between price and performance based on your input, including your budget and application preferences.");
            moreInfoAlert.showAndWait();
        });
        budgetOrientedConfigCB.setOnContextMenuRequested(e ->
        {
            moreInfoAlert.getDialogPane().setContentText("The budget-oriented configuration represents a build that will be on the cheaper side without sacrificing a lot of performance. For example, this option may be 5% less powerful than the optimal configuration option, but 10-15% cheaper.");
            moreInfoAlert.showAndWait();
        });
        performanceOrientedConfigCB.setOnContextMenuRequested(e ->
        {
            moreInfoAlert.getDialogPane().setContentText("The performance-oriented configuration represents a build that will be more expensive than the optimal configuration but will make up for it with a large increase in performance. For example, this option may be 5% more expensive than the optimal configuration option, but 10-15% more powerful.");
            moreInfoAlert.showAndWait();
        });

        budgetForm.getChildren().addAll(budgetText,budgetField);
        preferredMonitorResForm.getChildren().addAll(preferredMonitorResText,preferredMonitorResField);
        configurationVariationOptions.getChildren().addAll(configurationVariationText,optimalConfigCB,budgetOrientedConfigCB,performanceOrientedConfigCB);
        this.getChildren().addAll(overallInformationText,budgetForm,preferredMonitorResForm,configurationVariationOptions, submitButton);
    }
}