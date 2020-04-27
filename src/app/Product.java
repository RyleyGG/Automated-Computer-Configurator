//**********************************************************
// Class: Product
// Author: Ryley G.
// Date Modified: April 15, 2020
//
// Purpose: Represents the products that will eventually go into the user's computer build(s).
//
//
//************************************************************


public class Product
{
    private String category;
    private String name;
    private int id;
    private double cost;
    private int performance;
    private double costPerformanceRatio;
    private double griffithCoefficient;

    public Product(String category, String name, int id, double cost, int performance)
    {
        this.category = category;
        this.name = name;
        this.id = id;
        this.cost = cost;
        this.performance = performance;

        this.costPerformanceRatio = performance/cost;
    }

    public void calcGriffithCoefficient()
    {

    }

    //setters and getters

    public String getName()
    {
        return this.name;
    }

    public int getID()
    {
        return this.id;
    }

    public double getCost()
    {
        return this.cost;
    }

    public int getPerformance()
    {
        return this.performance;
    }

    public double getCostPerformanceRatio()
    {
        return this.costPerformanceRatio;
    }

    public double getGriffithCoefficient()
    {
        return this.griffithCoefficient;
    }
    
    public void setGriffithCoefficient(double griffithCoefficient)
    {
        this.griffithCoefficient = griffithCoefficient;
    }
}