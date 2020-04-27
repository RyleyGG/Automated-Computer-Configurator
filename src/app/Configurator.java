//**********************************************************
// Class: Configurator
// Author: Ryley G.
// Date Modified: April 15, 2020
//
// Purpose: Primary connection between the GUI and rest of the model, and handles much of the high-level model activity that includes more than one object type
//
//************************************************************

import pl.l7ssha.javasteam.*;
import java.io.*;
import com.jaunt.JauntException;
import com.jaunt.*;
import com.jaunt.ResponseException;
import com.jaunt.UserAgent; //Web-scraping
import com.jaunt.component.*;

import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;


public class Configurator
{
    //Application-related
    private List<GenericApplication> appList = new ArrayList<GenericApplication>();
    private StoreFrontService storefront = new StoreFrontService();

    //User preferences
    private int userBudget;
    private List<String> selectedConfigs;
    private List<String> selectedArchetypes;

    //Product lists
    private List<Product> gpuList = new ArrayList<Product>();
    private List<Product> cpuList = new ArrayList<Product>();


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
                    //e.printStackTrace();
                }
                catch(FileNotFoundException e)
                {
                    //e.printStackTrace();
                }
                catch(IOException e)
                {
                    //e.printStackTrace();
                }
            }
        }
        catch (IOException e)
        {
            //e.printStackTrace();
        }

        return basicSteamAppData;
    }
    
    public SteamApplication createSteamApplication(String[] curApp)
    {                       
        SteamApplication newSteamApp = new SteamApplication(storefront,curApp[0],curApp[1]);
        newSteamApp.gatherRequirements();

        if (newSteamApp.reqList != null)
        {
            this.appList.add(newSteamApp);
        }

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
                //g.printStackTrace();
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
            //g.printStackTrace();
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
                //g.printStackTrace();
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
            //g.printStackTrace();
        }
    }

    public boolean loadCachedSteamApplication(String appName, String appID)
    {
        String workingDir = System.getProperty("user.dir");
        File cachedApplicationList = new File(workingDir + "/cache/applications/" + appID + ".txt");
        SteamApplication newSteamApp = new SteamApplication(storefront, appName, appID);
        List<String> appRequirementList = new ArrayList<String>();

        if (cachedApplicationList.exists() == false)
        {
            return false;
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
            return false;
        }

        this.appList.add(newSteamApp);
        newSteamApp.parseCachedRequirements(appRequirementList.toArray(new String[0]));
        return true;
    }


    //web-scraped application

    public String[][] parseWebData(String appName, String webData)
    {
        WebScrapedApplication webApp = new WebScrapedApplication(appName);
        
        if (webApp.gatherRequirements(webData) == true)
        {
            return webApp.reqList;
        }

        return null;
    }

    public void saveWebScrapedApplication(String appName, String[][] reqList)
    {
        WebScrapedApplication webApp = new WebScrapedApplication(appName);

        webApp.name = appName;
        webApp.reqList = reqList;
        webApp.saveRequirements(appName);
        this.appList.add(webApp);
    }

    public WebScrapedApplication loadWebScrapedApplicationData(String appName)
    {
        String workingDir = System.getProperty("user.dir");
        File cachedApplicationList = new File(workingDir + "/cache/applications/" + appName + ".txt");
        WebScrapedApplication webApp = new WebScrapedApplication(appName);
        List<String> appRequirementList = new ArrayList<String>();

        if (cachedApplicationList.exists() == false)
        {
            return null;
        }

        this.appList.add(webApp);

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
            //g.printStackTrace();
        }
        
        webApp.parseCachedRequirements(appRequirementList.toArray(new String[0]));
        return webApp;
    }

    
    //manual application

    public void saveManualApplication(String appName, String[][] reqList)
    {
        GenericApplication manualApp = new GenericApplication(appName);
        manualApp.name = appName;
        manualApp.reqList = reqList;
        this.appList.add(manualApp);
        manualApp.saveRequirements(appName);
    }

    public GenericApplication loadManualApplicationData(String appName)
    {
        String workingDir = System.getProperty("user.dir");
        File cachedApplicationList = new File(workingDir + "/cache/applications/" + appName + ".txt");
        GenericApplication manualApp = new GenericApplication(appName);
        List<String> appRequirementList = new ArrayList<String>();

        if (cachedApplicationList.exists() == false)
        {
            return null;
        }

        this.appList.add(manualApp);

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
            //g.printStackTrace();
        }
        
        manualApp.parseCachedRequirements(appRequirementList.toArray(new String[0]));
        return manualApp;
    }


    //Product & overall computer build

    public void filterComponents(String[]... filterOptions)
    {

    }

    public boolean createCPU(String cpuName, String cpuID)
    {
        UserAgent userAgent = new UserAgent();

        //Attempting to dynamically gather CPU cost and performance
        try
        {
            userAgent.sendGET("https://www.cpubenchmark.net/cpu.php?id="+cpuID);

            int cpuPerformance = Integer.parseInt(userAgent.getSource().split("font-weight: bold; color: #F48A18;\">")[1].split("</span>")[0]);
            double cpuCost = Double.parseDouble(userAgent.getSource().split("Last Price Change:")[1].split(" USD")[0].replace("</strong>","").replace("&nbsp;","").replace("$","").replace("<a href=\"#history\">",""));
            Product cpu = new Product("CPU",cpuName,Integer.parseInt(cpuID),cpuCost,cpuPerformance);
            this.cpuList.add(cpu);
            this.cacheCPU(cpu);
            return true;
        }
        catch (ResponseException e) //If data cannot be dynamically gathered, attempt to use cached data
        {
            String workingDir = System.getProperty("user.dir");
            File cachedCPUData = new File(workingDir + "/cache/products/cpu/" + cpuID + ".txt");
            try
            {
                FileReader fr = new FileReader(cachedCPUData);  
                BufferedReader br = new BufferedReader(fr);
                String curLine = "";
                int cpuPerformance = -1;
                double cpuCost = -1;

                while ((curLine = br.readLine()) != null)
                {
                    if (curLine.contains("Cost"))
                    {
                        cpuCost = Double.parseDouble(curLine.split(": ")[1]);
                    }
                    else if (curLine.contains("Performance"))
                    {
                        cpuPerformance = Integer.parseInt(curLine.split(": ")[1]);
                    }
                }
                br.close();

                if (cpuCost > 0 && cpuPerformance > 0)
                {
                    Product cpu = new Product("CPU",cpuName,Integer.parseInt(cpuID),cpuCost,cpuPerformance);
                    this.cpuList.add(cpu);
                    return true;
                }
                else
                {
                    return false;
                }
            }
            catch (FileNotFoundException f)
            { 
            }
            catch (IOException f)
            {
            }
        }
        catch(NumberFormatException e)
        {
        }

        return false;
    }

    public void cacheCPU(Product cpu)
    {
        String workingDir = System.getProperty("user.dir");
        File cachedCPUData = new File(workingDir + "/cache/products/cpu/" + cpu.getID() + ".txt");

        if (cachedCPUData.exists() == false)
        {
            try
            {
                cachedCPUData.createNewFile();
            }
            catch (IOException e)
            {
            }
        }

        try
        {
            FileWriter fw = new FileWriter(cachedCPUData);
            BufferedWriter bw = new BufferedWriter(fw);

            bw.write("Name: " + cpu.getName() + "\n");
            bw.write("ID: " + cpu.getID() + "\n");
            bw.write("Cost: " + cpu.getCost() + "\n");
            bw.write("Performance: " + cpu.getPerformance() + "\n");
            bw.close();
        }
        catch (IOException e)
        {
        }
    }

    public String getCPUSocket(int id)
    {
        UserAgent userAgent = new UserAgent();
        
        try
        {
            userAgent.sendGET("https://www.cpubenchmark.net/cpu.php?id="+id);
            String cpuSocketVal = userAgent.getSource();
            cpuSocketVal = cpuSocketVal.split("Socket:")[1].split("</p>")[0].replace("</strong>","").replace("  "," ").trim();
            
            return cpuSocketVal;
        }
        catch (ResponseException e)
        {
        }

        return null;
    }

    public boolean createGPU(String gpuName, String gpuID)
    {
        UserAgent userAgent = new UserAgent();

        //Attempting to dynamically gather GPU cost and performance
        try
        {
            userAgent.sendGET("https://www.videocardbenchmark.net/gpu.php?id="+gpuID);

            int gpuPerformance = Integer.parseInt(userAgent.getSource().split("font-weight: bold; color: #F48A18;\">")[1].split("</span>")[0]);
            double gpuCost = Double.parseDouble(userAgent.getSource().split("Last Price Change:")[1].split(" USD")[0].replace("</strong>","").replace("&nbsp;","").replace("$",""));

            Product gpu = new Product("GPU",gpuName,Integer.parseInt(gpuID), gpuCost,gpuPerformance);
            this.gpuList.add(gpu);
            this.cacheGPU(gpu);
            return true;
        }
        catch (ResponseException e) //If data is unable to be dynamically gathered, attempt to use cached data.
        {
            String workingDir = System.getProperty("user.dir");
            File cachedGPUData = new File(workingDir + "/cache/products/gpu/" + gpuID + ".txt");
            try
            {
                FileReader fr = new FileReader(cachedGPUData);  
                BufferedReader br = new BufferedReader(fr);
                String curLine = "";
                int gpuPerformance = -1;
                double gpuCost = -1;

                while ((curLine = br.readLine()) != null)
                {
                    if (curLine.contains("Cost"))
                    {
                        gpuCost = Double.parseDouble(curLine.split(": ")[1]);
                    }
                    else if (curLine.contains("Performance"))
                    {
                        gpuPerformance = Integer.parseInt(curLine.split(": ")[1]);
                    }
                }
                br.close()
;                
                if (gpuCost > 0 && gpuPerformance > 0)
                {
                    Product gpu = new Product("GPU",gpuName,Integer.parseInt(gpuID),gpuCost,gpuPerformance);
                    this.gpuList.add(gpu);
                    return true;
                }
                else
                {
                    return false;
                }
            }
            catch (FileNotFoundException f)
            { 
            }
            catch (IOException f)
            {
            }
        }
        catch(NumberFormatException e)
        {
        }

        return false;
    }

    public void cacheGPU(Product gpu)
    {
        String workingDir = System.getProperty("user.dir");
        File cachedGPUData = new File(workingDir + "/cache/products/gpu/" + gpu.getID() + ".txt");

        if (cachedGPUData.exists() == false)
        {
            try
            {
                cachedGPUData.createNewFile();
            }
            catch (IOException e)
            {
            }
        }

        try
        {
            FileWriter fw = new FileWriter(cachedGPUData);
            BufferedWriter bw = new BufferedWriter(fw);

            bw.write("Name: " + gpu.getName() + "\n");
            bw.write("ID: " + gpu.getID() + "\n");
            bw.write("Cost: " + gpu.getCost() + "\n");
            bw.write("Performance: " + gpu.getPerformance() + "\n");
            bw.close();
        }
        catch (IOException e)
        {
        }
    }

    public void cacheHardwareData()
    {
        //This method will go to the repository of data for the benchmark software that is used to measure performance for this program (PassMark software)
        //It will pull a list of all hardware and their associated ID's.
        UserAgent userAgent = new UserAgent();

        //CPU
        try
        {
            userAgent.sendGET("https://www.cpubenchmark.net/cpu_list.php");
            String[] rawCPUData = userAgent.getSource().split("<tr"); //Currently the GPU's are organized within <TR> tags
            String[] cpuNames = new String[rawCPUData.length];
            int[] cpuIDs = new int[rawCPUData.length];

            for (int i = 0; i < rawCPUData.length; i++)
            {
                String curCPUData = rawCPUData[i].split("</tr>")[0];

                try
                {
                    cpuIDs[i] = Integer.parseInt(curCPUData.split(">")[0].replace("\"","").replace("id=","").replace("cpu","").trim());
                    cpuNames[i] = curCPUData.split("id="+cpuIDs[i]+"\">")[1].replace("<td>","").replace("</td>","").split("</a>")[0].trim();
                }              
                catch (ArrayIndexOutOfBoundsException e)
                {
                }  
                catch (NumberFormatException e)
                {
                    //This will only occur on parts of the HTML that aren't wanted anyway
                }
            }

            String workingDir = System.getProperty("user.dir"); //Review note: On the author's personal machine, Java was not properly finding the CWD, so its explicitly set here
            File cachedCPUData = new File(workingDir + "/cache/cpu_set.txt");
            if (cachedCPUData.exists() == false && cpuNames.length > 0)
            {
                try
                {
                    cachedCPUData.createNewFile();
                }
                catch (IOException g)
                {
                    //g.printStackTrace();
                }
            }
    
            try
            {
                FileWriter writer = new FileWriter(cachedCPUData);
                
                for (int i = 0; i < cpuIDs.length; i++)
                {
                    if (cpuIDs[i] != 0 && cpuNames[i] != null)
                    {
                        writer.write(cpuIDs[i] + ": " + cpuNames[i] + "\n");
                    }
                }
                
                writer.close();
            }
            catch (IOException g)
            {
                //g.printStackTrace();
            }
        }
        catch (ResponseException e)
        {
        }

        //GPU
        try
        {
            userAgent.sendGET("https://www.videocardbenchmark.net/gpu_list.php");
            String[] rawGPUData = userAgent.getSource().split("<TR"); //Currently the GPU's are organized within <TR> tags
            String[] gpuNames = new String[rawGPUData.length];
            int[] gpuIDs = new int[rawGPUData.length];

            for (int i = 0; i < rawGPUData.length; i++)
            {
                String curGPUData = rawGPUData[i].split("</TR>")[0];

                try
                {
                    gpuIDs[i] = Integer.parseInt(curGPUData.split(">")[0].replace("\"","").replace("id=","").replace("gpu","").trim());
                    gpuNames[i] = curGPUData.split("id="+gpuIDs[i]+"\">")[1].replace("<TD>","").replace("</TD>","").split("</A>")[0].trim();
                }              
                catch (ArrayIndexOutOfBoundsException e)
                {
                }  
                catch (NumberFormatException e)
                {
                    //This will only occur on parts of the HTML that aren't wanted anyway
                }
            }

            String workingDir = System.getProperty("user.dir"); //Review note: On the author's personal machine, Java was not properly finding the CWD, so its explicitly set here
            File cachedGPUData = new File(workingDir + "/cache/gpu_set.txt");
            if (cachedGPUData.exists() == false && gpuNames.length > 0)
            {
                try
                {
                    cachedGPUData.createNewFile();
                }
                catch (IOException g)
                {
                    //g.printStackTrace();
                }
            }
    
            try
            {
                FileWriter writer = new FileWriter(cachedGPUData);
                
                for (int i = 0; i < gpuIDs.length; i++)
                {
                    if (gpuIDs[i] != 0)
                    {
                        writer.write(gpuIDs[i] + ": " + gpuNames[i] + "\n");
                    }
                }
                
                writer.close();
            }
            catch (IOException g)
            {
                //g.printStackTrace();
            }
        }
        catch (ResponseException e)
        {
        }
    }

    public Product createMotherboard(String cpuSocket)
    {
        UserAgent userAgent = new UserAgent();
        String socketID = "";
        //Firstly, finding the CPU socket constraint to ensure compatibility
        try
        {
            userAgent.sendGET("https://motherboarddb.com/motherboards/?search=&market=c");
            String dirtySocketIDList = userAgent.getSource().split("<select name=\"socket\" class=\"select2-widget form-control\" id=\"id_socket\" multiple=\"multiple\">")[1].split("</select>")[0];
            for (int i = 0; i < dirtySocketIDList.split("<option").length; i++)
            {
                if (dirtySocketIDList.split("<option")[i].contains(cpuSocket) || dirtySocketIDList.split("<option")[i].contains(cpuSocket.replace("LGA", "LGA ")))
                {
                    socketID = dirtySocketIDList.split("<option")[i].split("value=\"")[1].split("\"")[0];
                    break;
                }
            }
        }
        catch (ResponseException e)
        {
        }

        //Then, finding a motherboard (filtered by popularity, first item chosen)
        try
        {
            userAgent.visit("https://motherboarddb.com/advanced-search/");
            Document doc = userAgent.doc;
            Form form = doc.getForm(2);

            try
            {
                doc.chooseMenuItem("Sockets:", cpuSocket.replace("LGA","LGA ").replace("  "," "));
                form.submit();           
                String motherboardPage = userAgent.getSource().split("<a href=\"")[1].split("\"")[0];
                userAgent.visit("https://motherboarddb.com" + motherboardPage);
               
                Product motherboard = new Product("Motherboard", userAgent.doc.findFirst("<title>").getChildText().split(" - Motherboard Specifications")[0], Integer.parseInt(motherboardPage.split("motherboards/")[1].split("/")[0]), 0, 0);
                return motherboard;
            }
            catch (MultipleFound e)
            {
            }
            catch (NotFound e)
            {
            }
        }
        catch (ResponseException e)
        {
        }

        return null;
    }

    public String[] parseMotherboardSpecs(int ID)
    {
        UserAgent userAgent = new UserAgent();
        String[] returnArr = new String[2];
        try
        {
            userAgent.visit("https://motherboarddb.com/motherboards/"+ID);
            if (userAgent.getSource().contains("DDR4"))
            {
                returnArr[0] = "DDR4";
            }
            else
            {
                returnArr[1] = "DDR3";
            }

            if (userAgent.getSource().contains("M.2"))
            {
                returnArr[1] = "M.2";
            }
            else
            {
                returnArr[1] = "SATA";
            }

        }
        catch (ResponseException e)
        {
        }

        return returnArr;
    }

    public void createComputerBuild()
    {
        for (int i = 0; i < this.selectedConfigs.size(); i++)
        {
            ComputerBuild computerBuild = new ComputerBuild(this, this.selectedConfigs.get(i), this.cpuList.toArray(new Product[0]), this.gpuList.toArray(new Product[0]), this.selectedArchetypes.toArray(new String[0]));
        }
    }

    public void saveBuilds(String userKey)
    {

    }

    public ComputerBuild[] loadBuilds(String userKey)
    {
        return new ComputerBuild[0];
    }

    public void generateGriffithCoefficients()
    {
        //CPU's
        for (int x = 0; x < this.cpuList.size(); x++)
        {
            int cpuPerformance = this.cpuList.get(x).getPerformance();
            int adequatePerformanceCount = 0; //Count of how many user-submitted applications the CPU could manage performance-wise

            for (int y = 0; y < this.appList.size(); y++)
            {
                if (cpuPerformance >= this.appList.get(y).getCPUPerformance())
                {
                    adequatePerformanceCount++;
                }
            }

            this.cpuList.get(x).setGriffithCoefficient((adequatePerformanceCount/this.appList.size()) * this.cpuList.get(x).getCostPerformanceRatio());
        }

        //GPU's
        for (int x = 0; x < this.gpuList.size(); x++)
        {
            int gpuPerformance = this.gpuList.get(x).getPerformance();
            int adequatePerformanceCount = 0; //Count of how many user-submitted applications the GPU could manage performance-wise

            for (int y = 0; y < this.appList.size(); y++)
            {
                if (gpuPerformance >= this.appList.get(y).getGPUPerformance())
                {
                    adequatePerformanceCount++;
                }
            }

            this.gpuList.get(x).setGriffithCoefficient((adequatePerformanceCount/this.appList.size()) * this.gpuList.get(x).getCostPerformanceRatio());
        }


        //Ordering the lists of CPU's and GPU's from highest to lowest Griffith Coefficients. The following uses a reverse bubble sorting algorithm.

        int notSortedCheck = 0;
        while (notSortedCheck != this.cpuList.size())
        {
            notSortedCheck = 0;
            for (int i = 0; i < this.cpuList.size(); i++)
            {

                if ((i + 1 < this.cpuList.size()) && (this.cpuList.get(i+1).getGriffithCoefficient() > this.cpuList.get(i).getGriffithCoefficient()))
                {
                    Product temp = this.cpuList.get(i);
                    this.cpuList.set(i,this.cpuList.get(i+1));
                    this.cpuList.set(i + 1, temp);
                }
                else
                {
                    notSortedCheck++;
                }
            }
        }

        notSortedCheck = 0;
        while (notSortedCheck != this.gpuList.size())
        {
            notSortedCheck = 0;
            for (int i = 0; i < this.gpuList.size(); i++)
            {

                if ((i + 1 < this.gpuList.size()) && (this.gpuList.get(i+1).getGriffithCoefficient() > this.gpuList.get(i).getGriffithCoefficient()))
                {
                    Product temp = this.gpuList.get(i);
                    this.gpuList.set(i,this.gpuList.get(i+1));
                    this.gpuList.set(i + 1, temp);
                }
                else
                {
                    notSortedCheck++;
                }
            }
        }
    }

    //Setters & getters

    public StoreFrontService getStoreFront()
    {
        return this.storefront;
    }

    public void addToAppList(GenericApplication webApp)
    {
        this.appList.add(webApp);
    }

    public List<GenericApplication> getAppList()
    {
        return this.appList;
    }

    public void setUserBudget(int budget)
    {
        this.userBudget = budget;
    }

    public void setSelectedConfigs(List<String> selectedConfigs)
    {
        this.selectedConfigs = selectedConfigs;
    }

    public void setSelectedArchetypes(List<String> selectedArchetypes)
    {
        this.selectedArchetypes = selectedArchetypes;
    }

    public List<Product> getGPUList()
    {
        return this.gpuList;
    }

    public List<Product> getCPUList()
    {
        return this.cpuList;
    }

    public int getUserBudget()
    {
        return this.userBudget;
    }
}