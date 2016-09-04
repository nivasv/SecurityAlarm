/******************************************************************************************************************
* File:ECSMonitor.java
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

class MotionSensor extends Thread
{
	private EventManagerInterface em = null;// Interface object to the event manager
	private String EvtMgrIP = null;			// Event Manager IP address
	boolean Registered = true;				// Signifies that this class is registered with an event manager.
	MessageWindow mw = null;				// This is the message window
	Indicator wi;							// Window Sensor indicator
	
	private float SensorStatus = 1;		// These parameter signify the status of Sensor; 1 = ON, 0 = OFF
	private boolean IsStatusUpdated = true;// These parameter signify the status of Sensor
	private boolean IsExit = false;

	public MotionSensor()
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
			System.out.println("MotionSensor::Error instantiating event manager interface: " + e);
			Registered = false;

		} // catch

	} //Constructor

	public MotionSensor( String EvmIpAddress )
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
			System.out.println("MotionSensor::Error instantiating event manager interface: " + e);
			Registered = false;

		} // catch

	} // Constructor

	public void run()
	{
		Event Evt = null;				// Event object
		EventQueue eq = null;			// Message Queue
//		int EvtId = 0;					// User specified event ID
		boolean AlarmState = false;		// Alarm state: false == off, true == on
		int	Delay = 1000;				// The loop delay (1 second)
		boolean Done = false;			// Loop termination flag
//		boolean ON = true;				// Used to turn on heaters, chillers, humidifiers, and dehumidifiers
//		boolean OFF = false;			// Used to turn off heaters, chillers, humidifiers, and dehumidifiers

		if (em != null)
		{
			// Now we create the ECS status and message panel
			// Note that we set up two indicators that are initially yellow. This is
			// because we do not know if the temperature/humidity is high/low.
			// This panel is placed in the upper left hand corner and the status 
			// indicators are placed directly to the right, one on top of the other

			mw = new MessageWindow("Motion Sensor Console", 0, 0);
	//		wi = new Indicator ("WINDOW SENSOR", mw.GetX()+ mw.Width(), 0);

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

					if ( Evt.GetEventId() == 3 )
					{
						if (Evt.GetMessage().equalsIgnoreCase("1")) // Security on
						{
							AlarmState = true;
							mw.WriteMessage("Motion Sensor is ON.");

						} // if

						if (Evt.GetMessage().equalsIgnoreCase("0")) // Security off
						{
							AlarmState = false;
							mw.WriteMessage("Motion Sensor is OFF.");

						} // if
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

		//				wi.dispose();

					} // if

				} // for
				
				// Post the window status
				
				if (AlarmState && IsStatusUpdated) {

					//////////// Sensor status can be 0 or 1 ////////////
					PostStatus( em, SensorStatus );
					if (SensorStatus == 0){
						mw.WriteMessage("Motion is detected." );
					}else{
						mw.WriteMessage("No motion is detected." );
					}

					IsStatusUpdated = false;
				} // if

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
	* CONCRETE METHOD:: SetSensorStatus
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

	public void SetSensorStatus(float status)
	{
		this.SensorStatus = status;


		this.IsStatusUpdated = true;

		if (status == 1){
			mw.WriteMessage( "***Sensor status entered as:: ON ***" );
		}
		else{
			mw.WriteMessage( "***Sensor status entered as:: OFF ***" );
		}

	} // SetSensorStatus

	/***************************************************************************
	* CONCRETE METHOD:: PostStatus
	* Purpose: This method posts the window status value to the
	* specified event manager. This method assumes an event ID of 6.
	*
	* Arguments: EventManagerInterface ei - this is the eventmanager interface
	*			 where the event will be posted.
	*
	*			 status - this is the status value.
	*
	* Returns: none
	*
	* Exceptions: None
	*
	***************************************************************************/

	static private void PostStatus(EventManagerInterface ei, float status)
	{
		// Here we create the event.

		Event evt = new Event( (int) 8, String.valueOf(status) );

		// Here we send the event to the event manager.

		try
		{
			ei.SendEvent( evt );
			//mw.WriteMessage( "Sent Window Event" );

		} // try

		catch (Exception e)
		{
			System.out.println( "Error Posting Motion status :: " + e );

		} // catch

	} // PostStatus

} // ECSMonitor