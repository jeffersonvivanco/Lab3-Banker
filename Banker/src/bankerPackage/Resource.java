package bankerPackage;

/**
 * Created by jeffersonvivanco on 11/18/16.
 */
public class Resource {

    private int resource;
    private int units;
    private static int resourceNumber = 1;

    public Resource (int resNum, int u){
        this.resource = resNum;
        this.units = u;
    }
    public Resource (int u){
        this.resource = resourceNumber;
        this.units = u;
        this.resourceNumber++;
    }

    public int getResource(){
        return this.resource;
    }
    public int getUnits(){
        return this.units;
    }
    public void addUnits(int numUnits){
        this.units = this.units+numUnits;
    }
    public int releaseUnits(int numUnits){
        if(numUnits <= this.units){
            this.units = this.units - numUnits;
            return numUnits;
        }
        else{
            return -1;
        }
    }
    @Override
    public String toString(){
        String string = "";
        string = "Resource: "+this.resource+" Units: "+this.units;
        return string;
    }
}
