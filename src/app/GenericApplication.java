public class GenericApplication
{
    protected String name;
    protected String[][] reqList;

    public void gatherRequirements()
    {
        System.out.println("gatherRequirements method was called, but is currently blank");
    }

    public void saveRequirements()
    {
        
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
}