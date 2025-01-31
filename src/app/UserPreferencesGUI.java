//**********************************************************
// Class: UserPreferencesGUI
// Author: Ryley G.
// Date Modified: April 15, 2020
//
// Purpose: Handles various pieces of input from the user pertaining to product preferences, budget, and other items.
//
//
//************************************************************

import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.text.*;
import javafx.scene.control.TextField;
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
import java.util.List;
import java.util.ArrayList;


class UserPreferencesGUI extends VBox
{
    private int userBudget;
    private List<String> selectedConfigs = new ArrayList<String>();
    private List<String> selectedArchetypes = new ArrayList<String>();

    public UserPreferencesGUI(Scene scene, Configurator configurator)
    {
        //GUI node creation
        Text overallInformationText = new Text("Go through each of the following options and enter the information that applies to you. Once you are done, click the submit button at the bottom of the window.\n");
        Button submitButton = new Button("Submit");
        submitButton.setId("userPreferencesSubmitButton");

        Text budgetText = new Text("Overall budget (include no peripherals and round to the nearest dollar) [Click for more information]:");
        TextField budgetField = new TextField();

        Text configurationVariationText = new Text("Select the configuration(s) you are interested in (Right click each item for more information):");
        VBox configurationVariationOptions = new VBox();
        CheckBox optimalConfigCB = new CheckBox("Optimal configuration");
        CheckBox budgetOrientedConfigCB = new CheckBox("Budget-oriented configuration");
        CheckBox performanceOrientedConfigCB = new CheckBox("Performance-oriented configuration");
        
        Alert moreInfoAlert = new Alert(AlertType.INFORMATION);
        moreInfoAlert.setTitle("Configuration Information");
        moreInfoAlert.setHeaderText("Detailed configuration information");
        moreInfoAlert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE); //Ensures alert window always fits the full text

        //This is used to help narrow down the hardware choices later on
        Text userArchetypesText = new Text("Select which of the following you feel represent you");
        VBox archetypeOptions = new VBox();
        CheckBox hardcoreGamerCB = new CheckBox("Hardcore Gamer");
        CheckBox contentCreatorCB = new CheckBox("Content Creator");
        CheckBox storageCB = new CheckBox("Need lots of storage");

        //GUI node setup
        overallInformationText.wrappingWidthProperty().bind(scene.widthProperty());
        userArchetypesText.wrappingWidthProperty().bind(scene.widthProperty());
        budgetField.maxWidthProperty().bind(scene.widthProperty().multiply(0.15));
        configurationVariationOptions.spacingProperty().bind(scene.heightProperty().multiply(0.007));
        archetypeOptions.spacingProperty().bind(scene.heightProperty().multiply(0.007));
        submitButton.translateYProperty().bind(scene.heightProperty().subtract(submitButton.layoutYProperty()).subtract(submitButton.heightProperty()).multiply(0.95));
        this.spacingProperty().bind(scene.widthProperty().multiply(0.015));
        this.translateXProperty().bind(scene.widthProperty().multiply(0.01));


        //Ensure user-input data validity
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

        //Cursor edits
        budgetText.setOnMouseEntered(e ->
        {
            this.setCursor(Cursor.HAND);
        });
        budgetText.setOnMouseExited(e ->
        {
            this.setCursor(Cursor.DEFAULT);
        });
        optimalConfigCB.setOnMouseEntered(e ->
        {
            this.setCursor(Cursor.HAND);
        });
        optimalConfigCB.setOnMouseExited(e ->
        {
            this.setCursor(Cursor.DEFAULT);
        });
        budgetOrientedConfigCB.setOnMouseEntered(e ->
        {
            this.setCursor(Cursor.HAND);
        });
        budgetOrientedConfigCB.setOnMouseExited(e ->
        {
            this.setCursor(Cursor.DEFAULT);
        });
        performanceOrientedConfigCB.setOnMouseEntered(e ->
        {
            this.setCursor(Cursor.HAND);
        });
        performanceOrientedConfigCB.setOnMouseExited(e ->
        {
            this.setCursor(Cursor.DEFAULT);
        });

        //Display more info to user as required
        budgetText.setOnMouseClicked(e ->
        {
            moreInfoAlert.getDialogPane().setContentText("The number you put in this field should represent the amount you wish to pay the computer configuration itself while excluding the price of any peripherals. Typical peripherals include pointer mice, keyboards, monitors, and headphones.");
            moreInfoAlert.showAndWait();    
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

        //The setting of these values is set to occur when the mouse enters the button so the action does not interfere with the click event handler for the same button found in the GUIController class.
        submitButton.setOnMouseEntered(e ->
        {
            this.selectedConfigs.clear();
            this.selectedArchetypes.clear();

            try
            {
                this.userBudget = Integer.parseInt(budgetField.getText());
            }
            catch (NumberFormatException f)
            {
                this.userBudget = -1;
            }

            if (optimalConfigCB.isSelected() && this.selectedConfigs.contains(optimalConfigCB.getText()) == false)
            {
                this.selectedConfigs.add(optimalConfigCB.getText());
            }
            if (budgetOrientedConfigCB.isSelected() && this.selectedConfigs.contains(budgetOrientedConfigCB.getText()) == false)
            {
                this.selectedConfigs.add(budgetOrientedConfigCB.getText());
            }
            if (performanceOrientedConfigCB.isSelected() && this.selectedConfigs.contains(performanceOrientedConfigCB.getText()) == false)
            {
                this.selectedConfigs.add(performanceOrientedConfigCB.getText());
            }
        
            if (hardcoreGamerCB.isSelected())
            {
                this.selectedArchetypes.add(hardcoreGamerCB.getText());
            }
            if (contentCreatorCB.isSelected())
            {
                this.selectedArchetypes.add(contentCreatorCB.getText());
            }
            if (storageCB.isSelected())
            {
                this.selectedArchetypes.add(storageCB.getText());
            }
        });

        configurationVariationOptions.getChildren().addAll(configurationVariationText,optimalConfigCB,budgetOrientedConfigCB,performanceOrientedConfigCB);
        archetypeOptions.getChildren().addAll(userArchetypesText, hardcoreGamerCB, contentCreatorCB, storageCB);
        this.getChildren().addAll(overallInformationText, budgetText, budgetField, configurationVariationOptions, archetypeOptions, submitButton);
    }

    //Setters & getters

    public int getBudget()
    {
        return this.userBudget;
    }
    public List<String> getSelectedConfigs()
    {
        return this.selectedConfigs;
    }

    public List<String> getSelectedArchetypes()
    {
        return this.selectedArchetypes;
    }
}