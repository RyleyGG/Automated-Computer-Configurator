//**********************************************************
// Class: GUIController
// Author: Ryley G.
// Date Modified: March 27, 2020
//
// Purpose: Manage the application input window in the GUI
//
// Additional Notes: Overall manager of the GUI - specifically, it handles the movement of the GUI from one section to the other as the user permits
//************************************************************

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.text.*;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.Region;

public class GUIController extends Application
{
    @Override
    public void start(Stage primaryStage)
    {
        Configurator configurator = new Configurator();
        Pane rootPane = new Pane();
        Scene scene = new Scene(rootPane,720,480);

        WelcomeGUI welcomeGUI = new WelcomeGUI(scene);

        welcomeGUI.getChildren().get(2).setOnMouseClicked(e -> 
        {
            ApplicationInputGUI applicationInputGUI = new ApplicationInputGUI(scene,configurator);
            rootPane.getChildren().set(0,applicationInputGUI);

            applicationInputGUI.lookup("#continueButton").setOnMouseReleased(f ->
            {                
                Text currentStatusText = new Text("Confirming application submission...");
                applicationInputGUI.getCurrentStatusContainer().getChildren().set(0,currentStatusText);
                String alertString = "";
                GenericApplication[] appList = configurator.getAppList().toArray(new GenericApplication[0]);
    
                for (int i = 0; i < appList.length; i++)
                {
                    alertString += appList[i].getName() + "\n";
                }
    
                Alert confirmationAlert = new Alert(AlertType.CONFIRMATION);
                confirmationAlert.setTitle("Application Submission");
                confirmationAlert.setHeaderText("Confirm that these are the applications you wish to submit.");
                ((Button) confirmationAlert.getDialogPane().lookupButton(ButtonType.OK)).setText("Yes");
                ((Button) confirmationAlert.getDialogPane().lookupButton(ButtonType.CANCEL)).setText("No");
                confirmationAlert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE); //Ensures alert window always fits the full text
                confirmationAlert.setContentText(alertString);
                confirmationAlert.showAndWait();  
                
                if (confirmationAlert.getResult() == ButtonType.OK)
                {
                    UserPreferencesGUI userPreferencesGUI = new UserPreferencesGUI(scene, configurator);
                    rootPane.getChildren().set(0,userPreferencesGUI);
                }
            });
        });


        rootPane.getChildren().add(welcomeGUI);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Automated Personal Computer Configurator");
		primaryStage.show();
    }

    public static void main(String args[])
    {
        launch(args);
    }
}

