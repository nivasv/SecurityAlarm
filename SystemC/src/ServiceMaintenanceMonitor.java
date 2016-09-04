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

class ServiceMaintenanceMonitor extends Thread
{
	private EventManagerInterface em = null;// Interface object to the event manager
	private String EvtMgrIP = null;			// Event Manager IP address
	
	private boolean isListDevices = false;// These parameter signify the status of Security
	
	HashMap<String, Device> deviceMap = new HashMap<String, Device>();
	
	boolean Registered = true;				// Signifies that this class is registered with an event manager.
	MessageWindow mw = null;				// This is the message window
	
	private boolean IsExit = false;

	public ServiceMaintenanceMonitor()
	{
		// event manager is on the local system

		try
		{
			// Here we create an event manager interface object. This assumes
			// that the event manager is on the local machine

			em = new EventManagerInterface("Service Maintenance Console/Monitor", "This application lists the cconnected devices and detects whether devices are working or not.");

		}

		catch (Exception e)
		{
			System.out.println("Service Maintenance Monitor::Error instantiating event manager interface: " + e);
			Registered = false;

		} // catch

	} //Constructor

	public ServiceMaintenanceMonitor( String EvmIpAddress )
	{
		// event manager is not on the local system

		EvtMgrIP = EvmIpAddress;

		
		try
		{
			// Here we create an event manager interface object. This assumes
			// that the event manager is NOT on the local machine

			em = new EventManagerInterface( EvtMgrIP, "Service Maintenance Console/Monitor", "This application lists the cconnected devices and detects whether devices are working or not." );
		}

		catch (Exception e)
		{
			System.out.println("ECSMonitor::Error instantiating event manager interface: " + e);
			Registered = false;

		} // catch
		
		

	} // Constructor

	public void run()
	{
		Event Evt = null;				// Event object
		EventQueue eq = null;			// Message Queue
		int EvtId = 0;					// Event ID
		float CurrentWindow = 1;	    // Current Window status as reported by the window sensor
		int Delay10 = 10000;			// Delay 10 seconds
		boolean Done = false;			// Loop termination flag
		int	Delay = 1000;				// The loop delay (1 second)
		boolean ON = true;				// Used to turn on heaters, chillers, humidifiers, and dehumidifiers
		boolean OFF = false;			// Used to turn off heaters, chillers, humidifiers, and dehumidifiers

		if (em != null)
		{
			// Now we create the ECS status and message panel
			// Note that we set up two indicators that are initially yellow. This is
			// because we do not know if the temperature/humidity is high/low.
			// This panel is placed in the upper left hand corner and the status 
			// indicators are placed directly to the right, one on top of the other

			mw = new MessageWindow("Service Monitoring Console", 0.2f, 0.2f);

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
	    	
	    	try
			{
	    		mw.WriteMessage("Waiting for 10 seconds");
				Thread.sleep( Delay10 );
			} // try

			catch( Exception e )
			{
				System.out.println( "Sleep error:: " + e );

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
	//				mw.WriteMessage("Event id: " + Evt.GetEventId());

					if ( Evt.GetEventId() == 21 ) // Window reading
					{
						
						String[] str_array = Evt.GetMessage().split(":");
//						mw.WriteMessage("Message: " + Evt.GetMessage());
						Device addDevice = new Device();
						
						addDevice.setName(str_array[1]);
//						mw.WriteMessage("1: " + str_array[1]);
						addDevice.setDescription(str_array[2]);
//						mw.WriteMessage("2: " + str_array[2]);
						addDevice.setChecked(true);
//						mw.WriteMessage("status: " + addDevice.isChecked());
						deviceMap.put(str_array[0], addDevice);
						
					} // if

					if ( Evt.GetEventId() == 22 ) // Fire Alarm
					{
						deviceMap.remove(Evt.GetMessage());
					} // if
					
					if ( Evt.GetEventId() == 23 ) // Health Response
					{
						em.HealthResponse();
					} // if
					
					if ( Evt.GetEventId() == 24 ) // Fire Alarm
					{						
						Device getDevice = deviceMap.get(Evt.GetMessage());
			//			mw.WriteMessage("Response: " + Evt.GetMessage());
						getDevice.setChecked(true);
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

					} // if
				} // for
				
	
				
				Set set = deviceMap.entrySet();
				Iterator iterator = set.iterator();
				while(iterator.hasNext()) {
					Map.Entry mentry = (Map.Entry)iterator.next();

					Device checkDevice = (Device) mentry.getValue();

					if (!checkDevice.isChecked()){
						mw.WriteMessage( "Device is not responding: " + checkDevice.getName());
					}
				} // while
				
				set = deviceMap.entrySet();
				iterator = set.iterator();
				while(iterator.hasNext()) {
					Map.Entry mentry = (Map.Entry)iterator.next();

					Device resetDevice = (Device) mentry.getValue();

					resetDevice.setChecked(false);

				} // while
				
				PostEvent(23, "Health check");
				
				// This delay slows down the sample rate to Delay milliseconds

				try
				{
		    		mw.WriteMessage("Waiting 10 seconds for device response");
					Thread.sleep( Delay10 );

				} // try

				catch( Exception e )
				{
					System.out.println( "Sleep error:: " + e );

				} // catch
				
				if (isListDevices){
					set = deviceMap.entrySet();
					iterator = set.iterator();
					while(iterator.hasNext()) {
						Map.Entry mentry = (Map.Entry)iterator.next();

						Device selectDevice = (Device) mentry.getValue();

						mw.WriteMessage( "Device : " + selectDevice.getName() + " *** Description : " + selectDevice.getDescription());

					} // while

					isListDevices = false;
				}

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

	private void PostEvent(int eventId, String eventmsg )
	{
		// Here we create the event.

		Event evt;

		evt = new Event( eventId, eventmsg );
		
		// Here we send the event to the event manager.

		try
		{
			em.SendEvent( evt );

		} // try

		catch (Exception e)
		{
			System.out.println("Error sending Service maintenance message:: " + eventmsg + " **** " + e);

		} // catch

	} // Heater
	
	/***************************************************************************
	* CONCRETE METHOD:: ListDevices
	* Purpose: This method returns the registered status
	*
	* Arguments: none
	*
	* Returns: boolean true if registered, false if not registered
	*
	* Exceptions: None
	*
	***************************************************************************/

	public void ListDevices()
	{
		this.isListDevices = true;

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

} // SMSMonitor