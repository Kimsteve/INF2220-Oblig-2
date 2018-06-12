	
import java.util.*;
import java.io.*;
import java.util.Arrays;
import java.io.PrintWriter;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

class Oblig2 {

	public static void main(String [] args  ) {
		
		String path = args[0]; // input file
		//Check if the args are 2 as staed in the assignment specifications.
		if(args.length < 2 ) {
			System.out.println("The correct arguement input should be be java Oblig2 <inputfile> <manpower>");	
			System.exit(0);	
		} 			
		//create project.	
		Project p  = new Project(path);
		p.read();

		//check if the task is realizable and traverse if so.
		if(p.realizable()) {
			p.traverse();
			p.criticalTask();
			p.printproject();
		}
	}
}

class Project {
	Task [] tsk; // holds the tasks
	Task [] task2;
	int executionTime;
	String path;
	Project(String path){
		this.path = path;
	}

	//read file.
	void read () {
		try {
			Scanner br = new Scanner (new FileReader(path));
			int taskSize = br.nextInt();  // get the number of task
			tsk = new Task [taskSize];

			//create all tasks.
			for(int i = 0; i < tsk.length; i++){
			tsk[i] = new Task(this);	
			}
			
			// read other contents of file and assign to taskInfo of each task.
			int taskCount  = 0;
			while (br.hasNext()){
			String task = br.nextLine();
			if(task.length()==0 || task.charAt(0)== ' ') continue;
			tsk[taskCount++].taskInfo = task;
						
		 	} 
		
		} catch (IOException e) {
		
		}

		// assign each taks its details.
		for (int i = 0;i< tsk.length; i++) {
			tsk[i].assign();	
		}
	}

	void criticalTask (){
		
		LinkedList<Task> temp = new LinkedList<Task>();

		for (int i = task2.length-1; i>=0;i-- ) {
			Task tempTask = task2[i];
			//check if stop time is equal to maximum exectution time.
			if(tempTask.stop()==executionTime){
				temp.add(tempTask); 
				tempTask.critical = true; // set this task as critical.
				//assign the slack for the task with equal stop and maximum execution time.(last)
				tempTask.slack = tempTask.stop()- executionTime;
			}
			
		}
		//loop through temp and check if the preceding task has equal stop time with the current task in
		// temp. if so it is added to temp and set as a critical tasks.

		while(temp.size()>0){
			Task tempTask = temp.remove();
			for (Task tempTask2: tempTask.waitsfor ) {

				if(tempTask.started==tempTask2.stop() && !tempTask2.critical){
					temp.add(tempTask2);
					tempTask2.critical = true; 
					
				} else{
					//this task are thus non critical and calculate the slack and assign to
					//specific tasks.
					int slack = tempTask.started - tempTask2.stop();
					tempTask2.slack = slack;
				}
			}
		}

	}

	//printing the project details.
	void printproject(){
		System.out.println();
		System.out.println("Print task details");
		System.out.println("id " + "\t"+ "time "  + "staff " + "L.startime  " 
			+ " E.starttime " + " slack   " + " dependancies "  +"        name" );

		for (int i = 0;i<tsk.length;i++ ) {
		   System.out.printf("%d\t %d\t %d\t %d\t %d\t \t %d\t %s\t  \t %s\t\n", tsk[i].id, tsk[i].time, tsk[i].staff,
			tsk[i].getLatestStart(), tsk[i].stop(), tsk[i].slack, Arrays.toString(tsk[i].getdependanciesArray()), tsk[i].name );
		}	

	}
	//loop thro all task and check if it is acyclic by
	//calling the checkCycle method 
    public boolean realizable() {
		for(int i = 0; i <tsk.length; i++) {
			if(!checkCycle(tsk[i]))
				return false;
		}
		return true;
	}

	private boolean checkCycle(Task checkCyc) {
		
		if(checkCyc.checkloop == -1) 
		return true;
		
		// A cycle is found and then print the nodes in the cycle. 	
		if(checkCyc.checkloop == 1) {
			printCycle(checkCyc);
			return false;
		}

		// if non of the above is fulfilled, set the checkCyc to 1 and 
		//check its dependancies. iterate through the dependancies and check
		// if cycle is found by recursion.
		checkCyc.checkloop = 1;
		for(int i = 0; i < checkCyc.dependencies.size(); i++) {
			Task next = checkCyc.dependencies.get(i);
			next.previous = checkCyc;
			if(!checkCycle(next))
				return false;
		}
		checkCyc.checkloop = -1;
		return true;
	}


	public void traverse (){
		LinkedList<Task>  travers = new LinkedList<Task>();
		task2  = new Task[tsk.length];
		//loop thro the task array and add the task with no dependancies to the linkedlist
		for(int i=0;i<tsk.length;i++) {

			if(tsk[i].numberOfdep == 0)
				travers.add(tsk[i]);
		}

		int totaltime = calTime(travers);
		System.out.println("**** Project's shortest execution time is  " + totaltime + "  ****");
	} 


	public int calTime(LinkedList<Task> travers ){
		//initializing values that will be used in the traverse process.
		int  time       = 0;
		int  manpowerpool      = 0;
		int  counter2       = 0;
		System.out.println("Time: "+time);

		LinkedList<Task> tempTraverse = new LinkedList<Task>(); // temporarily holds the task with zero
		// dependancies.
		//first loop thro task with no dependancies or tempTraverse is is >0
		while(travers.size() > 0 || tempTraverse.size() > 0) {
			ListIterator<Task> iterate;
			Task tempTask;
			//get all the task in travers and assign the start time and staff
			//add them to tempTraverse
			//print the task.
			while(travers.size() > 0) {
				tempTask            = travers.remove();
				manpowerpool         += tempTask.staff;
				tempTask.started    = time;
				
				task2[counter2++] = tempTask;
				tempTraverse.add(tempTask); // add this to tempTraverse.
				System.out.println("    Starting task: "+tempTask.id);
			}
			//iterate thro all task in tempTraverse and
			//assign mintime to stop time of the specific task.
			iterate = tempTraverse.listIterator();
			int mintime = -1;
			while(iterate.hasNext()) {
				tempTask = iterate.next();

				if(mintime < 0 || tempTask.stop() < mintime) {
					mintime = tempTask.stop();
				}
			}

			iterate = tempTraverse.listIterator();
			System.out.println("    Current Staff: "+manpowerpool);
			time = mintime;
			System.out.println("Time: "+time);
			while(iterate.hasNext()) {
				tempTask = iterate.next();
				if(tempTask.stop() != time) continue;

				//
				for(int i = 0; i < tempTask.dependencies.size(); i++) {
					tempTask.dependencies.get(i).numberOfdep--;
					if(tempTask.dependencies.get(i).numberOfdep <= 0) {
						// add this task to travers which contains task with zero depedancies
						//as the number of dependancies reduces.
						travers.add(tempTask.dependencies.get(i));
					}
				}
				System.out.println("    Finished task: "+tempTask.id);
				manpowerpool -= tempTask.staff; // subtract the staff of the completed task from the pool of staff.
				iterate.remove(); // the last task returned by next() is removed from tempTraverse.
			}
		}
		executionTime = time;
		return time;
	}


	private void printCycle(Task check) {
		LinkedList<Task> cyclic = new LinkedList<Task>();
		Task cyc = check;
		System.out.println("ERROR: a cycle  is found");
		System.out.println("printing all the nodes in the cyclic path");
		
		
		cyclic.push(cyc); //Push the first task to the list thus becoming start point for the cycle.
		do {
			check = check.previous;
			cyclic.add(check); //appends the task to the end of the list.

		} while(check != cyc);

		for(Task prTask : cyclic){
			System.out.print(prTask.getName() + " (" + prTask.getId() + ")" + " -> ");
		}
		System.out.println();
	}	 	
}




class Task {

	//tasks details
	int id, time, staff;
	String name;
	
	ArrayList<Task> waitsfor; // holds the preceding task of the current task
	ArrayList<Task> dependencies; // task depending on the current task.

	Project proj; 
	String taskInfo; //string holding details read from file.

	int numberOfdep;
	int slack;
	int started =-1;
	int checkloop =0;
	boolean critical = false;
	Task previous = null; 

	Task(Project proj){	
		this.proj = proj;
		waitsfor = new ArrayList<Task>();
		dependencies = new ArrayList<Task>();
		name      = null;
		id        = -1;
		numberOfdep = waitsfor.size();
		reset();
	}

	//split the string read from file and assign to the task details.
	public void assign(){
		String [] taskDetails = taskInfo.split("[\\s]+");

		id = Integer.parseInt(taskDetails[0]);
		name = taskDetails[1];
		time = Integer.parseInt(taskDetails[2]);
		staff = Integer.parseInt(taskDetails[3]);
		setDependancies(taskDetails);
	}

	// set the dependancies of the task
	//precedence and dependancies.
	public void setDependancies(String [] taskDetails){

		for (int i = 4; i<taskDetails.length ;i++ ) {
			 int dependency = Integer.parseInt(taskDetails[i]);
			 if(dependency == 0) continue;
                this.waitsfor.add(proj.tsk[dependency-1]); // set preceding task
                proj.tsk[dependency-1].dependencies.add(this);	 // set dependancies.
		}

		for(int i = 0; i < proj.tsk.length; i++) {
			proj.tsk[i].reset(); // reset dependencies length.
		}

	}

	public void reset() {
		numberOfdep = waitsfor.size(); // get the right number of preceding the tasks.
	}


	public int stop() {
		return time + started;
	}



	// the following methods are the getters. 
	public int numberOfdependencies(){
		return waitsfor.size();
	}
	public int numberOfd(){
		return dependencies.size();
	}
	public int getId() {
		return id;
	}

	public String getName() {
		return name;

	}

	public int getTime() {
		return time;
	}


	public int getStaff() {
		return staff;
	}
	public int getEarliestStart() {
		int temp = stop();

		return temp;
	}
	

	public int getLatestStart() {
		int temp = stop();
		 return stop() + slack;
		//return latestStart;
	}

	//store dependencies in array to help when printing taks details.
	public int [] getdependanciesArray (){
		int [] arr = new int [dependencies.size()];
		for(int i = 0; i < dependencies.size(); i++){
			arr[i] = dependencies.get(i).id;
		}
		return arr;
	}

}
