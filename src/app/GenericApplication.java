import java.io.*;

public class GenericApplication
{
    protected String name;
    protected String[][] reqList;

    public GenericApplication(String name)
    {
        this.name = name;
    }

    public GenericApplication()
    {
    }

    public void gatherRequirements()
    {
        System.out.println("gatherRequirements method was called, but is currently blank");
    }

    public void saveRequirements(String identifier)
    {
        String workingDir = System.getProperty("user.dir"); //Review note: On the author's personal machine, Java was not properly finding the CWD, so its explicitly set here
        File cachedRequirements = new File(workingDir + "/cache/applications/" + identifier + ".txt");
        if (cachedRequirements.exists() == false)
        {
            try
            {
                cachedRequirements.createNewFile();
            }
            catch (IOException g)
            {
                g.printStackTrace();
            }
        }

        try
        {
            FileWriter writer = new FileWriter(cachedRequirements);
            FileReader reader = new FileReader(cachedRequirements);
            BufferedReader br = new BufferedReader(reader);
            
            writer.write("Title: " + this.name + "\n");
            for (int x = 0; x < this.reqList.length; x++)
            {
                String curLine = br.readLine();
                this.reqList[x][0] = this.reqList[x][0].trim();
                this.reqList[x][1] = this.reqList[x][1].trim();
                if (this.reqList[x][0].length() != 0 && (this.reqList[x][0] + ": " + this.reqList[x][1] != curLine || curLine == "Title: " + this.name))
                {
                    writer.write(this.reqList[x][0] + ": " + this.reqList[x][1] +"\n");
                }
            }
            writer.close();
            br.close();
        }
        catch (IOException g)
        {
            g.printStackTrace();
        }
    }

    public void parseCachedRequirements(String[] appRequirementList)
    {
        int validRequirementCounter = 0;

        for (int x = 0; x < appRequirementList.length; x++)
        {
            if (appRequirementList[x].length() != 0 && appRequirementList[x] != null && appRequirementList[x].contains(":") && appRequirementList[x].contains("Additional Notes:") == false)
            {
                validRequirementCounter++;
            }
        }
        this.reqList = new String[validRequirementCounter][2];

        int lastRequirementEntered = -1;
        for (int x = 0; x < reqList.length; x++)
        {
            for (int y = lastRequirementEntered+1; y < appRequirementList.length; y++)
            {
                if (appRequirementList[y].length() != 0 && appRequirementList[y] != null && appRequirementList[y].contains(":") && appRequirementList[y].contains("Additional Notes:") == false)
                {
                    this.reqList[x] = appRequirementList[y].split(":",2);
                    lastRequirementEntered = y;
                    break;
                }
            }
        }
    }

    //Setters & getters

    public String getName()
    {
        return this.name;
    }

    public String[][] getReqList()
    {
        return this.reqList;
    }
}