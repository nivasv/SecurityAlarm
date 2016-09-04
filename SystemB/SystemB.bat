%ECHO OFF
rmic EventManager
START "EVENT MANAGER REGISTRY" /MIN /NORMAL rmiregistry
START "EVENT MANAGER" /MIN /NORMAL java EventManager
%ECHO Starting ECS System
PAUSE
%ECHO ECS Monitoring Console
START "MUSEUM ENVIRONMENTAL CONTROL SYSTEM CONSOLE" /NORMAL java ECSConsole %1
%ECHO Starting Temperature Controller Console
START "TEMPERATURE CONTROLLER CONSOLE" /MIN /NORMAL java TemperatureController %1
%ECHO Starting Humidity Sensor Console
START "HUMIDITY CONTROLLER CONSOLE" /MIN /NORMAL java HumidityController %1
START "TEMPERATURE SENSOR CONSOLE" /MIN /NORMAL java TemperatureSensor %1
%ECHO Starting Humidity Sensor Console
START "HUMIDITY SENSOR CONSOLE" /MIN /NORMAL java HumiditySensor %1
%ECHO Starting SMS System
PAUSE
%ECHO SMS Monitoring Console
START "MUSEUM SECURITY MONITORING SYSTEM CONSOLE" /NORMAL java SecurityConsole %1
%ECHO Starting Security Controller Console
START "SECURITY CONTROLLER CONSOLE" /MIN /NORMAL java SecurityController %1
START "WINDOW SENSOR CONSOLE" /MIN /NORMAL java WindowSensorConsole %1
START "DOOR SENSOR CONSOLE" /MIN /NORMAL java DoorSensorConsole %1
START "MOTION SENSOR CONSOLE" /MIN /NORMAL java MotionSensorConsole %1
START "FIRE SPRINKLER CONTROLLER CONSOLE" /MIN /NORMAL java SprinklerController %1
START "FIRE ALARM CONTROLLER CONSOLE" /MIN /NORMAL java FireAlarmController %1
START "FIRE SENSOR CONSOLE" /MIN /NORMAL java FireSensorConsole %1


