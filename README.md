# Lab3-Banker
The goal of this lab is to do resource allocation using both an optimistic resource manager and the banker’s algorithm of Dijkstra.
To run this program, follow the following steps. 
1) Download the repository
2) Open Terminal 
3) Navigate to where you saved the project, and go into Banker and then src. 
4) Once you are in the src folder in Terminal, do the following command javac bankerPackage/Banker.java 
5) And then do java bankerPackage.Banker _Name of Input_ Note:Absolute path of name of input, inputs included in repo. 
6) And thats it, you should see the results of the optimistic manager and the banker, enjoy! 

Sample run: 
cd src/
javac bankerPackage/Banker.java 
java bankerPackage.Banker /Users/jeffersonvivanco/IdeaProjects/Lab3-Banker/inputs/input-07.txt 

        FIFO      
Task 1      aborted
Task 2      aborted
Task 3      7  3  43%
Total time: 7  3  43%

        Banker      
Task 1      9  4  44%
Task 2      aborted
Task 3      7  3  43%
Total time: 16 7  44%

