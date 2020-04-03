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

