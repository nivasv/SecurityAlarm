/******************************************************************************************************************
* File:ECSConsole.java
* Course: 17655
* Project: Assignment 3
* Copyright: Copyright (c) 2009 Carnegie Mellon University
* Versions:
*	1.0 February 2009 - Initial rewrite of original assignment 3 (ajl).
*
* Description: This class is the console for the museum environmental control system. This process consists of two
* threads. The ECSMonitor object is a thread that is started that is responsible for the monitoring and control of
* the museum environmental systems. The main thread provides a text interface for the user to change the temperature
* and humidity ranges, as well as shut down the system.
*
* Parameters: None
*
* Internal Methods: None
*
******************************************************************************************************************/
import TermioPackage.*;
import EventPackage.*;

public class DoorSensorConsole
{
	public static void main(String args[])
	{
    	Termio UserInput = new Termio();	// Termio IO Object
		boolean Done = false;				// Main loop flag
		String Option = null;				// Menu choice from user
		Event Evt = null;					// Event object
		boolean Error = false;				// Error flag
		DoorSensor Sensor = null;			// The Security monitor system Monitor

		/////////////////////////////////////////////////////////////////////////////////
		// Get the IP address of the event manager
		/////////////////////////////////////////////////////////////////////////////////

 		if ( args.length != 0 )
 		{
			// event manager is not on the local system

			Sensor = new DoorSensor( args[0] );

		} else {

			Sensor = new DoorSensor();

		} // if


		// Here we check to see if registration worked. If ef is null then the
		// event manager interface was not properly created.

		if (Sensor.IsRegistered() )
		{
			Sensor.start(); // Here we start the monitoring and control thread

			while (!Done)
			{
				// Here, the main thread continues and provides the main menu

				System.out.println( "\n\n\n\n" );
				System.out.println( "Door Sensor Console: \n" );

				// Here is the main menu

				System.out.println( "Select an Option: \n" );
				System.out.println( "0: Break/Open Door" );
				System.out.println( "1: Door is closed" );
				System.out.print( "\n>>>> " );
				Option = UserInput.KeyboardReadString();

				//////////// option 1 ////////////

				// Here we set the sensor status
				if ( Option.equals( "0" ) )
				{

					Sensor.SetSensorStatus(0);

				} // if

				//////////// option 2 ////////////
				
				if ( Option.equals( "1" ) )
				{

					Sensor.SetSensorStatus(1);

				} // if
				
				if (Sensor.IsExit()){
					Done = true;
					System.out.println("\n\nSystem Exits.\n\n" );
				}

			} // while

		} else {

			System.out.println("\n\nUnable start the monitor.\n\n" );

		} // if

  	} // main

} // ECSConsole
