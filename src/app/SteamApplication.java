//**********************************************************
// Class: SteamApplication
// Author: Ryley G.
// Date Modified: March 27, 2020
//
// Purpose: Represents an application gathered by using the Steam API, and handles gathering the related system requirements.
//
//************************************************************

import java.io.*;
import pl.l7ssha.javasteam.*;

public class SteamApplication extends GenericApplication
{
    private StoreFrontService storefront;
    private int appID;

    public SteamApplication(StoreFrontService storefront, String appName, String appID)
    {
        this.storefront = storefront;
        this.name = appName;
        this.appID = Integer.parseInt(appID);
    }

    @Override
    public void gatherRequirements()
    {
        String appRequirements = storefront.getFullInfoOfApp(Integer.toString(this.appID)).getPcRequirements().getRecommended();

        if (appRequirements != null)
        {
            appRequirements = appRequirements.replace("</strong>","").replace("<li>","").replace("</li>","").replace("<br>","").replace("</ul>","").replace("Recommended:<ul class=\"bb_ul\">","").trim();
            String[] appRequirementList = appRequirements.split("<strong>");
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

            this.saveRequirements(Integer.toString(this.appID));
        }
    }
}