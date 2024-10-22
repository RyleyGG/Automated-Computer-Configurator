//**********************************************************
// Class: BuildGenerationGUI
// Author: Ryley G.
// Date Modified: April 15, 2020
//
// Purpose: Conveys the progress the application has made in regards to grabbing product information
//
//
//************************************************************

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.Region;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.Node;


class BuildGenerationGUI extends VBox
{
    protected List<String[][]> gpuList = new ArrayList<String[][]>();
    protected List<String[][]> cpuList = new ArrayList<String[][]>();
    VBox buildGenerationGUI = this;

    public BuildGenerationGUI(Scene scene, Configurator configurator)
    {
        GenericApplication[] appList = configurator.getAppList().toArray(new GenericApplication[0]);
        String workingDir = System.getProperty("user.dir"); //Review note: On the author's personal machine, Java was not properly finding the CWD, so its explicitly set here

        
        new Thread(() ->
        {
            File cachedGPUData = new File(workingDir + "/cache/gpu_set.txt");
            File cachedCPUData = new File(workingDir + "/cache/cpu_set.txt");

            //GUI node creation
            Text mainStepText = new Text("Preparing to check cached product data...");
            Text smallStepText = new Text("Loading...");
            Button continueButton = new Button("Continue");

            Platform.runLater(new Runnable()
            {
                @Override
                public void run()
                {
                    //GUI node setup
                    buildGenerationGUI.setAlignment(Pos.BASELINE_CENTER);
                    buildGenerationGUI.spacingProperty().bind(scene.heightProperty().multiply(0.007));
                    buildGenerationGUI.prefWidthProperty().bind(scene.widthProperty());
                    buildGenerationGUI.prefHeightProperty().bind(scene.heightProperty());
                    mainStepText.translateYProperty().bind(scene.heightProperty().multiply(0.45));
                    smallStepText.translateYProperty().bind(scene.heightProperty().multiply(0.45));
                    smallStepText.setStroke(Color.GRAY);
                    continueButton.setDisable(true);
                    continueButton.setVisible(false);
                    continueButton.setId("productContinueButton");
                    buildGenerationGUI.getChildren().addAll(mainStepText,smallStepText,continueButton);
                }
            });
            
            boolean cachedDataMissing = true;
            while (cachedDataMissing == true)
            {
                configurator.cacheHardwareData();

                if (cachedCPUData.exists() && cachedGPUData.exists())
                {
                    cachedDataMissing = false;
                    
                    //CPU
                    try
                    {
                        this.updateGUI(mainStepText, smallStepText, "Processing CPU data", "Preparing...");
                        smallStepText.setText("Cached CPU data found. Parsing now...");
                        this.updateGUI(mainStepText, smallStepText, null, "Cached CPU data found. Parsing now...");

                        FileReader fr = new FileReader(cachedCPUData);
                        BufferedReader br = new BufferedReader(fr);
                        String curLine = "";
                        while ((curLine = br.readLine()) != null)
                        {
                            this.updateGUI(mainStepText, smallStepText, null, "Data from file: " + curLine);
                            String[][] tempCPUArr = new String[1][2];
                            tempCPUArr[0][0] = curLine.split(": ")[1];
                            tempCPUArr[0][1] = curLine.split(":")[0];
                            this.cpuList.add(tempCPUArr);
                        }
                        br.close();
                        this.updateGUI(mainStepText, smallStepText, null, "");

                        //Creating the GUI representations themselves & gathering the data
                        //For each application, attempt to find its associted CPU requirement within the cached CPU list.
                        for (int x = 0; x < appList.length; x++)
                        {
                            this.updateGUI(mainStepText, smallStepText, "Generating deep CPU data for " + appList[x].getName(), "Preparing...");

                            //Finding the CPU name for the current application
                            String curAppCPUName = "ABCDEFGHIJK";
                            for (int n = 0; n < appList[x].getReqList().length; n++)
                            {
                                if (appList[x].getReqList()[n][0].equals("Processor"))
                                {
                                    curAppCPUName = appList[x].getReqList()[n][1];
                                    curAppCPUName = curAppCPUName.replace("i3 ", "i3-").replace("i5 ","i5-").replace("i7 ","i7-").replace("i9 ","i9-");
                                    smallStepText.setText("Cached name data found: " + curAppCPUName);
                                    this.updateGUI(mainStepText, smallStepText, null, "Cached name data found: " + curAppCPUName);
                                }
                            }

                            int cpusChecked = 0; //It was found that developer's will often leave generic CPU requirements for their applications, leading to sometimes hundreds of valid CPU's for a single application. An arbitrary limit has been imposed to ensure a balance been thoroughness and speed.
                            List<String> tempCPUNameList = new ArrayList<String>();
                            for (int y = 0; y < this.cpuList.size(); y++)
                            {
                                //The replacements are there because their inclusion tends to be very inconsistent across different mediums, so if they were kept in there would be a lot of false negatives.
                                if ((cpusChecked <= 25) && (this.cpuList.get(y)[0][0].toLowerCase().replace("intel","").replace("amd","").trim().contains(curAppCPUName.toLowerCase().replace("intel","").replace("amd","").trim()) || curAppCPUName.toLowerCase().replace("intel","").replace("amd","").trim().contains(this.cpuList.get(y)[0][0].toLowerCase().replace("intel","").replace("amd","").trim())))
                                {
                                    this.updateGUI(mainStepText, smallStepText, null, "CPU data paired (" + this.cpuList.get(y)[0][0] + ")");
                                    configurator.createCPU(this.cpuList.get(y)[0][0],this.cpuList.get(y)[0][1]);
                                    tempCPUNameList.add(this.cpuList.get(y)[0][0]);
                                    cpusChecked++;
                                }
                            }

                            //Once all the CPU's for the current application have been entered, iterate through the CPU's and assign the average CPU performance needed for an application to a field
                            double totalCPUPerformance = 0;
                            for (int n = 0; n < tempCPUNameList.size(); n++)
                            {
                                for (int y = 0; y < configurator.getCPUList().size(); y++)
                                {
                                    if (configurator.getCPUList().get(y).getName().contains(tempCPUNameList.get(n)))
                                    {
                                        totalCPUPerformance += configurator.getCPUList().get(y).getPerformance();
                                    }
                                }
                            }

                            if (totalCPUPerformance != 0)
                            {
                                configurator.getAppList().get(x).setCPUPerformance(totalCPUPerformance/cpusChecked);
                            }
                        }
                        
                    }
                    catch (FileNotFoundException e)
                    {
                    }
                    catch (IOException e)
                    {
                        //e.printStackTrace();
                    }

                    //GPU
                    try
                    {
                        this.updateGUI(mainStepText, smallStepText, "Processing GPU data", "Preparing...");

                        if (cachedGPUData.exists())
                        {
                            FileReader fr = new FileReader(cachedGPUData);
                            BufferedReader br = new BufferedReader(fr);
                            String curLine = "";
                            while ((curLine = br.readLine()) != null)
                            {
                                this.updateGUI(mainStepText, smallStepText, null, "Data from file: " + curLine);
                                String[][] tempGPUArr = new String[1][2];
                                tempGPUArr[0][0] = curLine.split(": ")[1];
                                tempGPUArr[0][1] = curLine.split(":")[0];
                                this.gpuList.add(tempGPUArr);
                            }
                            br.close();
                            this.updateGUI(mainStepText, smallStepText, null, " ");

                            //Creating the GUI representations themselves & gathering the data
                            //For each application, attempt to find its associted GPU requirement within the cached GPU list.
                            for (int x = 0; x < appList.length; x++)
                            {
                                this.updateGUI(mainStepText, smallStepText, "Generating deep GPU data for " + appList[x].getName(), null);

                                //Finding the GPU name for the current application
                                String curAppGPUName = "ABCDEFGHIJK";
                                for (int n = 0; n < appList[x].getReqList().length; n++)
                                {
                                    if (appList[x].getReqList()[n][0].equals("Graphics"))
                                    {
                                        curAppGPUName = appList[x].getReqList()[n][1];
                                        this.updateGUI(mainStepText, smallStepText, null, "Cached name data found: " + curAppGPUName);
                                    }
                                }

                                int gpusChecked = 0;
                                List<String> tempGPUNameList = new ArrayList<String>();
                                for (int y = 0; y < this.gpuList.size(); y++)
                                {
                                    //The replacements are there because their inclusion tends to be very inconsistent across different mediums, so if they were kept in there would be a lot of false negatives.
                                    if ((gpusChecked <= 25) && this.gpuList.get(y)[0][0].toLowerCase().replace("nvidia","").replace("amd","").replace("geforce","").trim().contains(curAppGPUName.toLowerCase().replace("nvidia","").replace("amd","").replace("geforce","").trim()) || curAppGPUName.toLowerCase().replace("nvidia","").replace("amd","").replace("geforce","").trim().contains(this.gpuList.get(y)[0][0].toLowerCase().replace("nvidia","").replace("amd","").replace("geforce","").trim()))
                                    {
                                        this.updateGUI(mainStepText, smallStepText, null, "GPU data paired (" + this.gpuList.get(y)[0][0] + ")");
                                        configurator.createGPU(this.gpuList.get(y)[0][0],this.gpuList.get(y)[0][1]);
                                        tempGPUNameList.add(this.gpuList.get(y)[0][0]);
                                        gpusChecked++;
                                    }
                                }

                                //Once all the GPU's for the current application have been entered, iterate through the GPU's and assign the average GPU performance needed for an application to a field
                                int totalGPUPerformance = 0;
                                for (int n = 0; n < tempGPUNameList.size(); n++)
                                {
                                    for (int y = 0; y < configurator.getGPUList().size(); y++)
                                    {
                                        if (configurator.getGPUList().get(y).getName().contains(tempGPUNameList.get(n)))
                                        {
                                            totalGPUPerformance += configurator.getGPUList().get(y).getPerformance();
                                        }
                                    }
                                }

                                if (totalGPUPerformance != 0)
                                {
                                    configurator.getAppList().get(x).setGPUPerformance(totalGPUPerformance/gpusChecked);
                                }
                            }
                        }
                    }
                    catch (FileNotFoundException e)
                    {
                    }
                    catch (IOException e)
                    {
                        //e.printStackTrace();
                    }

                    this.updateGUI(mainStepText, smallStepText, "Creating Computer Builds", "Generating Griffith Coefficients...");
                    configurator.generateGriffithCoefficients();
                    this.updateGUI(mainStepText, smallStepText, "Creating Computer Builds", "Generating your computer build(s)...");
                    configurator.createComputerBuild();


                    /*
                    The performance conscious checks should be like this:

                    if the set of products is above budget, and the ratio in price (newBuild/oldBuild) is less than the ratio in performance (newBuild/oldBuild)
                    */

                    this.updateGUI(mainStepText, smallStepText, "Done!", "Press the button below to view your computer build(s)!");
                    Platform.runLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            continueButton.setDisable(false);
                            continueButton.setVisible(true);
                        }
                    });

                    continueButton.setOnMouseReleased(h ->
                    {
                        System.out.println("Check");
                    });
                }
                else
                {
                    Alert warningAlert = new Alert(AlertType.INFORMATION);                                
                    warningAlert.setTitle("Action needed");
                    warningAlert.setHeaderText("Warning: necessary cached data missing");
                    warningAlert.getDialogPane().setContentText("You are missing data that is critical to the program. This data was not previously cached and was not able to be gathered by the program. Please ensure you have a working internet connection and exit this window to try again.");
                    warningAlert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE); //Ensures alert window always fits the full text
                    warningAlert.showAndWait();
                }
            }
        }).start();
    }

    public void updateGUI(Text mainTextNode, Text smallTextNode, String mainText, String smallText)
    {
        new Thread(() ->
        {
            Platform.runLater(new Runnable()
            {
                @Override
                public void run()
                {
                    if (mainText != null)
                    {
                        mainTextNode.setText(mainText);
                    }

                    if (smallText != null)
                    {
                        smallTextNode.setText(smallText);
                    }
                }
            });
        }).start();
    }
}