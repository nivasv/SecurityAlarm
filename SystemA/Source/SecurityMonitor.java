/******************************************************************************************************************
f* File:ECSMonitor.java
* Course: 17655
* Project: Assignment A3
* Copyright: Copyright (c) 2009 Carnegie Mellon University
* Versions:
*	1.0 March 2009 - Initial rewrite of original assignment 3 (ajl).
*
* Description:
*
* This class monitors the environmental control systems that control museum temperature and humidity. In addition to
* monitoring the temperature and humidity, the ECSMonitor also allows a user to set the humidity and temperature
* ranges to be maintained. If temperatures exceed those limits over/under alarm indicators are triggered.
*
* Parameters: IP address of the event manager (on command line). If blank, it is assumed that the event manager is
* on the local machine.
*
* Internal Methods:
*	static private void Heater(EventManagerInterface ei, boolean ON )
*	static private void Chiller(EventManagerInterface ei, boolean ON )
*	static private void Humidifier(EventManagerInterface ei, boolean ON )
*	static private void Dehumidifier(EventManagerInterface ei, boolean ON )
*
******************************************************************************************************************/
import InstrumentationPackage.*;
import EventPackage.*;

import java.util.*;

import javax.swing.text.Position;

class SecurityMonitor extends Thread
{
	private EventManagerInterface em = null;// Interface object to the event manager
	private String EvtMgrIP = null;			// Event Manager IP address
	private float SecurityStatus = 0;		// These parameter signify the status of Security; 1 = Armed, 0 = Disarmed
	private boolean IsStatusUpdated = true;// These parameter signify the status of Security
	private boolean IsExit = false;
	
	boolean Registered = true;				// Signifies that this class is registered with an event manager.
	MessageWindow mw = null;				// This is the message window
	Indicator wi;							// Window indicator
	Indicator di;							// Door indicator
	Indicator mi;							// Motion indicator

	public SecurityMonitor()
	{
		// event manager is on the local system

		try
		{
			// Here we create an event manager interface object. This assumes
			// that the event manager is on the local machine

			em = new EventManagerInterface();

		}

		catch (Exception e)
		{
			System.out.println("SecurityMonitor::Error instantiating event manager interface: " + e);
			Registered = false;

		} // catch

	} //Constructor

	public SecurityMonitor( String EvmIpAddress )
	{
		// event manager is not on the local system

		EvtMgrIP = EvmIpAddress;

		try
		{
			// Here we create an event manager interface object. This assumes
			// that the event manager is NOT on the local machine

			em = new EventManagerInterface( EvtMgrIP );
		}

		catch (Exception e)
		{
			System.out.println("SecurityMonitor::Error instantiating event manager interface: " + e);
			Registered = false;

		} // catch

	} // Constructor

	public void run()
	{
		Event Evt = null;				// Event object
		EventQueue eq = null;			// Message Queue
		int EvtId = 0;					// Event ID
		float CurrentWindow = 1;	    // Current Window status as reported by the window sensor
		float CurrentDoor = 1;	        // Current Window status as reported by the window sensor
		float CurrentMotion = 1;	    // Current Window status as reported by the window sensor
		int	Delay = 1000;				// The loop delay (1 second)
		boolean Done = false;			// Loop termination flag
		boolean ON = true;				// Used to turn on heaters, chillers, humidifiers, and dehumidifiers
		boolean OFF = false;			// Used to turn off heaters, chillers, humidifiers, and dehumidifiers

		if (em != null)
		{
			// Now we create the ECS status and message panel
			// Note that we set up two indicators that are initially yellow. This is
			// because we do not know if the temperature/humidity is high/low.
			// This panel is placed in the upper left hand corner and the status 
			// indicators are placed directly to the right, one on top of the other

			mw = new MessageWindow("SMS Monitoring Console", 0.2f, 0.2f);
			wi = new Indicator ("Window status", mw.GetX()+ mw.Width(), 0);
			di = new Indicator ("Door status", mw.GetX()+ mw.Width(), 0);
			mi = new Indicator ("Motion status", mw.GetX()+ mw.Width(), 0);
			//hi = new Indicator ("HUMI UNK", mw.GetX()+ mw.Width(), (int)(mw.Height()/2), 2 );

			mw.WriteMessage( "Registered with the event manager." );

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
				// Here we get our event queue from the event manager

				try
				{
					eq = em.GetEventQueue();

				} // try

				catch( Exception e )
				{
					mw.WriteMessage("Error getting event queue::" + e );

				} // catch

				// If there are messages in the queue, we read through them.
				// We are looking for EventIDs = 1 or 2. Event IDs of 1 are temperature
				// readings from the temperature sensor; event IDs of 2 are humidity sensor
				// readings. Note that we get all the messages at once... there is a 1
				// second delay between samples,.. so the assumption is that there should
				// only be a message at most. If there are more, it is the last message
				// that will effect the status of the temperature and humidity controllers
				// as it would in reality.

				int qlen = eq.GetSize();

				for ( int i = 0; i < qlen; i++ )
				{
					Evt = eq.GetEvent();

					if ( Evt.GetEventId() == 6 ) // Window reading
					{
						try
						{
							CurrentWindow = Float.valueOf(Evt.GetMessage()).floatValue();

						} // try

						catch( Exception e )
						{
							mw.WriteMessage("Error reading window: " + e);

						} // catch

					} // if

					if ( Evt.GetEventId() == 7 ) // Door reading
					{
						try
						{
							CurrentDoor = Float.valueOf(Evt.GetMessage()).floatValue();

						} // try

						catch( Exception e )
						{
							mw.WriteMessage("Error reading door: " + e);

						} // catch

					} // if

					if ( Evt.GetEventId() == 8 ) // Motion reading
					{
						try
						{
							CurrentMotion = Float.valueOf(Evt.GetMessage()).floatValue();

						} // try

						catch( Exception e )
						{
							mw.WriteMessage("Error reading motion: " + e);

						} // catch

					} // if

					// If the event ID == 99 then this is a signal that the simulation
					// is to end. At this point, the loop termination flag is set to
					// true and this process unregisters from the event manager.

					if ( Evt.GetEventId() == 99 )
					{
						Done = true;
						IsExit = true;

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

						wi.dispose();
						di.dispose();
						mi.dispose();

					} // if

					// Check the sensor status and alarm if needed
					if (Evt.GetEventId() == 6 || Evt.GetEventId() == 3){
						if (SecurityStatus == 0) // If Security is disarmed
						{
							wi.SetLampColorAndMessage("Window monitor off", 0);
						}

						if (SecurityStatus == 1) // If Security is armed
						{
							if(CurrentWindow == 0){
								mw.WriteMessage("Window:: Open/Broke");
								wi.SetLampColorAndMessage("Window monitor on - Open/Broke", 3);
								AlarmController(9, "W1"); // Raise window alarm
							}

							if (CurrentWindow == 1){
								mw.WriteMessage("Window:: Closed");
								wi.SetLampColorAndMessage("Window monitor on - Closed", 1);
								AlarmController(9, "W0"); // Off window alarm
							}

						}
					}
					
					// Check the sensor status and alarm if needed
					if (Evt.GetEventId() == 7 || Evt.GetEventId() == 3){
						if (SecurityStatus == 0) // If Security is disarmed
						{
							di.SetLampColorAndMessage("Door monitor off", 0);
						}

						if (SecurityStatus == 1) // If Security is armed
						{
							if(CurrentDoor == 0){
								mw.WriteMessage("Door:: Open/Broke");
								di.SetLampColorAndMessage("Door monitor on - Open/Broke", 3);
								AlarmController(9, "D1"); // Raise door alarm
							}

							if (CurrentDoor == 1){
								mw.WriteMessage("Door:: Closed");
								di.SetLampColorAndMessage("Door monitor on - Closed", 1);
								AlarmController(9, "D0"); // Off door alarm
							}

						}
					}
					
					// Check the sensor status and alarm if needed
					if (Evt.GetEventId() == 8 || Evt.GetEventId() == 3){
						if (SecurityStatus == 0) // If Security is disarmed
						{
							mi.SetLampColorAndMessage("Motion monitor off", 0);
						}

						if (SecurityStatus == 1) // If Security is armed
						{
							if(CurrentMotion == 0){
								mw.WriteMessage("No motion");
								mi.SetLampColorAndMessage("Motion monitor on - Detected", 3);
								AlarmController(9, "M1"); // Raise Motion alarm
							}

							if (CurrentMotion == 1){
								mw.WriteMessage("Motion is detected");
								mi.SetLampColorAndMessage("Motion monitor on - No motion", 1);
								AlarmController(9, "M0"); // Off Motion alarm
							}

						}
					}

				} // for
				
				// Check Security status and effect control as necessary

				if (IsStatusUpdated){
					if (SecurityStatus == 1){
						PostSecurity(ON);
					} else {
						PostSecurity(OFF);
					}
					IsStatusUpdated = false;
				}
				
				// This delay slows down the sample rate to Delay milliseconds

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

	/***************************************************************************
	* CONCRETE METHOD:: IsRegistered
	* Purpose: This method returns the registered status
	*
	* Arguments: none
	*
	* Returns: boolean true if registered, false if not registered
	*
	* Exceptions: None
	*
	***************************************************************************/

	public boolean IsRegistered()
	{
		return( Registered );

	} // SetTemperatureRange
	
	/***************************************************************************
	* CONCRETE METHOD:: IsExit
	* Purpose: This method returns the registered status
	*
	* Arguments: none
	*
	* Returns: boolean true if registered, false if not registered
	*
	* Exceptions: None
	*
	***************************************************************************/

	public boolean IsExit()
	{
		return( IsExit );

	} // SetTemperatureRange

	/***************************************************************************
	* CONCRETE METHOD:: SetTemperatureRange
	* Purpose: This method sets the temperature range
	*
	* Arguments: float lowtemp - low temperature range
	*			 float hightemp - high temperature range
	*
	* Returns: none
	*
	* Exceptions: None
	*
	***************************************************************************/

	public void SetSecurityStatus(float securitystatus)
	{
		this.SecurityStatus = securitystatus;
		this.IsStatusUpdated = true;
		
		if (securitystatus == 1){
			mw.WriteMessage( "***Security status changed to:: ON ***" );
		}
		else{
			mw.WriteMessage( "***Security status changed to:: OFF ***" );
		}

	} // SetTemperatureRange

	public void Halt()
	{
		mw.WriteMessage( "***HALT MESSAGE RECEIVED - SHUTTING DOWN SYSTEM***" );

		// Here we create the stop event.

		Event evt;

		evt = new Event( (int) 98, "XXX" );

		// Here we send the event to the event manager.

		try
		{
			em.SendEvent( evt );

		} // try

		catch (Exception e)
		{
			System.out.println("Error sending halt message:: " + e);

		} // catch

	} // Halt
	
	/***************************************************************************
	* CONCRETE METHOD:: Heater
	* Purpose: This method posts events that will signal the temperature
	*		   controller to turn on/off the heater
	*
	* Arguments: boolean ON(true)/OFF(false) - indicates whether to turn the
	*			 heater on or off.
	*
	* Returns: none
	*
	* Exceptions: Posting to event manager exception
	*
	***************************************************************************/

	private void PostSecurity( boolean ON )
	{
		// Here we create the event.

		Event evt;

		if ( ON )
		{
			evt = new Event( (int) 3, "1" );

		} else {

			evt = new Event( (int) 3, "0" );

		} // if

		// Here we send the event to the event manager.

		try
		{
			em.SendEvent( evt );

		} // try

		catch (Exception e)
		{
			System.out.println("Error sending security control message:: " + e);

		} // catch

	} // Heater
	
	/***************************************************************************
	* CONCRETE METHOD:: Heater
	* Purpose: This method posts events that will signal the temperature
	*		   controller to turn on/off the heater
	*
	* Arguments: boolean ON(true)/OFF(false) - indicates whether to turn the
	*			 heater on or off.
	*
	* Returns: none
	*
	* Exceptions: Posting to event manager exception
	*
	***************************************************************************/

	private void AlarmController(int eventId, String postAlarm )
	{
		// Here we create the event.

		Event evt;

		evt = new Event( eventId, postAlarm );
		
		// Here we send the event to the event manager.

		try
		{
			em.SendEvent( evt );

		} // try

		catch (Exception e)
		{
			System.out.println("Error sending Alarm control message:: " + postAlarm + " **** " + e);

		} // catch

	} // Heater

} // SMSMonitor