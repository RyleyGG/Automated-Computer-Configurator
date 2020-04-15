//**********************************************************
// Class: ApplicationInputGUI
// Author: Ryley G.
// Date Modified: April 3, 2020
//
// Purpose: Manage the application input window in the GUI
//
// Additional Notes: For testing the Steam API caching, some applications that definitely work include "Garry's Mod" and "Divinity: Original Sin 2"
//************************************************************


//GUI
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.text.*;
import javafx.geometry.Pos;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.control.ComboBox;
import javafx.collections.FXCollections;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.control.TextField;
import javafx.scene.image.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

//Steam API
import pl.l7ssha.javasteam.*;

//General
import com.google.gson.JsonSyntaxException;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

//Web-scraping
import com.jaunt.JauntException;
import com.jaunt.ResponseException;
import com.jaunt.UserAgent;


public class ApplicationInputGUI extends VBox
{
    private String[][] basicSteamAppData;
    private HBox totalApplicationCountContainer = new HBox();
    private HBox currentStatusContainer = new HBox();
    private int applicationCount = 0;

    public ApplicationInputGUI(Scene scene, Configurator configurator)
    {
        //Top, preliminary input section

        //GUI node creation
        HBox preliminaryPanel = new HBox();
        Text applicationTypeText = new Text("Application Source:");
        Button continueButton = new Button("Continue");

        String[] applicationList =  new String[3];
        applicationList[0] = "Steam Marketplace";
        applicationList[1] = "Website";
        applicationList[2] = "Enter Data Manually";
        ComboBox<String> applicationOptions = new ComboBox<String>(FXCollections.observableArrayList(applicationList));

        //GUI node setup
        preliminaryPanel.spacingProperty().bind(scene.widthProperty().multiply(0.01));
        preliminaryPanel.prefWidthProperty().bind(scene.widthProperty());
        preliminaryPanel.prefHeightProperty().bind(scene.heightProperty().multiply(0.1));
        preliminaryPanel.translateXProperty().bind(scene.widthProperty().multiply(0.01));
        preliminaryPanel.getChildren().addAll(applicationTypeText,applicationOptions, continueButton);
        continueButton.setId("continueButton");


        //Center, main input section
        
        //GUI node creation
        VBox defaultPanel = new VBox();
        Text defaultText = new Text("Select an application source to begin");

        //GUI node setup
        defaultPanel.setAlignment(Pos.BASELINE_CENTER);
        defaultPanel.prefWidthProperty().bind(scene.widthProperty());
        defaultPanel.prefHeightProperty().bind(scene.heightProperty());
        defaultText.translateYProperty().bind(scene.heightProperty().multiply(0.32));
        
        applicationOptions.setOnAction(e ->
        {
            if (applicationOptions.getValue() == "Steam Marketplace")
            {
                VBox steamApplicationInputPanel = this.createSteamInputPanel(scene, configurator);
                this.getChildren().set(1,steamApplicationInputPanel);
            }
            else if (applicationOptions.getValue() == "Website")
            {
                VBox websiteApplicationInputPanel = this.createWebsiteInputPanel(scene, configurator);
                this.getChildren().set(1,websiteApplicationInputPanel);
            }
            else if (applicationOptions.getValue() == "Enter Data Manually")
            {
                VBox manualApplicationInputPanel = this.createManualInputPanel(scene, configurator);
                this.getChildren().set(1,manualApplicationInputPanel);
            }
        });

        defaultPanel.prefHeightProperty().bind(scene.heightProperty().multiply(0.86));
        defaultPanel.getChildren().add(defaultText);

        //Bottom, current status section

        //GUI node creation
        HBox infoPanel = new HBox();
        this.totalApplicationCountContainer = new HBox();
        this.currentStatusContainer = new HBox();
        Text totalApplicationCountText = new Text("Total Applications Saved: 0");
        Text currentStatusText = new Text("Waiting for user input...");

        //GUI node setup
        infoPanel.prefWidthProperty().bind(scene.widthProperty());
        infoPanel.prefHeightProperty().bind(scene.heightProperty().multiply(0.05));
        this.totalApplicationCountContainer.prefWidthProperty().bind(scene.widthProperty());
        this.totalApplicationCountContainer.prefHeightProperty().bind(scene.heightProperty().multiply(0.05));
        this.currentStatusContainer.prefWidthProperty().bind(scene.widthProperty());
        this.currentStatusContainer.prefHeightProperty().bind(scene.heightProperty().multiply(0.05));
        this.totalApplicationCountContainer.setAlignment(Pos.CENTER_LEFT);
        this.currentStatusContainer.setAlignment(Pos.CENTER_RIGHT);
        this.totalApplicationCountContainer.translateXProperty().bind(scene.widthProperty().multiply(0.01));
        this.currentStatusContainer.translateXProperty().bind(scene.widthProperty().multiply(0.01).divide(-1));

        this.totalApplicationCountContainer.getChildren().add(totalApplicationCountText);
        this.currentStatusContainer.getChildren().add(currentStatusText);
        infoPanel.getChildren().addAll(totalApplicationCountContainer,currentStatusContainer);

        this.getChildren().addAll(preliminaryPanel,defaultPanel,infoPanel);
    }

    public VBox createSteamInputPanel(Scene scene, Configurator configurator)
    {
        //Preliminary Steam API setup
        SteamAPI.initialize("8C903FFFA438516B3096134D21E4B43B"); //Web API Key, used to access the Steam API
        this.basicSteamAppData = configurator.parseBasicSteamData(configurator.saveBasicSteamData()); //Data for nearly all applications across Steam
        String workingDir = System.getProperty("user.dir"); //Review note: On the author's personal machine, Java was not properly finding the CWD, so its explicitly set here
        Text defaultStatusText = new Text("Waiting for user input...");
        this.currentStatusContainer.getChildren().set(0,defaultStatusText);

        //Preliminary GUI setup
        VBox inputPanel = new VBox();
        inputPanel.prefWidthProperty().bind(scene.widthProperty());
        inputPanel.prefHeightProperty().bind(scene.heightProperty().multiply(0.85));
        inputPanel.setAlignment(Pos.BASELINE_CENTER);


        //GUI node creation
        Text text = new Text("Enter applications below");
        TextField enterApplication = new TextField();

        //GUI node setup
        text.translateYProperty().bind(scene.heightProperty().multiply(0.32));
        enterApplication.maxWidthProperty().bind(scene.widthProperty().multiply(0.95));
        enterApplication.setAlignment(Pos.BASELINE_CENTER);
        enterApplication.translateYProperty().bind(scene.heightProperty().multiply(0.35));


        //Whenever the user types in the search bar
        enterApplication.setOnKeyReleased(e ->
        {
            inputPanel.getChildren().remove(2, inputPanel.getChildren().size());

            int totalApplicableApps = 0;
            String[][] curatedApplicableApps = new String[5][2];

            //Setting up front-facing curated list of applications based on user input
            //Review note: This currently iterates through the entirety of the application list gathered from Steam. It will updated to be more optimized in the future.
            for (int i = 0; i < this.basicSteamAppData.length-2; i++)
            {
                if ((this.basicSteamAppData[i][0].contains(enterApplication.getText()) || this.basicSteamAppData[i][0].toLowerCase().contains(enterApplication.getText())) && totalApplicableApps < 5)
                {
                    totalApplicableApps++;
                    curatedApplicableApps[totalApplicableApps-1][0] = this.basicSteamAppData[i][0];
                    curatedApplicableApps[totalApplicableApps-1][1] = this.basicSteamAppData[i][1];
                }

                //Minor time-saving optimization.
                if (totalApplicableApps >= 5)
                {
                    break;
                }
            }

            //For each curated application, generate and implement the necessary GUI elements to reflect the applications
            for (int i = 0; i < totalApplicableApps; i++)
            {
                if (curatedApplicableApps[i][0] != null)
                {
                    //Currently an issue with some applications as it pertains to grabbing full info
                    //Issue seems isolated to the API attempting to gather Mac/Linux requirements. This is an issue with the API itself.
                    //For now, the information for these applications cannot be gathered but a workaround will hopefully eventually be implemented
                
                    //GUI node creation (for curated application display)
                    StackPane curatedGameView = new StackPane();
                    HBox appInformation = new HBox();
                    Rectangle appBackground = new Rectangle();
                    Image appImage;

                    //This try-catch handles the loading/gathering and caching of application images.
                    String appImageLoc = "";
                    try
                    {
                        //Method using hard-coded URL structure
                        //Benefits: Much faster than the Steam API method
                        appImageLoc = configurator.loadSteamAppImage(curatedApplicableApps[i][1]);

                        if (appImageLoc.length() == 0)
                        {
                            appImageLoc = "https://steamcdn-a.akamaihd.net/steam/apps/" + curatedApplicableApps[i][1] + "/header.jpg";
                            configurator.saveSteamAppImage(curatedApplicableApps[i][1],appImageLoc);
                        }
                        else
                        {
                            appImageLoc = appImageLoc.split(curatedApplicableApps[i][1] + ": ")[1].trim();
                        }

                        /* Method using SteamAPI
                        //Benefits: Potentially more future-proof than the hard-coded URL structure method
                        appImageLoc = configurator.loadSteamAppImage(curatedApplicableApps[i][1]);

                        if (appImageLoc.length() == 0)
                        {
                            RichSteamGame curatedApp = storefront.getFullInfoOfApp(curatedApplicableApps[i][1]);
                            appImageLoc = curatedApp.getHeaderImage();
                            configurator.saveSteamAppImage(curatedApplicableApps[i][1],appImageLoc);
                        }
                        else
                        {
                            appImageLoc = appImageLoc.split(curatedApplicableApps[i][1] + ": ")[1].trim();
                            System.out.println(appImageLoc);
                        }
                        */
                    }
                    catch (JsonSyntaxException f)
                    {
                        //f.printStackTrace();
                    }
                    catch (ArrayIndexOutOfBoundsException f)
                    {
                        //f.printStackTrace();
                    }

                    //Creating the image if possible, or using a default image if the given image URL is invalid
                    try
                    {
                        appImage = new Image(appImageLoc);
                    }
                    catch(IllegalArgumentException f)
                    {
                        appImage = new Image("file:///"+workingDir + "/cache/defaultimage.jpg");
                    }

                    ImageView appImageView = new ImageView(appImage);
                    Text appName = new Text(curatedApplicableApps[i][0]);
                    
                    //GUI node setup (for curated application display)
                    curatedGameView.prefWidthProperty().bind(enterApplication.widthProperty());
                    curatedGameView.prefHeightProperty().bind(scene.heightProperty().multiply(0.35).divide(5));
                    curatedGameView.translateYProperty().bind(enterApplication.translateYProperty().add(enterApplication.heightProperty()));

                    appInformation.prefWidthProperty().bind(enterApplication.widthProperty());
                    appInformation.prefHeightProperty().bind(enterApplication.heightProperty());
                    appInformation.setAlignment(Pos.CENTER_LEFT);
                    appInformation.setMouseTransparent(true);

                    appBackground.widthProperty().bind(curatedGameView.prefWidthProperty());
                    appBackground.heightProperty().bind(curatedGameView.prefHeightProperty());
                    appBackground.setFill(Color.rgb(230,230,230));
                    appBackground.setOnMouseEntered(f ->
                    {
                        appBackground.setFill(Color.rgb(150,150,150));
                        curatedGameView.getChildren().set(0,appBackground);
                        scene.setCursor(Cursor.HAND);
                    });
                    appBackground.setOnMouseExited(f ->
                    {
                        appBackground.setFill(Color.rgb(230,230,230));
                        curatedGameView.getChildren().set(0,appBackground);
                        scene.setCursor(Cursor.DEFAULT);
                    });

                    //Review Note: the setOnMouseClicked method has an issue with using the i increment directly...
                    //stating that it must be final. 'tempIndex' is a workaround, and although it seems less than optimal to me, it works for now.
                    int tempIndex = i;
                    appBackground.setOnMouseClicked(f ->
                    {
                        try
                        {
                            boolean appAlreadyEntered = false;

                            for (int x = 0; x < configurator.getAppList().size(); x++)
                            {
                                if (configurator.getAppList().get(x).name == curatedApplicableApps[tempIndex][0])
                                {
                                    appAlreadyEntered = true;
                                }
                            }

                            if (appAlreadyEntered == false)
                            {
                                if (configurator.createSteamApplication(curatedApplicableApps[tempIndex]).reqList != null)
                                {
                                    this.applicationCount++;
                                    Text applicationCountText = new Text("Total Applications Saved: " + this.applicationCount);
                                    this.totalApplicationCountContainer.getChildren().set(0,applicationCountText);
                                    Text currentStatusText = new Text("Application data saved successfully.");
                                    this.currentStatusContainer.getChildren().set(0,currentStatusText);
                                }
                                else if (configurator.loadCachedSteamApplication(curatedApplicableApps[tempIndex][0], curatedApplicableApps[tempIndex][1]) == true)
                                {
                                    this.applicationCount++;
                                    Text applicationCountText = new Text("Total Applications Saved: " + this.applicationCount);
                                    this.totalApplicationCountContainer.getChildren().set(0,applicationCountText);
                                    Text currentStatusText = new Text("Cached application data used successfully.");
                                    this.currentStatusContainer.getChildren().set(0,currentStatusText);
                                }
                                else
                                {
                                    Text currentStatusText = new Text("Failed to save application data.");
                                    this.currentStatusContainer.getChildren().set(0,currentStatusText);
                                }
                            }
                            else
                            {
                                Text currentStatusText = new Text("Application already entered");
                                this.currentStatusContainer.getChildren().set(0,currentStatusText);
                            }
                        }
                        catch(JsonSyntaxException g)
                        {
                            Text currentStatusText = new Text("Failed to save application data.");
                            this.currentStatusContainer.getChildren().set(0,currentStatusText);
                        }
                    });

                    appImageView.setPreserveRatio(true);
                    appImageView.fitHeightProperty().bind(appBackground.heightProperty().multiply(0.85));
                    appImageView.translateXProperty().bind(scene.widthProperty().multiply(0.05));

                    appName.translateXProperty().bind(appImageView.translateXProperty().multiply(1.05));
                    
                    appInformation.getChildren().addAll(appImageView,appName);
                    curatedGameView.getChildren().addAll(appBackground,appInformation);

                    //If this results to true, it means user has already begun search, so replace old applications
                    if (inputPanel.getChildren().size() > 2 && i == 0)
                    {
                        inputPanel.getChildren().remove(2, inputPanel.getChildren().size());
                        inputPanel.getChildren().add(curatedGameView);
                    }
                    else
                    {
                        inputPanel.getChildren().add(curatedGameView);
                    }

                    
                    if (totalApplicableApps == 0)
                    {
                        inputPanel.getChildren().remove(2, inputPanel.getChildren().size());
                    }
                }
            }
        });

        inputPanel.getChildren().addAll(text,enterApplication);
        return inputPanel;
    }

    public VBox createWebsiteInputPanel(Scene scene, Configurator configurator)
    {
        //Preliminary GUI setup
        VBox inputPanel = new VBox();
        inputPanel.prefWidthProperty().bind(scene.widthProperty());
        inputPanel.prefHeightProperty().bind(scene.heightProperty().multiply(0.85));
        inputPanel.setAlignment(Pos.BASELINE_CENTER);
        inputPanel.spacingProperty().bind(scene.heightProperty().multiply(0.025));        
        Text defaultStatusText = new Text("Waiting for user input...");
        this.currentStatusContainer.getChildren().set(0,defaultStatusText);

        //GUI node creation
        Text urlText = new Text("Webpage URL that hosts the application requirements:");
        Text nameText = new Text("Application name:");
        HBox urlForm = new HBox();
        HBox nameForm = new HBox();
        TextField enterApplicationName = new TextField();
        TextField enterApplicationURL = new TextField();
        Button submitURLButton = new Button("Search");
        Button spacerButton = new Button("Search");

        //GUI node setup

        urlForm.translateYProperty().bind(scene.heightProperty().multiply(0.32));
        urlText.translateYProperty().bind(urlForm.translateYProperty());
        nameForm.translateYProperty().bind(scene.heightProperty().multiply(0.27));
        nameText.translateYProperty().bind(nameForm.translateYProperty());
        urlForm.maxWidthProperty().bind(inputPanel.widthProperty());
        nameForm.maxWidthProperty().bind(inputPanel.widthProperty());
        enterApplicationURL.maxWidthProperty().bind(scene.widthProperty().multiply(0.85));
        enterApplicationName.maxWidthProperty().bind(scene.widthProperty().multiply(0.85));
        urlForm.spacingProperty().bind(scene.widthProperty().multiply(0.007));
        nameForm.spacingProperty().bind(scene.widthProperty().multiply(0.007));
        HBox.setHgrow(enterApplicationURL,Priority.ALWAYS);
        HBox.setHgrow(enterApplicationName,Priority.ALWAYS);
        enterApplicationName.setAlignment(Pos.BASELINE_CENTER);
        enterApplicationURL.setAlignment(Pos.BASELINE_CENTER);
        submitURLButton.setAlignment(Pos.BASELINE_RIGHT);
        spacerButton.setAlignment(Pos.BASELINE_RIGHT);
        nameForm.setAlignment(Pos.BASELINE_CENTER);
        urlForm.setAlignment(Pos.BASELINE_CENTER);
        spacerButton.setVisible(false);
        spacerButton.setDisable(true);

        urlForm.getChildren().addAll(enterApplicationURL, submitURLButton);
        nameForm.getChildren().addAll(enterApplicationName, spacerButton);
        inputPanel.getChildren().addAll(nameText, nameForm, urlText, urlForm);

        submitURLButton.setOnMouseClicked(e ->
        {
            Text currentStatusText = new Text("URL submitted. Connecting...");
            this.currentStatusContainer.getChildren().set(0,currentStatusText);

            //This local class is used at various points to attempt to find cached requirements if the program is unable to grab new data for any reason
            class FindCachedRequirements
            {
                public FindCachedRequirements(HBox currentStatusContainer, int applicationCount, HBox totalApplicationCountContainer)
                {
                    WebScrapedApplication tempApp = configurator.loadWebScrapedApplicationData(enterApplicationName.getText());
            
                    if (tempApp != null)
                    {
                        String[][] tempRequirements = tempApp.reqList;

                        String alertString = "";
                        for (int i = 0; i < tempRequirements.length; i++)
                        {
                            alertString += tempRequirements[i][0] + ": " + tempRequirements[i][1] + "\n";
                        }

                        Alert requirementAlert = new Alert(AlertType.CONFIRMATION);
                        requirementAlert.setTitle("Requirement Confirmation");
                        requirementAlert.setHeaderText("These were previously saved. Are they correct?");
                        ((Button) requirementAlert.getDialogPane().lookupButton(ButtonType.OK)).setText("Yes");
                        ((Button) requirementAlert.getDialogPane().lookupButton(ButtonType.CANCEL)).setText("No");
                        requirementAlert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE); //Ensures alert window always fits the full text
                        requirementAlert.setContentText(alertString);
                        requirementAlert.showAndWait();
    
                        if (requirementAlert.getResult() == ButtonType.OK)
                        {
                            configurator.saveWebScrapedApplication(enterApplicationName.getText(), tempRequirements);
                            Text currentStatusText = new Text("Requirements confirmed & saved.");
                            currentStatusContainer.getChildren().set(0,currentStatusText);
                            applicationCount++;
                            Text applicationCountText = new Text("Total Applications Saved: " + applicationCount);
                            totalApplicationCountContainer.getChildren().set(0,applicationCountText);
                        }
                        else
                        {
                            Text currentStatusText = new Text("Requirements deemed invalid. Consider using the manual application entry.");
                            currentStatusContainer.getChildren().set(0,currentStatusText);
                            applicationCount++;
                            Text applicationCountText = new Text("Total Applications Saved: " + applicationCount);
                            totalApplicationCountContainer.getChildren().set(0,applicationCountText);
                        }
                    }
                    else
                    {
                        Text currentStatusText = new Text("No cached data available. Consider using the manual application entry.");
                        currentStatusContainer.getChildren().set(0,currentStatusText);
                    }
                }
            }

            try
            {
                UserAgent userAgent = new UserAgent();
                userAgent.sendGET(enterApplicationURL.getText());
                currentStatusText = new Text("Connection successful. Looking for requirements...");
                this.currentStatusContainer.getChildren().set(0,currentStatusText);
                String[][] tempRequirements = configurator.parseWebData(enterApplicationName.getText(),userAgent.getSource());

                if (tempRequirements != null)
                {
                    currentStatusText = new Text("Requirements found. Confirming validity...");
                    this.currentStatusContainer.getChildren().set(0,currentStatusText);

                    Alert requirementAlert = new Alert(AlertType.CONFIRMATION);
                    requirementAlert.setTitle("Requirement Confirmation");
                    requirementAlert.setHeaderText("Are these correct?");
                    ((Button) requirementAlert.getDialogPane().lookupButton(ButtonType.OK)).setText("Yes");
                    ((Button) requirementAlert.getDialogPane().lookupButton(ButtonType.CANCEL)).setText("No");
                    requirementAlert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE); //Ensures alert window always fits the full text


                    String alertString = "";
                    for (int i = 0; i < tempRequirements.length; i++)
                    {
                        alertString += tempRequirements[i][0] + ": " + tempRequirements[i][1] + "\n";
                    }

                    requirementAlert.setContentText(alertString);
                    requirementAlert.showAndWait();

                    if (requirementAlert.getResult() == ButtonType.OK)
                    {
                        configurator.saveWebScrapedApplication(enterApplicationName.getText(), tempRequirements);
                        currentStatusText = new Text("Requirements confirmed & saved.");
                        this.currentStatusContainer.getChildren().set(0,currentStatusText);
                        this.applicationCount++;
                        Text applicationCountText = new Text("Total Applications Saved: " + this.applicationCount);
                        this.totalApplicationCountContainer.getChildren().set(0,applicationCountText);
                    }
                    else
                    {
                        if (enterApplicationName.getText().length() != 0)
                        {
                            currentStatusText = new Text("Requirements deemed invalid. Attempting to find cached requirement data...");
                            this.currentStatusContainer.getChildren().set(0,currentStatusText);
                            new FindCachedRequirements(this.currentStatusContainer, this.applicationCount, this.totalApplicationCountContainer);
                        }
                        else
                        {
                            currentStatusText = new Text("Requirements deemed invalid. Add a title if you wish to attempt to search cached data.");
                            this.currentStatusContainer.getChildren().set(0,currentStatusText);
                        }
                    }
                }
                else
                {
                    if (enterApplicationName.getText().length() != 0)
                    {
                        currentStatusText = new Text("Unable to find requirements. Attempting to find cached requirement data...");
                        this.currentStatusContainer.getChildren().set(0,currentStatusText);
                        new FindCachedRequirements(this.currentStatusContainer, this.applicationCount, this.totalApplicationCountContainer);
                    }
                    else
                    {
                        currentStatusText = new Text("Unable to find requirements. Add a title if you wish to attempt to search cached data.");
                        this.currentStatusContainer.getChildren().set(0,currentStatusText);
                    }
                }
            }
            catch (ResponseException f)
            {
                if (enterApplicationName.getText().length() != 0)
                {
                    currentStatusText = new Text("Failed to connect. Attempting to find cached requirement data...");
                    this.currentStatusContainer.getChildren().set(0,currentStatusText);
                    new FindCachedRequirements(this.currentStatusContainer, this.applicationCount, this.totalApplicationCountContainer);
                }
                else
                {
                    currentStatusText = new Text("Failed to connect.");
                    this.currentStatusContainer.getChildren().set(0,currentStatusText);
                }
            }
        });

        return inputPanel;
    }

    public VBox createManualInputPanel(Scene scene, Configurator configurator)
    {
        VBox inputPanel = new VBox();
        inputPanel.prefWidthProperty().bind(scene.widthProperty());
        inputPanel.prefHeightProperty().bind(scene.heightProperty().multiply(0.85));
        inputPanel.spacingProperty().bind(scene.widthProperty().multiply(0.007));
        Text defaultStatusText = new Text("Waiting for user input...");
        this.currentStatusContainer.getChildren().set(0,defaultStatusText);
        
        //GUI node creation
        Text appNameText = new Text("Application name:");
        Text cpuText = new Text("CPU:");
        Text gpuText = new Text("GPU:");
        Text ramText = new Text("RAM:");
        Text storageText = new Text("Storage:");
        TextField appNameField = new TextField();
        TextField cpuField = new TextField();
        TextField gpuField = new TextField();
        TextField ramField = new TextField();
        TextField storageField = new TextField();
        HBox appNameForm = new HBox();
        HBox cpuForm = new HBox();
        HBox gpuForm = new HBox();
        HBox ramForm = new HBox();
        HBox storageForm = new HBox();
        HBox buttonForm = new HBox();
        Button submitButton = new Button("Submit");
        Button searchCacheButton = new Button("Search Cache");

        //GUI Node setup
        appNameForm.maxWidthProperty().bind(scene.widthProperty());
        cpuForm.maxWidthProperty().bind(scene.widthProperty());
        gpuForm.maxWidthProperty().bind(scene.widthProperty());
        ramForm.maxWidthProperty().bind(scene.widthProperty());
        storageForm.maxWidthProperty().bind(scene.widthProperty());
        buttonForm.maxWidthProperty().bind(scene.widthProperty());

        HBox.setHgrow(appNameField, Priority.ALWAYS);
        HBox.setHgrow(cpuField, Priority.ALWAYS);
        HBox.setHgrow(gpuField, Priority.ALWAYS);
        HBox.setHgrow(ramField, Priority.ALWAYS);
        HBox.setHgrow(storageField, Priority.ALWAYS);

        appNameForm.spacingProperty().bind(scene.widthProperty().multiply(0.015));
        cpuForm.spacingProperty().bind(scene.widthProperty().multiply(0.015));
        gpuForm.spacingProperty().bind(scene.widthProperty().multiply(0.015));
        ramForm.spacingProperty().bind(scene.widthProperty().multiply(0.015));
        storageForm.spacingProperty().bind(scene.widthProperty().multiply(0.015));
        buttonForm.spacingProperty().bind(scene.widthProperty().multiply(0.015));

        appNameField.maxWidthProperty().bind(scene.widthProperty().multiply(0.55));
        cpuField.maxWidthProperty().bind(scene.widthProperty().multiply(0.55));
        gpuField.maxWidthProperty().bind(scene.widthProperty().multiply(0.55));
        ramField.maxWidthProperty().bind(scene.widthProperty().multiply(0.55));
        storageField.maxWidthProperty().bind(scene.widthProperty().multiply(0.55));
        
        inputPanel.translateXProperty().bind(scene.widthProperty().multiply(0.01));


        submitButton.setOnMouseReleased(e ->
        {
            Text currentStatusText = new Text("Confirming input validity...");
            this.currentStatusContainer.getChildren().set(0,currentStatusText);

            String nameData = appNameField.getText();
            String cpuData = cpuField.getText();
            String gpuData = gpuField.getText();
            String ramData = ramField.getText();
            String storageData = storageField.getText();

            Alert requirementAlert = new Alert(AlertType.CONFIRMATION);
            requirementAlert.setTitle("Requirement Confirmation");
            requirementAlert.setHeaderText("Are these correct?");
            ((Button) requirementAlert.getDialogPane().lookupButton(ButtonType.OK)).setText("Yes");
            ((Button) requirementAlert.getDialogPane().lookupButton(ButtonType.CANCEL)).setText("No");
            requirementAlert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE); //Ensures alert window always fits the full text
            requirementAlert.setContentText("Title: " + nameData+"\n"+"CPU: " + cpuData+"\n"+"GPU: " + gpuData+"\n"+"RAM: " + ramData+"\n"+"Storage: " + storageData);
            requirementAlert.showAndWait();

            if (requirementAlert.getResult() == ButtonType.OK)
            {
                currentStatusText = new Text("Validity confirmed. Saving & caching data...");
                this.currentStatusContainer.getChildren().set(0,currentStatusText);

                String[][] dataCollection = new String[4][2];
                dataCollection[0][0] = "CPU";
                dataCollection[1][0] = "GPU";
                dataCollection[2][0] = "RAM";
                dataCollection[3][0] = "Storage";
                dataCollection[0][1] = cpuData;
                dataCollection[1][1] = gpuData;
                dataCollection[2][1] = ramData;
                dataCollection[3][1] = storageData;

                configurator.saveManualApplication(nameData, dataCollection);
                currentStatusText = new Text("Application saved.");
                this.currentStatusContainer.getChildren().set(0,currentStatusText);
                this.applicationCount++;
                Text applicationCountText = new Text("Total Applications Saved: " + this.applicationCount);
                this.totalApplicationCountContainer.getChildren().set(0,applicationCountText);
            }
            else
            {
                currentStatusText = new Text("Input deemed invalid. Waiting further input...");
                this.currentStatusContainer.getChildren().set(0,currentStatusText);
            }
        });

        searchCacheButton.setOnMouseReleased(e ->
        {
            if (appNameField.getText().length() == 0)
            {
                Text currentStatusText = new Text("You must enter an application name first.");
                this.currentStatusContainer.getChildren().set(0,currentStatusText);
            }
            else
            {
                Text currentStatusText = new Text("Searching the cache...");
                this.currentStatusContainer.getChildren().set(0,currentStatusText);
                GenericApplication manualApp = configurator.loadManualApplicationData(appNameField.getText());

                if (manualApp == null)
                {
                    currentStatusText = new Text("No cached items match your search.");
                    this.currentStatusContainer.getChildren().set(0,currentStatusText);
                }
                else
                {
                    Alert requirementAlert = new Alert(AlertType.CONFIRMATION);
                    requirementAlert.setTitle("Requirement Confirmation");
                    requirementAlert.setHeaderText("The following cached data was retrieved. Is it correct?");
                    ((Button) requirementAlert.getDialogPane().lookupButton(ButtonType.OK)).setText("Yes");
                    ((Button) requirementAlert.getDialogPane().lookupButton(ButtonType.CANCEL)).setText("No");
                    requirementAlert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE); //Ensures alert window always fits the full text

                    String alertString = "";
                    for (int i = 0; i < manualApp.reqList.length; i++)
                    {
                        alertString += manualApp.reqList[i][0] + ": " + manualApp.reqList[i][1] + "\n";
                    }

                    requirementAlert.setContentText(alertString);
                    currentStatusText = new Text("Cached data found. Confirming validity...");
                    this.currentStatusContainer.getChildren().set(0,currentStatusText);
                    requirementAlert.showAndWait();
                    
                    if (requirementAlert.getResult() == ButtonType.OK)
                    {
                        currentStatusText = new Text("Data validated! Saving application...");
                        this.currentStatusContainer.getChildren().set(0,currentStatusText);
                        configurator.saveManualApplication(manualApp.name, manualApp.reqList);
                        currentStatusText = new Text("Application saved.");
                        this.currentStatusContainer.getChildren().set(0,currentStatusText);
                        this.applicationCount++;
                        Text applicationCountText = new Text("Total Applications Saved: " + this.applicationCount);
                        this.totalApplicationCountContainer.getChildren().set(0,applicationCountText);
                    }
                    
                }
            }
        });


        appNameForm.getChildren().addAll(appNameText,appNameField);
        cpuForm.getChildren().addAll(cpuText,cpuField);
        gpuForm.getChildren().addAll(gpuText,gpuField);
        ramForm.getChildren().addAll(ramText,ramField);
        storageForm.getChildren().addAll(storageText,storageField);
        buttonForm.getChildren().addAll(searchCacheButton,submitButton);
        inputPanel.getChildren().addAll(appNameForm,cpuForm,gpuForm,ramForm,storageForm,buttonForm);
        return inputPanel;
    }

    public HBox getCurrentStatusContainer()
    {
        return this.currentStatusContainer;
    }

}