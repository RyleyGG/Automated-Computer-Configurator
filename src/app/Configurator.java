//**********************************************************
// Class: Configurator
// Author: Ryley G.
// Date Modified: April 3, 2020
//
// Purpose: Primary connection between the GUI and rest of the model, and handles much of the high-level model activity that includes more than one object type
//
//************************************************************

import pl.l7ssha.javasteam.*;
import java.io.*;
import com.jaunt.JauntException;
import com.jaunt.UserAgent; //Web-scraping

import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;


public class Configurator
{
    private List<GenericApplication> appList = new ArrayList<GenericApplication>();
    private int totalBudget;
    private StoreFrontService storefront = new StoreFrontService();


    public void gatherUserInput()
    {

    }


    //Steam application-related

    public File saveBasicSteamData()
    {
        String workingDir = System.getProperty("user.dir"); //Review note: On the author's personal machine, Java was not properly finding the CWD, so its explicitly set here
        File basicSteamAppData = new File(workingDir + "/cache/basicsteamdata.txt");
        try
        {
            if (basicSteamAppData.exists() == false)
            {
                basicSteamAppData.createNewFile();

                //Using the Jaunt web-scraping library, gather the necessary Steam Marketplace application data
                try
                {
                    UserAgent userAgent = new UserAgent();
                    userAgent.sendGET("https://api.steampowered.com/IStoreService/GetAppList/v1/?format=json&if_modified_since=0&have_description_language=&include_games=1&include_dlc=0&include_hardware=1&include_software=1&include_videos=0&last_appid=0&max_results=100000&key=8C903FFFA438516B3096134D21E4B43B");
                    BufferedWriter output = new BufferedWriter(new FileWriter(basicSteamAppData));
                    
                    output.write(userAgent.json.toString());
                    output.close();
                }
                catch(JauntException e)
                {
                    e.printStackTrace();
                }
                catch(FileNotFoundException e)
                {
                    e.printStackTrace();
                }
                catch(IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return basicSteamAppData;
    }
    
    public SteamApplication createSteamApplication(String[] curApp)
    {                       
        SteamApplication newSteamApp = new SteamApplication(storefront,curApp[0],curApp[1]);
        newSteamApp.gatherRequirements();
        this.appList.add(newSteamApp);

        return newSteamApp;
    }

    public String[][] parseBasicSteamData(File inputFile)
    {
        Scanner scanner;
        String[][] appInfo;
        int totalEntries = 0;

        //Given all of the application data supplied by Steam, narrow each entry down to the application's name and its unique ID.
        //Other data can be gathered at a later point if needed.
        try
        {
            scanner = new Scanner(inputFile);

            //Initializing appInfo
            try
            {
                while (scanner.hasNextLine())
                {
                    if (scanner.next().contains("}"))
                    {
                        totalEntries++;
                    }
                }
            }
            catch (NoSuchElementException e)
            {
            }

            appInfo = new String[totalEntries][2];
            scanner.close();

            Scanner scannerTwo = new Scanner(inputFile);

            //Adding data to appInfo
            for (int x = 0; x < appInfo.length; x++)
            {
                for (int i = 0; i < 6; i++)
                {
                    try
                    {
                        String curLine = scannerTwo.nextLine();

                        if (curLine.contains("\"name\":"))
                        {
                            appInfo[x][0] = curLine.substring(0,curLine.length() - 2).replace("\"name\":\"", "").replace("&amp;","&").trim();
                        }
                        
                        if (curLine.contains("appid"))
                        {
                            appInfo[x][1] = curLine.replace("\"appid\":","").replace(",","").trim();
                        }
                    }
                    catch(NoSuchElementException e)
                    {
                    }
                }
            }

            scannerTwo.close();
            return appInfo;
        }
        catch (FileNotFoundException e)
        {
            System.out.println("FileNotFoundException check");
            return new String[0][0];
        }
    }

    public String loadSteamAppImage(String appID)
    {
        String workingDir = System.getProperty("user.dir");
        File cachedImageList = new File(workingDir + "/cache/steam_app_images.txt");
        
        if (cachedImageList.exists() == false)
        {
            try
            {
                cachedImageList.createNewFile();
            }
            catch (IOException g)
            {
                g.printStackTrace();
            }
        }

        try
        {
            FileReader fr = new FileReader(cachedImageList);
            BufferedReader br = new BufferedReader(fr);

            String curLine = br.readLine();
            while (curLine != null)
            {
                if (curLine.split(":")[0] == appID)
                {
                    br.close();
                    return curLine;
                }
                curLine = br.readLine();
            }
            br.close();
        }
        catch (IOException g)
        {
            g.printStackTrace();
        }

        return "";
    }

    public void saveSteamAppImage(String appID, String appImageLoc)
    {
        String workingDir = System.getProperty("user.dir");
        File cachedImageList = new File(workingDir + "/cache/steam_app_images.txt");
        FileReader fr;
        BufferedReader br;
        FileWriter fw;
        BufferedWriter bw;

        if (cachedImageList.exists() == false)
        {
            try
            {
                cachedImageList.createNewFile();
            }
            catch (IOException g)
            {
                g.printStackTrace();
            }
        }

        try
        {
            fr = new FileReader(cachedImageList);
            br = new BufferedReader(fr);
            fw = new FileWriter(cachedImageList,true);
            bw = new BufferedWriter(fw);
            boolean dataFound = false;

            String curLine = br.readLine();
            while (curLine != null)
            {
                if (curLine.contains(appID))
                {
                    dataFound = true;
                    break;
                }
                curLine = br.readLine();
            }

            if (dataFound == false)
            {
                bw.write(appID + ": " + appImageLoc + "\n");
            }
            br.close();
            bw.close();
        }
        catch (IOException g)
        {
            g.printStackTrace();
        }
    }

    public SteamApplication loadCachedSteamApplication(String appName, String appID)
    {
        String workingDir = System.getProperty("user.dir");
        File cachedApplicationList = new File(workingDir + "/cache/applications/" + appID + ".txt");
        SteamApplication newSteamApp = new SteamApplication(storefront, appName, appID);
        this.appList.add(newSteamApp);
        List<String> appRequirementList = new ArrayList<String>();

        if (cachedApplicationList.exists() == false)
        {
            return null;
        }

        try
        {
            FileReader fr = new FileReader(cachedApplicationList);
            BufferedReader br = new BufferedReader(fr);

            String curLine = br.readLine();
            while (curLine != null)
            {
                appRequirementList.add(curLine);
                curLine = br.readLine();
            }
            br.close();
        }
        catch (IOException g)
        {
            g.printStackTrace();
        }
        
        newSteamApp.parseCachedRequirements(appRequirementList.toArray(new String[0]));
        return newSteamApp;
    }


    //web-scraped application

    public boolean parseWebData(String appName, String webData)
    {
        WebScrapedApplication webApp = new WebScrapedApplication(appName);
        
        if (webApp.gatherRequirements(webData) == true)
        {
            this.appList.add(webApp);
            webApp.saveRequirements();
            return true;
        }

        return false;
    }

    public void loadWebScrapedApplicationData()
    {

    }


    //Product & overall computer build

    public void saveProduct(Product product)
    {

    }

    public void loadProduct(String productID)
    {

    }

    public void filterComponents(String[]... filterOptions)
    {

    }

    public void createProduct()
    {

    }

    public void createComputerBuild()
    {

    }

    public void saveBuilds(String userKey)
    {

    }

    public ComputerBuild[] loadBuilds(String userKey)
    {
        return new ComputerBuild[0];
    }


    //Setters & getters

    public StoreFrontService getStoreFront()
    {
        return this.storefront;
    }
}