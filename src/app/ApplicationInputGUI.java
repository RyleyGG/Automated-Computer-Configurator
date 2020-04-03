//**********************************************************
// Class: ApplicationInputGUI
// Author: Ryley G.
// Date Modified: March 27, 2020
//
// Purpose: Manage the application input window in the GUI
//
// Additional Notes: For testing the Steam API caching, some applications that definitely work include "Garry's Mod" and "Divinity: Original Sin 2"
//************************************************************


//GUI imports
import javafx.scene.Cursor;
import javafx.scene.Scene;
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

//Steam API imports
import pl.l7ssha.javasteam.*;

//General imports
import com.google.gson.JsonSyntaxException;

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

        String[] applicationList =  new String[3];
        applicationList[0] = "Steam Marketplace";
        applicationList[1] = "Website";
        applicationList[2] = "Enter Data Manually";
        ComboBox<String> applicationOptions = new ComboBox<String>(FXCollections.observableArrayList(applicationList));

        //GUI node setup
        preliminaryPanel.spacingProperty().bind(scene.widthProperty().multiply(0.01));
        preliminaryPanel.prefWidthProperty().bind(scene.widthProperty());
        preliminaryPanel.prefHeightProperty().bind(scene.heightProperty().multiply(0.1));
        preliminaryPanel.getChildren().addAll(applicationTypeText,applicationOptions);


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
                VBox websiteApplicationInputPanel = this.createWebsiteInputPanel();
                this.getChildren().set(1,websiteApplicationInputPanel);
            }
            else if (applicationOptions.getValue() == "Enter Data Manually")
            {
                VBox manualApplicationInputPanel = this.createManualInputPanel();
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

        this.totalApplicationCountContainer.getChildren().add(totalApplicationCountText);
        this.currentStatusContainer.getChildren().add(currentStatusText);
        infoPanel.getChildren().addAll(totalApplicationCountContainer,currentStatusContainer);

        this.getChildren().addAll(preliminaryPanel,defaultPanel,infoPanel);
    }

    public VBox createSteamInputPanel(Scene scene, Configurator configurator)
    {
        //Preliminary Steam API setup
        SteamAPI.initialize("8C903FFFA438516B3096134D21E4B43B"); //Web API Key, used to access the Steam API
        this.basicSteamAppData = configurator.parseBasicSteamData(configurator.cacheBasicSteamData()); //Data for nearly all applications across Steam
        String workingDir = System.getProperty("user.dir"); //Review note: On the author's personal machine, Java was not properly finding the CWD, so its explicitly set here

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
                            configurator.cacheSteamAppImage(curatedApplicableApps[i][1],appImageLoc);
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
                            configurator.cacheSteamAppImage(curatedApplicableApps[i][1],appImageLoc);
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
                            //Application count here currently allows for the same application to be selected and incremented more than once -- will be updated later
                            if (configurator.createSteamApplication(curatedApplicableApps[tempIndex]).reqList != null)
                            {
                                this.applicationCount++;
                                Text applicationCountText = new Text("Total Applications Saved: " + this.applicationCount);
                                this.totalApplicationCountContainer.getChildren().set(0,applicationCountText);
                                Text currentStatusText = new Text("Application data saved successfully.");
                                this.currentStatusContainer.getChildren().set(0,currentStatusText);
                            }
                            else if (configurator.loadCachedSteamApplication(curatedApplicableApps[tempIndex][0], curatedApplicableApps[tempIndex][1]) != null)
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

    public VBox createWebsiteInputPanel()
    {
        VBox inputPanel = new VBox();
        Text text = new Text("Website Input Panel!");

        inputPanel.getChildren().add(text);

        return inputPanel;
    }

    public VBox createManualInputPanel()
    {
        VBox inputPanel = new VBox();
        Text text = new Text("Manual Input Panel!");

        inputPanel.getChildren().add(text);

        return inputPanel;
    }
}