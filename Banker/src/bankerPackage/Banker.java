package bankerPackage;

import java.io.*;
import java.util.*;
/**
 * Created by jeffersonvivanco on 11/18/16.
 */
public class Banker {

    public static void main(String[] args)throws IOException{

        Banker banker = new Banker();

        /*
        Reading file from commandline
         */
        FileInputStream input  = null;
        BufferedReader br = null;
        try{
            input  = new FileInputStream(args[0]);
            br = new BufferedReader(new InputStreamReader(input));
        }catch (Exception e){
            System.err.println("File could not be read or could not be found. Please make sure"+
                    " you entered the correct abs path name of your file. Please run again.");
            System.exit(0);
        }
        /*
        Hashmap of resources
         */
        HashMap<Integer, Resource> resourceHashMap = new HashMap<Integer,Resource>();
        /*
        Hashmap of tasks, one for optimistic and the other for banker's algorithm
         */
        HashMap<Integer,Task> taskHashMap = new HashMap<Integer,Task>();
        HashMap<Integer,Task> taskHashMap2 = new HashMap<Integer,Task>();

        /*
        Organizing input into hashmap of tasks and hashmap of resources
         */
        String line = null;
        int lineNum = 1;
        while((line=br.readLine())!=null){
            if(lineNum == 1){
                String[] firstLine = line.split(" ");
                int numOfTasks = Integer.parseInt(firstLine[0]);
                for(int x=1; x<=numOfTasks; x++){
                    Task t = new Task(x);
                    Task t2 = new Task(x);
                    taskHashMap.put(x,t);
                    taskHashMap2.put(x,t2);
                }
                int numOfResources = Integer.parseInt(firstLine[1]);
                int resourceNum = 1;
                for(int i=2; i<firstLine.length; i++){
                    Resource r = new Resource(Integer.parseInt(firstLine[i]));
                    resourceHashMap.put(resourceNum,r);
                    resourceNum++;
                }
            }
            else{
                if(line.matches("(.*)(\\d)(\\s)(\\d)(\\s)(\\d)")){
                    int taskNum = Integer.parseInt(line.split("\\s+")[1]);
                    Task temp = taskHashMap.get(taskNum);
                    Task temp2 = taskHashMap2.get(taskNum);
                    temp2.addActivity(line);
                    temp.addActivity(line);
                }
            }
            lineNum++;
        }

        /* Comment out to display content in resource in file */
//        Set rescSet  = resourceHashMap.entrySet();
//        Iterator rescIterator = rescSet.iterator();
//        while(rescIterator.hasNext()){
//            Map.Entry mentry = (Map.Entry)rescIterator.next();
//            System.out.println("Key: "+mentry.getKey()+" Value: "+mentry.getValue());
//        }
        /* Comment out below to display content in tasks file */
//        Set set = taskHashMap.entrySet();
//        Iterator iterator = set.iterator();
//        while (iterator.hasNext()){
//            Map.Entry mentry = (Map.Entry)iterator.next();
//            System.out.println("Key: "+mentry.getKey()+" Value: "+mentry.getValue());
//        }

        /* Passing hashmaps of tasks and resources to the optimistic manager function and banker's algorithm function.
         * Note: These methods also print the results to the console.
         */
        banker.optimisticResourceManager(taskHashMap,resourceHashMap);
        banker.bankerAlgorithm(taskHashMap2,resourceHashMap);

    }
    /*
    Function imitates a resource manager to do resource allocation.
     */
    public void optimisticResourceManager(HashMap<Integer,Task> taskHashMap, HashMap<Integer, Resource> resourceHashMap){
        /* Queue for tasks that are waiting */
        Queue<Task> taskWaitQ = new LinkedList<Task>();
        /* Hashmap for tasks that have finished */
        HashMap<Integer,Task> finishedTasksHashmap  = new HashMap<Integer,Task>();
        /* Hashmap for resources that are to be released at cycle n+1 */
        HashMap<Integer,Resource> resourceWaitHashMap = new HashMap<Integer,Resource>();
        /* Arraylist for tasks that have been processed in the waitQ and need to be put back in the taskHashMap */
        ArrayList<Task> processedQueuedTasks = new ArrayList<Task>();
        /* Hashmap for tasks that are computing */
        HashMap<Integer,Task> computingTasksMap = new HashMap<Integer, Task>();

        int cycle = 0;//cycle #
        int numOfTasks = taskHashMap.size();
        while(finishedTasksHashmap.size() != numOfTasks){//Note: For final production: !taskHashMap.isEmpty() || !resourceWaitQ.isEmpty()

            boolean isTerminate = false; //boolean to check that if the act is terminate, not to increase cycle
            boolean isDeadLock = false; //boolean to check if it's deadlocked
            boolean didSomethingElseToo = false; //boolean to check that it did another activity besides terminate, so increment cycle.

            /* Checking the resourceWaitQ to see if there are any resources that were released to be released at cycle n+1 */
            if(!resourceWaitHashMap.isEmpty()){
                Set set = resourceWaitHashMap.entrySet();
                Iterator iterator = set.iterator();
                while(iterator.hasNext()){
                    Map.Entry mentry = (Map.Entry)iterator.next();
                    Resource r = (Resource)mentry.getValue();
                    resourceHashMap.get(r.getResource()).addUnits(r.getUnits());
                }
                resourceWaitHashMap.clear();
            }
            /* Checking the taskwaitQ to see if we can satisfy any request of the tasks that are waiting. */
            if(!taskWaitQ.isEmpty()){

                Iterator<Task> iter = taskWaitQ.iterator();
                while(iter.hasNext()){
                    Task t = iter.next();
                    String act = t.seeQueuedAct();
                    String[] actLine = act.split("\\s+");
                    String action = actLine[0];
                    int actTaskNum = Integer.parseInt(actLine[1]);
                    int actResourceNum = Integer.parseInt(actLine[2]);
                    int actResourceUnits = Integer.parseInt(actLine[3]);
                    //If act is request
                    if(action.equals("request")){
                        //If available, grant the request
                        int unitsAvailable = resourceHashMap.get(actResourceNum).getUnits();
                        if(unitsAvailable >= actResourceUnits){
                            t.getQueuedAct();
                            t.incrementWait();
                            int unitsRequested = resourceHashMap.get(actResourceNum).releaseUnits(actResourceUnits);
                            Resource r  = t.getResource(actResourceNum);
                            r.addUnits(unitsRequested);
                            processedQueuedTasks.add(t);
                            iter.remove();
                            didSomethingElseToo = true;
                        }
                        else{
                            t.incrementWait();
                        }
                    }

                }
            }
            /* Going over each process and doing the available activity */
            Set set = taskHashMap.entrySet();
            Iterator iterator = set.iterator();
            while (iterator.hasNext()){
                Map.Entry mentry = (Map.Entry)iterator.next();
                Task temp = (Task)mentry.getValue();

                //Parsing act line of current task
                String activityLine = "";
                if(temp.seeQueuedAct() != null){
                    activityLine = temp.getQueuedAct();
                }
                else{
                    activityLine = temp.getAct();
                }
                String[] actLine = activityLine.split("\\s+");
                String act = actLine[0];
                int actTaskNum = Integer.parseInt(actLine[1]);
                int actResourceNum = Integer.parseInt(actLine[2]);
                int actResourceUnits = Integer.parseInt(actLine[3]);

                /* Activities */
                //If act is initiate
                if(act.equals("initiate")){
                    //Do nothing, optimistic manager doesn't care
                    didSomethingElseToo = true;
                }
                //If act is request
                if(act.equals("request")){
                    //If available, grant the request
                    int unitsRequested = resourceHashMap.get(actResourceNum).releaseUnits(actResourceUnits);
                    if(unitsRequested != -1){
                        Resource r  = temp.getResource(actResourceNum);
                        r.addUnits(unitsRequested);
                    }
                    else{
                        temp.addQueuedAct(activityLine);
                        taskWaitQ.add(temp);
                        iterator.remove();
                    }
                    didSomethingElseToo = true;
                }
                //If act is release
                if(act.equals("release")){
                    //Release it in this cycle, make it available to tasks at the next cycle
                    //Also, here we need to check if any tasks are in the taskwaitQ and if they are, can we satisfy their request with the new release
                    temp.releaseUnitsFromRes(actResourceUnits,actResourceNum);
                    Resource found = resourceWaitHashMap.get(actResourceNum);
                    if(found==null){
                        found = new Resource(actResourceNum, actResourceUnits);
                        resourceWaitHashMap.put(actResourceNum,found);
                    }
                    else{
                        found.addUnits(actResourceUnits);
                    }
                    didSomethingElseToo = true;
                }
                //If act is compute
                if(act.equals("compute")){
                    int numOfCycles = actResourceNum;
                    temp.addComputeCycle(numOfCycles);
                    computingTasksMap.put(actTaskNum,temp);
                    iterator.remove();
                    didSomethingElseToo = true;
                }
                //If act is terminate
                if(act.equals("terminate")){
                    //Terminate task, should be removed from taskHashMap and added to finishedTasksHashmap
                    temp.addTotalTime(cycle);
                    finishedTasksHashmap.put(actTaskNum,temp);
                    iterator.remove();
                    isTerminate = true;
                }
                else{
                    isTerminate = false;
                }
            }
            //Adding processed queued tasks back to taskHashmap
            if(processedQueuedTasks.size()>0){
                for(Task processed : processedQueuedTasks){
                    taskHashMap.put(processed.getTaskNum(),processed);
                }
                processedQueuedTasks.clear();
            }
            /* Checking for deadlock */
            if(taskHashMap.isEmpty() && !taskWaitQ.isEmpty()){
                isDeadLock = true;

                while(isDeadLock){
                    //Getting task with lowest task num and removing it
                    int lowest = 100;
                    Task lowestTask = null;
                    for(Task tl : taskWaitQ){
                        if(tl.getTaskNum() <= lowest){
                            lowest = tl.getTaskNum();
                        }
                    }
                    Iterator<Task> iter = taskWaitQ.iterator();
                    while(iter.hasNext()){
                        Task current = iter.next();
                        if(current.getTaskNum() == lowest){
                            lowestTask = current;
                            iter.remove();
                        }
                    }
                    Task t = lowestTask;
                    //Putting the removed task's resources to the resourcesWaitHashMap, these resources will be put back next cycle.
                    ArrayList<Resource> taskResources = t.getResources();
                    for(Resource r : taskResources){
                        Resource found = resourceWaitHashMap.get(r.getResource());
                        if(found == null){
                            resourceWaitHashMap.put(r.getResource(),r);
                        }
                        else{
                            found.addUnits(r.getUnits());
                        }
                    }
                    t.abort();
                    finishedTasksHashmap.put(t.getTaskNum(),t);
                    //Checking to see if another task in the Q can be granted its request due to the recent release of resources by the aborted task
                    Task peek = taskWaitQ.peek();
                    String act = peek.seeQueuedAct();
                    String[] actLine = act.split("\\s+");
                    String action = actLine[0];
                    int actTaskNum = Integer.parseInt(actLine[1]);
                    int actResourceNum = Integer.parseInt(actLine[2]);
                    int actResourceUnits = Integer.parseInt(actLine[3]);
                    //If act is request
                    if(action.equals("request")){
                        //If available, grant the request
                        int availableResourceUnits = resourceHashMap.get(actResourceNum).getUnits()+resourceWaitHashMap.get(actResourceNum).getUnits();
                        if(availableResourceUnits >= actResourceUnits){
                            isDeadLock = false;
                        }
                        else{
                            isDeadLock = true;
                        }
                    }

                }
            }
            /* Going over the tasks currently computing */
            Set compSet = computingTasksMap.entrySet();
            Iterator compIter = compSet.iterator();
            while(compIter.hasNext()){
                Map.Entry mentry = (Map.Entry)compIter.next();
                Task compT = (Task)mentry.getValue();
                compT.minusComptCycle();
                if(compT.getComputeCycles() == 0){
                    taskHashMap.put(compT.getTaskNum(),compT);
                    compIter.remove();
                }
                didSomethingElseToo = true;
            }
            if(!isTerminate || didSomethingElseToo)
            {
                cycle++; //Incrementing cycle after the activity was done for each task
            }
        }
        /* Calculating final time and displaying info, iterating over finishedTasksHashmap. */
        int totalTime = 0;
        int totalWaitingTime = 0;
        Set set = finishedTasksHashmap.entrySet();
        Iterator finishedMapIt = set.iterator();
        System.out.println("        FIFO        ");
        while(finishedMapIt.hasNext()){
            Map.Entry mentry = (Map.Entry)finishedMapIt.next();
            Task t = (Task)mentry.getValue();
            if(t.isAborted()){
                System.out.println("Task "+t.getTaskNum()+"      "+"aborted");
            }
            else{
                totalTime = totalTime+t.getTotalTime();
                totalWaitingTime = totalWaitingTime + t.getWaitTime();
                double percentTimeWaiting = ((t.getWaitTime()/1.0)/t.getTotalTime())*100;
                System.out.printf("Task %-7d%-3d%-3d%.0f%%\n",t.getTaskNum(),t.getTotalTime(),t.getWaitTime(),percentTimeWaiting);
            }

        }
        double totalPercentTimeWaiting = ((totalWaitingTime/1.0)/totalTime)*100;
        System.out.printf("Total time: %-3d%-3d%.0f%%\n\n",totalTime,totalWaitingTime,totalPercentTimeWaiting);
    }
    public void bankerAlgorithm(HashMap<Integer,Task> taskHashMap, HashMap<Integer,Resource> resourceHashMap){
        /* Hashmap for tasks that have terminated */
        HashMap<Integer, Task> finishedTasksHashmap = new HashMap<Integer, Task>();
        /* Hashmap for resources that are to be released at cycle n+1 */
        HashMap<Integer, Resource> resourceWaitHashMap = new HashMap<Integer, Resource>();
        /* Hashmap to hold tasks that are computing */
        HashMap<Integer, Task> computingTasksMap  = new HashMap<Integer,Task>();
        /* Queue that holds tasks that are waiting. */
        Queue<Task> taskWaitQ = new LinkedList<Task>();
        /* Arraylist for tasks that have been processed in the waitQ and need to be put back in the taskHashMap */
        ArrayList<Task> processedQueuedTasks = new ArrayList<Task>();

        //Checking number of units in resource manager to use later to determine if it's okay to grant requests
        int totalNumOfUnits = 0;
        Set checkSet = resourceHashMap.entrySet();
        Iterator checkIter = checkSet.iterator();
        while(checkIter.hasNext()){
            Map.Entry mentry = (Map.Entry)checkIter.next();
            Resource r = (Resource)mentry.getValue();
            totalNumOfUnits = totalNumOfUnits+r.getUnits();
        }
        int numOfTasks = taskHashMap.size();
        int cycle = 0;
        while(finishedTasksHashmap.size() != numOfTasks){

            boolean isTerminate = false; //boolean to check that if the act is terminate, not to increase cycle
            boolean didSomethingElseToo = false; //boolean to check that even if terminated, did other things too to increase cycle
            boolean hasAllResources = false; //boolean to check if the banker has all resources, if it does, then it will grant the request to the task
            /* Checking the resourceWaitQ to see if there are any resources that were released to be released at cycle n+1 */
            if(!resourceWaitHashMap.isEmpty()){
                Set set = resourceWaitHashMap.entrySet();
                Iterator iterator = set.iterator();
                while(iterator.hasNext()){
                    Map.Entry mentry = (Map.Entry)iterator.next();
                    Resource r = (Resource)mentry.getValue();
                    resourceHashMap.get(r.getResource()).addUnits(r.getUnits());
                }
                resourceWaitHashMap.clear();
            }
            /* Checking if resource manager has all resources, if it does, then it can grant other tasks their requests. */
            Set checkSet2 = resourceHashMap.entrySet();
            Iterator checkIter2 = checkSet2.iterator();
            int totalResourceUnitsFound = 0;
            while(checkIter2.hasNext()){
                Map.Entry mentry = (Map.Entry)checkIter2.next();
                Resource r= (Resource)mentry.getValue();
                totalResourceUnitsFound = totalResourceUnitsFound+r.getUnits();
            }
            if(totalNumOfUnits == totalResourceUnitsFound){
                hasAllResources = true;
            }
            /* Checking the taskwaitQ to see if we can satisfy any request of the tasks that are waiting. */
            if(!taskWaitQ.isEmpty()){

                Iterator<Task> iter = taskWaitQ.iterator();
                while(iter.hasNext()){
                    Task t = iter.next();
                    String act = t.seeQueuedAct();
                    String[] actLine = act.split("\\s+");
                    String action = actLine[0];
                    int actTaskNum = Integer.parseInt(actLine[1]);
                    int actResourceNum = Integer.parseInt(actLine[2]);
                    int actResourceUnits = Integer.parseInt(actLine[3]);
                    //If act is request
                    if(action.equals("request")){
                        //If available, grant the request, and if the banker has all resources
                        int possibleNeeded = t.getResource(actResourceNum).possibleNeededUnits();
                        int unitsAvailable = resourceHashMap.get(actResourceNum).getUnits();
                        if(unitsAvailable >= possibleNeeded && hasAllResources){
                            t.getQueuedAct();
                            t.incrementWait();
                            t.addRequestedUnits(actResourceUnits);
                            int unitsRequested = resourceHashMap.get(actResourceNum).releaseUnits(actResourceUnits);
                            Resource r  = t.getResource(actResourceNum);
                            r.addUnits(unitsRequested);
                            processedQueuedTasks.add(t);
                            iter.remove();
                            didSomethingElseToo = true;
                        }
                        else{
                            t.incrementWait();
                        }
                    }

                }
            }
            /* Going over each process and doing the available activity */
            Set set  = taskHashMap.entrySet();
            Iterator iterator = set.iterator();
            while(iterator.hasNext()){

                Map.Entry mentry = (Map.Entry)iterator.next();
                Task temp = (Task)mentry.getValue();

                //Parsing act line of current task
                String activityLine = "";
                if(temp.seeQueuedAct() != null){
                    activityLine = temp.getQueuedAct();
                }
                else{
                    activityLine = temp.getAct();
                }
                String[] actLine = activityLine.split("\\s+");
                String act = actLine[0];
                int actTaskNum = Integer.parseInt(actLine[1]);
                int actResourceNum = Integer.parseInt(actLine[2]);
                int actResourceUnits = Integer.parseInt(actLine[3]);

                                /* Activities */
                //If act is initiate
                if(act.equals("initiate")){
                    int claim = actResourceUnits;
                    if(claim <= resourceHashMap.get(actResourceNum).getUnits()){
                        temp.getResource(actResourceNum).setInitialClaim(actResourceUnits);
                        temp.addMaxResourceUnits(claim);
                    }
                    else{
                        temp.abort();
                        finishedTasksHashmap.put(actTaskNum,temp);
                        iterator.remove();
                    }
                    didSomethingElseToo = true;
                }
                //If act is request
                if(act.equals("request")){
                    //If resources is greater than claim, abort
                    int totalUnitsSoFar = temp.getResource(actResourceNum).getUnits() + actResourceUnits;
                    if(totalUnitsSoFar > temp.getResource(actResourceNum).getInitialClaim()){
                        temp.abort();
                        ArrayList<Resource> taskResources = temp.getResources();
                        for(Resource r : taskResources){
                            Resource found = resourceWaitHashMap.get(r.getResource());
                            if(found == null){
                                resourceWaitHashMap.put(r.getResource(),r);
                            }
                            else{
                                found.addUnits(r.getUnits());
                            }
                        }
                        finishedTasksHashmap.put(actTaskNum,temp);
                        iterator.remove();
                    }
                    else{
                        //If available, grant the request
                        int possibleNeeded = temp.getResource(actResourceNum).possibleNeededUnits();
                        int availableUnits = resourceHashMap.get(actResourceNum).getUnits();
                        if(possibleNeeded <= availableUnits){
                            int unitsRequested = resourceHashMap.get(actResourceNum).releaseUnits(actResourceUnits);
                            Resource r  = temp.getResource(actResourceNum);
                            r.addUnits(unitsRequested);
                            temp.addRequestedUnits(actResourceUnits);
                        }
                        else{
                            temp.addQueuedAct(activityLine);
                            taskWaitQ.add(temp);
                            iterator.remove();
                        }
                    }
                    didSomethingElseToo = true;
                }
                //If act is release
                if(act.equals("release")){
                    //Release it in this cycle, make it available to tasks at the next cycle
                    //Also, here we need to check if any tasks are in the taskwaitQ and if they are, can we satisfy their request with the new release
                    temp.releaseUnitsFromRes(actResourceUnits,actResourceNum);
                    Resource found = resourceWaitHashMap.get(actResourceNum);
                    if(found==null){
                        found = new Resource(actResourceNum, actResourceUnits);
                        resourceWaitHashMap.put(actResourceNum,found);
                    }
                    else{
                        found.addUnits(actResourceUnits);
                    }
                    didSomethingElseToo = true;
                }
                //If act is compute
                if(act.equals("compute")){
                    int numOfCycles = actResourceNum;
                    temp.addComputeCycle(numOfCycles);
                    computingTasksMap.put(actTaskNum,temp);
                    iterator.remove();
                    didSomethingElseToo = true;
                }
                //If act is terminate
                if(act.equals("terminate")){
                    //Terminate task, should be removed from taskHashMap and added to finishedTasksHashmap
                    temp.addTotalTime(cycle);
                    finishedTasksHashmap.put(actTaskNum,temp);
                    iterator.remove();
                    isTerminate = true;
                }
                else{
                    isTerminate = false;
                }


            }
            //Adding processed queued tasks back to taskHashmap
            if(processedQueuedTasks.size()>0){
                for(Task processed : processedQueuedTasks){
                    taskHashMap.put(processed.getTaskNum(),processed);
                }
                processedQueuedTasks.clear();
            }
            /* Going over the tasks currently computing */
            Set compSet = computingTasksMap.entrySet();
            Iterator compIter = compSet.iterator();
            while(compIter.hasNext()){
                Map.Entry mentry = (Map.Entry)compIter.next();
                Task compT = (Task)mentry.getValue();
                compT.minusComptCycle();
                if(compT.getComputeCycles() == 0){
                    taskHashMap.put(compT.getTaskNum(),compT);
                    compIter.remove();
                }
                didSomethingElseToo = true;
            }
            if(!isTerminate || didSomethingElseToo)
            {
                cycle++; //Incrementing cycle after the activity was done for each task
            }

        }
        /* Calculating final time and displaying info, iterating over finishedTasksHashmap. */
        int totalTime = 0;
        int totalWaitingTime = 0;
        Set set = finishedTasksHashmap.entrySet();
        Iterator finishedMapIt = set.iterator();
        System.out.println("        Banker        ");
        while(finishedMapIt.hasNext()){
            Map.Entry mentry = (Map.Entry)finishedMapIt.next();
            Task t = (Task)mentry.getValue();
            if(t.isAborted()){
                System.out.println("Task "+t.getTaskNum()+"      "+"aborted");
            }
            else{
                totalTime = totalTime+t.getTotalTime();
                totalWaitingTime = totalWaitingTime + t.getWaitTime();
                double percentTimeSpentWaiting = ((t.getWaitTime()/1.0)/t.getTotalTime())*100;
                System.out.printf("Task %-7d%-3d%-3d%.0f%%\n",t.getTaskNum(),t.getTotalTime(),t.getWaitTime(),percentTimeSpentWaiting);
            }

        }
        double percentTimeSpentWaiting = ((totalWaitingTime/1.0)/totalTime)*100;
        System.out.printf("Total time: %-3d%-3d%.0f%%\n",totalTime,totalWaitingTime,percentTimeSpentWaiting);

    }
}
