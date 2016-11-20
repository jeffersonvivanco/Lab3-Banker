package bankerPackage;

import java.util.*;

/**
 * Created by jeffersonvivanco on 11/18/16.
 */
public class Task {

    private int taskNum;
    private Queue<String> activities;
    private Queue<String> queuedActivities;
    private int waitTime = 0;
    private HashMap<Integer,Resource> resourceHashMap;
    private int totalTime = 0;
    private boolean aborted = false;

    public Task (int taskNum){
        this.taskNum = taskNum;
        this.activities = new LinkedList<String>();
        this.resourceHashMap = new HashMap<Integer,Resource>();
        this.queuedActivities = new LinkedList<String>();
    }
    public void addActivity(String act){
        this.activities.add(act);
    }
    public void incrementWait(){
        this.waitTime++;
    }
    public int getWaitTime(){
        return this.waitTime;
    }
    public Queue<String> getActivities(){
        return this.activities;
    }
    public String getAct(){
        return this.activities.poll();
    }
    public String getQueuedAct(){
        return this.queuedActivities.poll();
    }
    public String seeQueuedAct(){
        return this.queuedActivities.peek();
    }
    public void addQueuedAct(String act){
        this.queuedActivities.add(act);
    }
    public void addTotalTime(int t){
        if(!this.aborted){
            this.totalTime = t;
        }
    }
    public int getTotalTime(){
        return this.totalTime;
    }
    public int getTaskNum(){
        return this.taskNum;
    }
    public void abort(){
        this.aborted = true;
        this.totalTime = 0;
        this.waitTime = 0;
    }
    public boolean isAborted(){
        return this.aborted;
    }
    public ArrayList<Resource> getResources(){
        ArrayList<Resource> resources = new ArrayList<Resource>();
        Set set  = resourceHashMap.entrySet();
        Iterator iterator = set.iterator();
        while(iterator.hasNext()){
            Map.Entry mentry = (Map.Entry)iterator.next();
            resources.add((Resource) mentry.getValue());
        }
        return resources;
    }
    public Resource getResource(int resourceNum){
        Resource r = resourceHashMap.get(resourceNum);
        if(r == null){
            r = new Resource(resourceNum, 0);
            resourceHashMap.put(resourceNum,r);
            return resourceHashMap.get(resourceNum);
        }
        else{
            return resourceHashMap.get(resourceNum);
        }
    }
    public Resource releaseResource(int resourceNum){
        Resource released  = resourceHashMap.remove(resourceNum);
        return released;
    }
    public void releaseUnitsFromRes(int units, int res){
        Resource r = resourceHashMap.get(res);
        r.releaseUnits(units);
    }
    @Override
    public String toString(){
        String string = "Task Num: "+this.taskNum+"\n";
        if(!aborted){
            for(String act : this.activities){
                string = string+act+"\n";
            }
        }
        else{
            string = string +"aborted";
        }

        return string;
    }

}
