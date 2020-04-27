//**********************************************************
// Class: GUIController
// Author: Ryley G.
// Date Modified: April 15, 2020
//
// Purpose: Overall manager of the GUI - specifically, it handles the movement of the GUI from one section to the other as the user permits
//************************************************************

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.text.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.Region;

import java.lang.invoke.MethodHandles.Lookup;
import java.util.List;

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
    
                if (appList.length > 0)
                {
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
                        
                        
                        userPreferencesGUI.lookup("#userPreferencesSubmitButton").setOnMouseReleased(g ->
                        {
                            int userBudget = userPreferencesGUI.getBudget();
                            List<String> selectedConfigs = userPreferencesGUI.getSelectedConfigs();
                            String[] selectedConfigsArr = selectedConfigs.toArray(new String[0]);
                            List<String> selectedArchetypes = userPreferencesGUI.getSelectedArchetypes();
                            String[] selectedArchetypesArr = selectedArchetypes.toArray(new String[0]);
                            String configString = "";
                            String archetypeString = "";

                            if (selectedConfigsArr.length > 0 && selectedArchetypesArr.length > 0 && userBudget > -1)
                            {
                                for (int i = 0; i < selectedConfigsArr.length; i++)
                                {
                                    configString += selectedConfigsArr[i]+"\n";
                                }

                                for (int i = 0; i < selectedArchetypesArr.length; i++)
                                {
                                    archetypeString += selectedArchetypesArr[i]+"\n";
                                }

                                confirmationAlert.setTitle("Preference Confirmation");
                                confirmationAlert.setHeaderText("Please confirm that the preferences below are correct");
                                confirmationAlert.setContentText("Budget: " + userBudget + "\n\nSelected Configurations:\n" + configString + "\nSelected Archetypes:\n" + archetypeString);
                                confirmationAlert.showAndWait();

                                if (confirmationAlert.getResult() == ButtonType.OK)
                                {
                                    configurator.setUserBudget(userBudget);
                                    configurator.setSelectedConfigs(selectedConfigs);
                                    configurator.setSelectedArchetypes(selectedArchetypes);
                                    BuildGenerationGUI buildGenerationGUI = new BuildGenerationGUI(scene, configurator);
                                    rootPane.getChildren().set(0,buildGenerationGUI);

                                }
                            }
                            else
                            {
                                Alert configAlert = new Alert(AlertType.INFORMATION);
                                configAlert.setTitle("Action needed");
                                configAlert.setHeaderText("Warning: not all fields completed");
                                configAlert.getDialogPane().setContentText("At least one of the fields haven't been completed. Make sure that each field has a value before proceeding.");
                                configAlert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE); //Ensures alert window always fits the full text
                                configAlert.showAndWait();
                            }
                        });
                        
                    }
                }

                else
                {
                    Alert infoAlert = new Alert(AlertType.INFORMATION);
                    infoAlert.setTitle("Cannot proceed");
                    infoAlert.setHeaderText("Warning: no applications submitted");
                    infoAlert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE); //Ensures alert window always fits the full text
                    infoAlert.setContentText("The program cannot proceed until you have entered at least one suitable application. Remember that the more programs you enter, the more likely it is that the generated build will suit all of your needs.");
                    infoAlert.showAndWait();  
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

