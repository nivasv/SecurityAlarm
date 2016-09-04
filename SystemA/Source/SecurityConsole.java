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

public class SecurityConsole
{
	public static void main(String args[])
	{
    	Termio UserInput = new Termio();	// Termio IO Object
		boolean Done = false;				// Main loop flag
		String Option = null;				// Menu choice from user
		Event Evt = null;					// Event object
		boolean Error = false;				// Error flag
		SecurityMonitor Monitor = null;		// The Security monitor system Monitor
		float SecurityStatus = 0;			// These parameter signify the status of Security

		/////////////////////////////////////////////////////////////////////////////////
		// Get the IP address of the event manager
		/////////////////////////////////////////////////////////////////////////////////

 		if ( args.length != 0 )
 		{
			// event manager is not on the local system

			Monitor = new SecurityMonitor( args[0] );

		} else {

			Monitor = new SecurityMonitor();

		} // if


		// Here we check to see if registration worked. If ef is null then the
		// event manager interface was not properly created.

		if (Monitor.IsRegistered() )
		{
			Monitor.start(); // Here we start the monitoring and control thread

			while (!Done)
			{
				// Here, the main thread continues and provides the main menu

				System.out.println( "\n\n\n\n" );
				System.out.println( "Security Monitor System (SMS) Command Console: \n" );

				if (args.length != 0)
					System.out.println( "Using event manger at: " + args[0] + "\n" );
				else
					System.out.println( "Using local event manger \n" );

				System.out.println( "Select an Option: \n" );
				System.out.println( "0: Disarm the Security" );
				System.out.println( "1: Arm the Security" );
				System.out.print( "\n>>>> " );
				Option = UserInput.KeyboardReadString();

				//////////// option 1 ////////////

				// Here we set the security status
				if ( Option.equals( "0" ) )
				{

					Monitor.SetSecurityStatus(0);

				} // if

				//////////// option 2 ////////////
				
				if ( Option.equals( "1" ) )
				{

					Monitor.SetSecurityStatus(1);

				} // if

				if (Monitor.IsExit()){
					Done = true;
					System.out.println("\n\nSystem Exits.\n\n" );
				}

			} // while

		} else {

			System.out.println("\n\nUnable start the monitor.\n\n" );

		} // if

  	} // main

} // ECSConsole
