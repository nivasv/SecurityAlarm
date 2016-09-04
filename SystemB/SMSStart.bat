%ECHO OFF
%ECHO Starting SMS System
PAUSE
%ECHO SMS Monitoring Console
START "MUSEUM SECURITY MONITORING SYSTEM CONSOLE" /NORMAL java SecurityConsole %1
%ECHO Starting Security Controller Console
START "SECURITY CONTROLLER CONSOLE" /MIN /NORMAL java SecurityController %1
START "WINDOW SENSOR CONSOLE" /MIN /NORMAL java WindowSensorConsole %1
START "DOOR SENSOR CONSOLE" /MIN /NORMAL java DoorSensorConsole %1
START "MOTION SENSOR CONSOLE" /MIN /NORMAL java MotionSensorConsole %1