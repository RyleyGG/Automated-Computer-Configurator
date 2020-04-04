import com.jaunt.JauntException;
import com.jaunt.UserAgent;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

public class WebScrapedApplication extends GenericApplication
{
    WebScrapedApplication(String name)
    {
        this.name = name;
    }

    public Boolean gatherRequirements(String webData)
    {
        //Multiple methods to attempt to gather data from raw HTML:
        //1a. Assume <li> tags are used to organize requirement data.
        //1b. Look for <li> tags, then look for keywords and pull appropriately.

        //<li> tag method
        String[] splitWebData = webData.split("<li");
        boolean htmlTagCheck = false;
        String extractedRequirement = ""; //The requirement after it's been parsed from the HTML data
        String temp = ""; //Temporary holder of potentially valid requirement information during the parsing process
        int index = 0; //Multiple loops need to use the same index
        List<String[]> requirements = new ArrayList<String[]>();
        
        String[] keyTerms = new String[11];
        keyTerms[0] = "cpu:";
        keyTerms[1] = "processor:";
        keyTerms[2] = "processing unit:";
        keyTerms[3] = "graphics card:";
        keyTerms[4] = "gpu:";
        keyTerms[5] = "graphics processor:";
        keyTerms[6] = "ram:";
        keyTerms[7] = "memory:";
        keyTerms[8] = "storage:";
        keyTerms[9] = "harddrive:";
        keyTerms[10] = "hard drive:";
        //here create list of valid terms to look for. Once term has been used, remove it so same requiement is not grabbed twice

        //Grabs the data from the HTML and parses the data out from the HTML
        for (int y = 0; y < keyTerms.length; y++)
        {
            for (int i = 0; i < splitWebData.length; i++)
            {
                splitWebData[i] = splitWebData[i].split("</li>")[0];

                if (splitWebData[i].toLowerCase().contains(keyTerms[y]))
                {
                    while (index < splitWebData[i].toCharArray().length-1)
                    {
                        while (htmlTagCheck == false)
                        {
                            for (int x = index; x < splitWebData[i].toCharArray().length; x++)
                            {
                                if (splitWebData[i].toCharArray()[x] == '<')
                                {
                                    htmlTagCheck = true;
                                    index++;
                                    break;
                                }
                                else if (splitWebData[i].toCharArray()[x] == '>' && splitWebData[i].indexOf("<") > x)
                                {
                                    temp = "";
                                }
                                else
                                {
                                    temp += splitWebData[i].toCharArray()[x];
                                }
                                
                                index++;
                            }
                        }

                        while (htmlTagCheck == true)
                        {
                            for (int x = index; x < splitWebData[i].toCharArray().length; x++)
                            {
                                if (splitWebData[i].toCharArray()[x] == '>')
                                {
                                    htmlTagCheck = false;
                                    index++;
                                    break;
                                }
                                
                                index++;
                            }
                            htmlTagCheck = false;
                        }
                    }

                    extractedRequirement = temp;
                    
                    //System.out.println(Arrays.toString(extractedRequirement.split(":")));

                    String[] tempArr = new String[2];
                    tempArr[0] = extractedRequirement.split(":")[0];
                    tempArr[1] = extractedRequirement.split(":")[1];
                    requirements.add(tempArr);
                    y++; //Only want each key term to be used one, so force update here
                }
            }

            index = 0;
            temp = "";
        }

        this.reqList = new String[requirements.size()][2];
        for (int n = 0; n < requirements.size(); n++)
        {
            this.reqList[n][0] = requirements.get(n)[0];
            this.reqList[n][1] = requirements.get(n)[1];
        }

        System.out.println(Arrays.deepToString(this.reqList));
        return false;
    }
}