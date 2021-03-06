package haxm.core;

import haxm.LogFile;
import haxm.VirtState;
import haxm.VirtStateEnum;
import haxm.components.CloudRegistry;
import haxm.holders.EntityHolder;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class CloudVirt{
	
	/**A CUTE LITTLE VERSION NUMBER AS A STRING :) ^_^*/
	public static  final String CLOUDVIRT_VERSION_STRING = "1.0";
	
	/**A CloudRegistry variable that has the information regarding the cloud resources*/
	public static CloudRegistry cloudRegistry;
	
	/**An event queue to hold the events*/
	public static EventQueue globalQueue;
	
	/**A list of simulation entities*/	
	private static EntityHolder entityHolder;
	
	/**A State object to maintain the state of simulation*/	
	private static VirtState simulationState;
	
	/**A time variable to represent clock time of the simulation*/
	private  static double currentTime;
	
	public static LogFile mainLog;
	public static LogFile eventsLog;
	public static LogFile entityLog;
	
	public static void initSimulationEnvironment(){
		
		globalQueue =  new EventQueue();
		entityHolder = new EntityHolder();
		simulationState = new VirtState(VirtStateEnum.INVALID);
		currentTime = 0.00;
		
		mainLog = new LogFile("log.txt");
		eventsLog = new LogFile("events.txt");
		entityLog = new LogFile("entity.txt");
		
		cloudRegistry = new CloudRegistry("Cloud Registry 1");
		
	}
	
	public static void setCurrentTime(double time){
		currentTime = time;
	}

	public static double getCurrentTime(){
		return currentTime;
	}
	
	public static void writeLog(LogFile logFile, String message){
		if(logFile != null){
			logFile.append(message);
		}
		mainLog.append(message);
	}
	
	/** Method to start the simulation*/
	public static double startSimulation(){
		double duration;
		writeLog(null, "=========================SIMULATION STARTED=========================");
		
		duration = simulate();
		
		writeLog(null, "=========================SIMULATION ENDED=========================");
		
		
		finishSimulation();
		
		
		
		return duration;
	}

	
	private static void finishSimulation() {
		invalidateSimulationEnvironment();		
	}
	

	private static void invalidateSimulationEnvironment() {
		cloudRegistry = null;
		globalQueue =  null;
		entityHolder = null;
		simulationState = null;
		currentTime = -1;
		
		mainLog.close();
		mainLog = null;
		
		eventsLog.close();
		eventsLog = null;
		
		entityLog.close();
		entityLog = null;
	}

	
	private static double simulate() {
		if(!isRunning()){
			simulationState.setState(VirtStateEnum.RUNNING);
			entityHolder.startEntities();
		}
		while( (!toTerminate()) && nextTick()){
		}
		return getCurrentTime();		
	}

	
	

	
	
	private static boolean isRunning() {
		// TODO Auto-generated method stub
		if(simulationState.getState() == VirtStateEnum.RUNNING){
			return true;
		}else{	
			return false;
		}	
	}

	private static boolean nextTick() {
		
		entityHolder.runAll();
		
		if(globalQueue.empty()){
			return false;
		}else{
			boolean processmore = true;
			VirtEvent currentEvent = globalQueue.extract();
			VirtEvent firstEvent = currentEvent;
			
			do{
				processEvent(currentEvent);
				if(globalQueue.empty()){
					processmore = false;
				}else{
					currentEvent = globalQueue.extract();
					if(firstEvent.getTime() != currentEvent.getTime()){
						processmore = false;
					}
				}
			}while(processmore);
			return (!globalQueue.empty());
		}
	}

	private static void processEvent(VirtEvent currentEvent) {
		setCurrentTime(currentEvent.getTime());
		switch(currentEvent.getType()){
			case INVALID:
				break;
			case SEND:
				int destinationId = currentEvent.getDestinationId();
				VirtEntity destinationEntity = entityHolder.getEntityByID(destinationId);
				destinationEntity.addToLocalQueue(currentEvent);
				break;
		}
	}

	private static boolean toTerminate() {
		// TODO Auto-generated method stub
		return false;
	}

	/** Method to stop the simulation*/
	public static boolean stopSimulation(){
		System.out.println("shutdown");
		return true;
	}

	/** Method to pause the simulation*/
	public static boolean pauseSimulation(){
		return true;
	}

	/** Method to abruptly terminate the simulation*/
	public static boolean abruptlyTerminateSimulation(){
		return true;
	}
	
	public static void addEntity(VirtEntity entity){
		entityHolder.addEntity(entity);
	}
}


