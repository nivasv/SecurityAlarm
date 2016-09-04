/******************************************************************************************************************
* File:TemperatureController.java
* Course: 17655
* Project: Assignment A3
* Copyright: Copyright (c) 2009 Carnegie Mellon University
* Versions:
*	1.0 March 2009 - Initial rewrite of original assignment 3 (ajl).
*
* Description:
*
* This class simulates a device that controls a heater and chiller. It polls the event manager for event ids = 5
* and reacts to them by turning on or off the heater or chiller. The following command are valid strings for con
* trolling the heater and chiller:
*
*	H1 = heater on
*	H0 = heater off
*	C1 = chillerer on
*	C0 = chiller off
*
* The state (on/off) is graphically displayed on the terminal in the indicator. Command messages are displayed in
* the message window. Once a valid command is recieved a confirmation event is sent with the id of -5 and the command in
* the command string.
*
* Parameters: IP address of the event manager (on command line). If blank, it is assumed that the event manager is
* on the local machine.
*
* Internal Methods:
*	static private void ConfirmMessage(EventManagerInterface ei, String m )
*
******************************************************************************************************************/
import InstrumentationPackage.*;
import EventPackage.*;
import java.util.*;

class SecurityController
{
	public static void main(String args[])
	{
		String EvtMgrIP;				// Event Manager IP address
		Event Evt = null;				// Event object
		EventQueue eq = null;			// Message Queue
		int EvtId = 0;					// User specified event ID
		EventManagerInterface em = null;// Interface object to the event manager
		boolean AlarmState = false;		// Alarm state: false == off, true == on
		boolean SecurityStatusChanged = false;

		int	Delay = 2500;				// The loop delay (2.5 seconds)
		boolean Done = false;			// Loop termination flag

		/////////////////////////////////////////////////////////////////////////////////
		// Get the IP address of the event manager
		/////////////////////////////////////////////////////////////////////////////////

 		if ( args.length == 0 )
 		{
			// event manager is on the local system

			System.out.println("\n\nAttempting to register on the local machine..." );

			try
			{
				// Here we create an event manager interface object. This assumes
				// that the event manager is on the local machine

				em = new EventManagerInterface();
			}

			catch (Exception e)
			{
				System.out.println("Error instantiating event manager interface: " + e);

			} // catch

		} else {

			// event manager is not on the local system

			EvtMgrIP = args[0];

			System.out.println("\n\nAttempting to register on the machine:: " + EvtMgrIP );

			try
			{
				// Here we create an event manager interface object. This assumes
				// that the event manager is NOT on the local machine

				em = new EventManagerInterface( EvtMgrIP );
			}

			catch (Exception e)
			{
				System.out.println("Error instantiating event manager interface: " + e);

			} // catch

		} // if

		// Here we check to see if registration worked. If ef is null then the
		// event manager interface was not properly created.

		if (em != null)
		{
			System.out.println("Registered with the event manager." );

			/* Now we create the temperature control status and message panel
			** We put this panel about 1/3 the way down the terminal, aligned to the left
			** of the terminal. The status indicators are placed directly under this panel
			*/

			float WinPosX = 0.0f; 	//This is the X position of the message window in terms 
								 	//of a percentage of the screen height
			float WinPosY = 0.3f; 	//This is the Y position of the message window in terms 
								 	//of a percentage of the screen height 
			
			MessageWindow mw = new MessageWindow("Security Controller Status Console", WinPosX, WinPosY);
			
			// Put the status indicators under the panel...
			
			Indicator wi = new Indicator ("Window Alarm Controller", mw.GetX(), mw.GetY()+mw.Height());
			Indicator di = new Indicator ("Door Alarm Controller", mw.GetX(), mw.GetY()+mw.Height());
			Indicator mi = new Indicator ("Motion Alarm Controller", mw.GetX(), mw.GetY()+mw.Height());
			
			mw.WriteMessage("Registered with the event manager." );

	    	try
	    	{
				mw.WriteMessage("   Participant id: " + em.GetMyId() );
				mw.WriteMessage("   Registration Time: " + em.GetRegistrationTime() );

			} // try

	    	catch (Exception e)
			{
				System.out.println("Error:: " + e);

			} // catch

			/********************************************************************
			** Here we start the main simulation loop
			*********************************************************************/

			while ( !Done )
			{

				try
				{
					eq = em.GetEventQueue();

				} // try

				catch( Exception e )
				{
					mw.WriteMessage("Error getting event queue::" + e );

				} // catch

				// If there are messages in the queue, we read through them.
				// We are looking for EventIDs = 5, this is a request to turn the
				// heater or chiller on. Note that we get all the messages
				// at once... there is a 2.5 second delay between samples,.. so
				// the assumption is that there should only be a message at most.
				// If there are more, it is the last message that will effect the
				// output of the temperature as it would in reality.

				int qlen = eq.GetSize();

				for ( int i = 0; i < qlen; i++ )
				{
					Evt = eq.GetEvent();

					EvtId = Evt.GetEventId();
		//			mw.WriteMessage("Event:" + EvtId + " " + Evt.GetMessage());
					if ( Evt.GetEventId() == 3 )
					{
						if (Evt.GetMessage().equalsIgnoreCase("1")) // Security on
						{
							AlarmState = true;
							SecurityStatusChanged = true;
							mw.WriteMessage("Security controller is ON.");
							wi.SetLampColorAndMessage("WINDOW ALARM OFF", 3);
							di.SetLampColorAndMessage("DOOR ALARM OFF", 3);
							mi.SetLampColorAndMessage("MOTION ALARM OFF", 3);


						} // if

						if (Evt.GetMessage().equalsIgnoreCase("0")) // Security off
						{
							AlarmState = false;
							SecurityStatusChanged = true;
							mw.WriteMessage("Security controller is OFF.");

						} // if
					} // if
					
					if (Evt.GetEventId() == 9) {
						SecurityStatusChanged = true;
					}
					
					if (SecurityStatusChanged){
						if ( AlarmState ){
							if (Evt.GetEventId() == 9)
							{
								if (Evt.GetMessage().equalsIgnoreCase("W1")) // Window alarm on
								{
									wi.SetLampColorAndMessage("WINDOW ALARM ON", 1);
									mw.WriteMessage("Received WINDOW alarm turn ON event" );

								} // if

								if (Evt.GetMessage().equalsIgnoreCase("W0")) // Window alarm off
								{
									wi.SetLampColorAndMessage("WINDOW ALARM OFF", 3);
									mw.WriteMessage("Received WINDOW alarm turn OFF event" );

								} // if

								if (Evt.GetMessage().equalsIgnoreCase("D1")) // DOOR alarm on
								{
									di.SetLampColorAndMessage("DOOR ALARM ON", 1);
									mw.WriteMessage("Received DOOR alarm turn ON event" );

								} // if

								if (Evt.GetMessage().equalsIgnoreCase("D0")) // DOOR alarm off
								{
									di.SetLampColorAndMessage("DOOR ALARM OFF", 3);
									mw.WriteMessage("Received DOOR alarm turn OFF event" );

								} // if
								if (Evt.GetMessage().equalsIgnoreCase("M1")) // MOTION alarm on
								{
									mi.SetLampColorAndMessage("MOTION ALARM ON", 1);
									mw.WriteMessage("Received MOTION alarm turn ON event" );

								} // if

								if (Evt.GetMessage().equalsIgnoreCase("M0")) // MOTION alarm off
								{
									mi.SetLampColorAndMessage("MOTION ALARM OFF", 3);
									mw.WriteMessage("Received MOTION alarm turn OFF event" );

								} // if

							} // if
						}// if
						else {
							// Set to green, heater is off
							wi.SetLampColorAndMessage("WINDOW ALARM DISABLED", 0);
							di.SetLampColorAndMessage("DOOR ALARM DISABLED", 0);
							mi.SetLampColorAndMessage("MOTION ALARM DISABLED", 0);
						}
						SecurityStatusChanged = false;
					}
					

					// If the event ID == 99 then this is a signal that the simulation
					// is to end. At this point, the loop termination flag is set to
					// true and this process unregisters from the event manager.

					if ( Evt.GetEventId() == 99 )
					{
						Done = true;

						try
						{
							em.UnRegister();

				    	} // try

				    	catch (Exception e)
				    	{
							mw.WriteMessage("Error unregistering: " + e);

				    	} // catch

				    	mw.WriteMessage( "\n\nSimulation Stopped. \n");

						// Get rid of the indicators. The message panel is left for the
						// user to exit so they can see the last message posted.

			//			ti.dispose();
						wi.dispose();
						di.dispose();
						mi.dispose();

					} // if

				} // for

				try
				{
					Thread.sleep( Delay );

				} // try

				catch( Exception e )
				{
					System.out.println( "Sleep error:: " + e );

				} // catch

			} // while

		} else {

			System.out.println("Unable to register with the event manager.\n\n" );

		} // if

	} // main

} // SecurityController