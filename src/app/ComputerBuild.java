import java.util.ArrayList;
import java.util.List;

public class ComputerBuild
{
    private double cost;
    private String type;
    private List<Product> productList = new ArrayList<Product>();
    private Product cpu;
    private Product gpu;
    private Product RAM;
    private Product[] storageList;
    private Product powerSupply;
    private Product cpuCooler;
    private Product motherboard;

    public ComputerBuild(Configurator configurator, String type, Product[] cpuList, Product[] gpuList, String[] selectedArchetypes)
    { 
        this.type = type;

        if (this.type == "Optimal configuration")
        {
            this.cpu = cpuList[0];
            this.gpu = gpuList[0];
            this.productList.add(this.cpu);
            this.productList.add(this.gpu);

            /* Some notes regarding build creation:
            * The CPU and GPU are directly chosen based on the applications the user submits.
                Other parts of the PC, such as the storage solution, are harder to objectively measure overall.
                Therefore, the user's selected archetypes will play an important factor in determining how these products are chosen.
            * The power supply is currently hard-coded in.
                This is primarily because there doesn't seem to be a reliable way to access power usage data for most products.
                The product itself is of good quality and the 750W standard should be more than enough for all except the most extreme edge cases.
            */


            //Hard-coding the power supply and CPU cooler
            this.powerSupply = new Product("Power Supply", "Corsair CX750M", 104.99);
            this.productList.add(this.powerSupply);
            this.cpuCooler = new Product("CPU Cooler", "Cryorig H7", 89.99); //Make sure bracket is correct
            this.productList.add(this.cpuCooler);

            //Finding and creating the motherboard
            String cpuSocket = configurator.getCPUSocket(this.cpu.getID());
            this.motherboard = configurator.createMotherboard(cpuSocket);
            this.productList.add(motherboard);
            
            String[] motherboardSpecifications = configurator.parseMotherboardSpecs(this.motherboard.getID());


            //Creating the storage solution
            this.storageList = new Product[2];
            if (motherboardSpecifications[1] == "M.2")
            {
                storageList[0] = new Product("Storage","XPG SX8200 Pro PCIe NVMe Gen3 x4 M.2", 104.99);
            }
            else
            {
                storageList[0] = new Product("Storage","ADATA Ultimate SU800 512GB", 64.99);
            }
            
            this.productList.add(storageList[0]);

            for (int i = 0; i < selectedArchetypes.length; i++)
            {
                if ((selectedArchetypes[i].equals("Need lots of storage") || selectedArchetypes[i].equals("Content Creator")) && storageList[1] == null)
                {
                    storageList[1] = new Product("Storage", "Seagate FireCuda 2TB Solid State Hybrid Drive", 99.99);
                    this.productList.add(storageList[1]);
                }

                if (selectedArchetypes[i].equals("Hardcore Gamer") && storageList[1] == null)
                {
                    storageList[1] = new Product("Storage", "ADATA SU760 1TB 3D NAND 2.5 Inch SATA III Internal SSD", 102.99);
                    this.productList.add(storageList[1]);
                }
            }


            //Creating the RAM solution
            if (motherboardSpecifications[0] == "DDR3")
            {
                this.RAM = new Product("RAM", "HyperX FURY 16GB (2 x 8GB)", 86.27);
            }
            else
            {
                this.RAM = new Product("RAM", "G.SKILL TridentZ RGB Series 16 GB (2 x 8GB)", 94.89);
            }
            this.productList.add(this.RAM);

            //Once all products have been added, determine the total cost and then budget as appropriate
            double totalCost = 0;
            for (int i = 0; i < this.productList.size(); i++)
            {
                totalCost += this.productList.get(i).getCost();
            }
            this.cost = totalCost;


            if (this.cost > configurator.getUserBudget())
            {
                this.budgetCorrection(configurator, cpuSocket); //Checking for changes that need to occur as a result of budget issues
            }
        }
    }

    public void budgetCorrection(Configurator configurator, String cpuSocket)
    {
        for (int i = 0; i < this.productList.size(); i++)
        {
            if (this.productList.get(i).getName() == "Seagate FireCuda 2TB Solid State Hybrid Drive")
            {
                this.productList.set(i, new Product("Storage", "Seagate FireCuda 1TB Solid State Hybrid Drive", 70.99));
                this.cost -= 29; 
                break;
            }
            else if (this.productList.get(i).getName() == "ADATA SU760 1TB 3D NAND 2.5 Inch SATA III Internal SSD")
            {
                this.productList.set(i, new Product("Storage","ADATA Ultimate SU800 512GB", 64.99));
                this.cost -= 38;
                break;
            }
        }

        if (this.cost > configurator.getUserBudget())
        {
            for (int i = 0; i < this.productList.size(); i++)
            {
                if (this.productList.get(i).getName() == "Cryorig H7" && (cpuSocket.replace("LGA ", "LGA") == "LGA1151") || cpuSocket == "AM4")
                {
                    this.productList.set(i, new Product("CPU Cooler", "Cooler Master Hyper 212 Evo", 34.99));
                    this.cost -= 55;
                }
            }
        }

        //If the build is still over-budget at this point, iterate through the CPU/GPU lists and downgrade by the least amounts possible, to a certain extent.
        int cpuCompromiseCounter = 0;
        int gpuCompromiseCounter = 0;
        while (this.cost > configurator.getUserBudget() && cpuCompromiseCounter < configurator.getCPUList().size()/2 && gpuCompromiseCounter < configurator.getGPUList().size()/2)
        {
            if (configurator.getCPUList().get(cpuCompromiseCounter).getGriffithCoefficient() - configurator.getCPUList().get(cpuCompromiseCounter + 1).getGriffithCoefficient() < configurator.getGPUList().get(gpuCompromiseCounter).getGriffithCoefficient() - configurator.getGPUList().get(gpuCompromiseCounter + 1).getGriffithCoefficient())
            {
                this.cpu = configurator.getCPUList().get(cpuCompromiseCounter + 1);
                cpuCompromiseCounter++;
            }
            else
            {
                this.gpu = configurator.getGPUList().get(gpuCompromiseCounter + 1);
                gpuCompromiseCounter++;
            }
        } 
        
        this.productList.set(0, this.cpu);
        this.productList.set(1, this.gpu);
    }


    //Setters & getters

    public void setCPU(Product cpu)
    {
        this.cpu = cpu;
    }

    public void setGPU(Product gpu)
    {
        this.gpu = gpu;
    }

    public void setRAM(Product RAM)
    {
        this.RAM = RAM;
    }
}