//**********************************************************
// Class: ProductGUI
// Author: Ryley G.
// Date Modified: April 13, 2020
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

import javafx.scene.Scene;
import javafx.scene.layout.VBox;


class ProductGUI extends VBox
{
    List<String[][]> gpuList = new ArrayList<String[][]>();

    public ProductGUI(Scene scene, Configurator configurator)
    {
        GenericApplication[] appList = configurator.getAppList().toArray(new GenericApplication[0]);

        configurator.cacheHardwareData();

        String workingDir = System.getProperty("user.dir"); //Review note: On the author's personal machine, Java was not properly finding the CWD, so its explicitly set here
        
        //GPU
        try
        {
            File cachedGPUData = new File(workingDir + "/cache/gpu_set.txt");

            if (cachedGPUData.exists())
            {
                FileReader fr = new FileReader(cachedGPUData);
                BufferedReader br = new BufferedReader(fr);
                String curLine = "";
                while ((curLine = br.readLine()) != null)
                {
                    String[][] tempGPUArr = new String[1][2];
                    tempGPUArr[0][0] = curLine.split(": ")[1];
                    tempGPUArr[0][1] = curLine.split(":")[0];
                    this.gpuList.add(tempGPUArr);
                }
                br.close();

                //Creating the GUI representations themselves & gathering the data
                //For each application, attempt to find its associted GPU requirement within the cached GPU list.
                for (int x = 0; x < appList.length; x++)
                {
                    //Finding the GPU name for the current application

                    String curAppGPUName = "ABCDEFGHIJK";
                    for (int n = 0; n < appList[x].getReqList().length; n++)
                    {
                        if (appList[x].getReqList()[n][0].equals("Graphics"))
                        {
                            curAppGPUName = appList[x].getReqList()[n][1];
                        }
                    }

                    for (int y = 0; y < this.gpuList.size(); y++)
                    {
                        //The replacements are there because their inclusion tends to be very inconsistent across different mediums, so if they were kept in there would be a lot of false negatives.
                        if (this.gpuList.get(y)[0][0].toLowerCase().replace("nvidia","").replace("amd","").replace("geforce","").trim().contains(curAppGPUName.toLowerCase().replace("nvidia","").replace("amd","").replace("geforce","").trim()) || curAppGPUName.toLowerCase().replace("nvidia","").replace("amd","").replace("geforce","").trim().contains(this.gpuList.get(y)[0][0].toLowerCase().replace("nvidia","").replace("amd","").replace("geforce","").trim()))
                        {
                            configurator.createGPU(this.gpuList.get(y)[0][0],this.gpuList.get(y)[0][1]);
                        }
                    }
                }
            }
        }
        catch (FileNotFoundException e)
        {
            //This means that the GPU data was not previously cached and was not able to be gathered newly. What should be done in this case?
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        //Getting CPU data ready


    }
}