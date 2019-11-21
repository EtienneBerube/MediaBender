
/****************************************************************
Gesture.ino
APDS-9960 RGB and Gesture Sensor
To perform a NEAR gesture, hold your hand
far above the sensor and move it close to the sensor (within 2
inches). Hold your hand there for at least 1 second and move it
away.
To perform a FAR gesture, hold your hand within 2 inches of the
sensor for at least 1 second and then move it above (out of
range) of the sensor.
Hardware Connections:
IMPORTANT: The APDS-9960 can only accept 3.3V!
 
 Arduino Pin  APDS-9960 Board  Function
 
 3.3V         VCC              Power
 GND          GND              Ground
 A4           SDA              I2C Data
 A5           SCL              I2C Clock
 2            INT              Interrupt
Resources:
Include Wire.h and SparkFun_APDS-9960.h
****************************************************************/

#include <Wire.h>
#include <SparkFun_APDS9960.h>

// Pins
#define APDS9960_INT    2 // Needs to be an interrupt pin

//Request type
#define REQUEST_NONE        0x00 //XXXXX000
#define REQUEST_FLAG        0x01 //XXXXX001
#define REQUEST_SENSIBILITY 0x02 //XXXXX010
#define REQUEST_REQUEST1    0x03 //XXXXX011
#define REQUEST_REQUEST2    0x04 //XXXXX100
#define REQUEST_REQUEST3    0x05 //XXXXX101
#define REQUEST_REQUEST4    0x06 //XXXXX110
#define REQUEST_REQUEST5    0x07 //XXXXX111

//Sensibility level
#define SENSIBILITY_LOW     0x00 //XXX00XXX
#define SENSIBILITY_MEDIUM  0x08 //XXX01XXX
#define SENSIBILITY_HIGH    0x10 //XXX10XXX
#define SENSIBILITY_MAX     0x18 //XXX11XXX

// Gestures
#define GESTURE_NONE     0x00 //X0000000
#define GESTURE_UP       0x01 //X0000001
#define GESTURE_DOWN     0x02 //X0000010
#define GESTURE_LEFT     0x03 //X0000011
#define GESTURE_RIGHT    0x04 //X0000100
#define GESTURE_NEAR     0x05 //X0000101
#define GESTURE_FAR      0x06 //X0000110
#define GESTURE_GESTURE1 0x07 //X0000111
#define GESTURE_GESTURE2 0x08 //X0001000
#define GESTURE_GESTURE3 0x09 //X0001001
//We can define as much as we want

//Flags
#define FLAG_SYSTEMINITEXCEPTION 0x01 //X0000001
#define FLAG_SENSORINITEXCEPTION 0x02 //X0000010
#define FLAG_GESTUREUNAVAILABLE  0x04 //X0000100
#define FLAG_SETSENSIBILITYFAIL  0x08 //X0001000
#define FLAG_FLAG2               0x10 //X0010000
#define FLAG_FLAG3               0x20 //X0100000
#define FLAG_FLAG4               0x40 //X1000000

//Answer bit
#define REQUESTANSWER     0x80 //1XXXXXXX

//Comparator
#define FLAGS         0x7F //X1111111
#define REQUESTS      0x07 //XXXXX111
#define GESTURES      0x7F //X1111111
#define SENSIBILITIES 0x18 //XXX11XXX

// Global Variables
SparkFun_APDS9960 apds = SparkFun_APDS9960();
static byte message_gesture = 0x00;
static byte message_request = 0x00;
static byte flags = 0x00;
int isr_flag = 0;

void setup() {
  // Set interrupt pin as input
  pinMode(APDS9960_INT, INPUT);

  // Initialize Serial port
  Serial.begin(9600);

#if DEBUG
  Serial.println();
  Serial.println(F("--------------------------------"));
  Serial.println(F("SparkFun APDS-9960 - GestureTest"));
  Serial.println(F("--------------------------------"));
#endif

  while (!Serial) {
    // wait for serial port to connect.
  }
  
  // Initialize interrupt service routine
  attachInterrupt(0, interruptRoutine, FALLING);

  // Initialize APDS-9960 (configure I2C and initial values)
  if ( apds.init() ) {
#if DEBUG
    Serial.println(F("APDS-9960 initialization complete"));
#endif
  } else {
#if DEBUG
    Serial.println(F("Something went wrong during APDS-9960 init!"));
#endif
    message_request = message_request|FLAG_SYSTEMINITEXCEPTION;
  }
  
  // Start running the APDS-9960 gesture sensor engine
  if ( apds.enableGestureSensor(true) ) {
#if DEBUG
    Serial.println(F("Gesture sensor is now running"));
#endif
  } else {
#if DEBUG    
    Serial.println(F("Something went wrong during gesture sensor init!"));
#endif
    message_request = message_request|FLAG_SENSORINITEXCEPTION;
  }
}

void loop() {
  if( isr_flag == 1 ) {
    detachInterrupt(0);
    handleGesture();
    Serial.write(message_gesture);
    isr_flag = 0;
    attachInterrupt(0, interruptRoutine, FALLING);
    message_gesture = 0x00;
  }
  if(Serial.available()){
    message_request=message_request|REQUESTANSWER;
    handleRequest();
    message_request = 0x00;
  }
}

void interruptRoutine() {
  isr_flag = 1;
}

void handleGesture() {
    if ( apds.isGestureAvailable() ) {
    switch ( apds.readGesture() ) {
      case DIR_UP:
      message_gesture = message_gesture|GESTURE_UP;
#if DEBUG
        Serial.println("UP");
#endif
        break;
      case DIR_DOWN:
      message_gesture = message_gesture|GESTURE_DOWN;
#if DEBUG
        Serial.println("DOWN");
#endif
        break;
      case DIR_LEFT:
      message_gesture = message_gesture|GESTURE_LEFT;
#if DEBUG
        Serial.println("LEFT");
#endif
        break;
      case DIR_RIGHT:
      message_gesture = message_gesture|GESTURE_RIGHT;
#if DEBUG
        Serial.println("RIGHT");
#endif
        break;
      case DIR_NEAR:
      message_gesture = message_gesture|GESTURE_NEAR;
#if DEBUG
        Serial.println("NEAR");
#endif
        break;
      case DIR_FAR:
      message_gesture = message_gesture|GESTURE_FAR;
#if DEBUG
        Serial.println("FAR");
#endif
        break;
      default:
      message_gesture = message_gesture|GESTURE_NONE;
#if DEBUG
        Serial.println("NONE");
#endif
    }
  }
}

void handleRequest(){
  byte request = Serial.read();
  switch(request&REQUESTS){
    case REQUEST_FLAG:
    flagRequest();
    message_request = message_request|flags;
    Serial.write(message_request);
    break;
    case REQUEST_SENSIBILITY:
    setSensibility(request);
    Serial.write(message_request);
    break;
    case REQUEST_REQUEST1:
    case REQUEST_REQUEST2:
    case REQUEST_REQUEST3:
    case REQUEST_REQUEST4:
    case REQUEST_REQUEST5:
    case REQUEST_NONE:
    default:
    //Nothing here yet
    break;
  }
}

void flagRequest(){
  if(!apds.isGestureAvailable()){
    flags = flags|FLAG_GESTUREUNAVAILABLE;
  }else{
    flags = (flags&(~FLAG_GESTUREUNAVAILABLE));
  }
}

void setSensibility(byte sensibility){
  switch(sensibility&SENSIBILITIES){
    case SENSIBILITY_LOW:
    if(!apds.setGestureGain(0)){
      message_request = message_request|FLAG_SETSENSIBILITYFAIL;
    }else{
      message_request = (message_request&(~FLAG_SETSENSIBILITYFAIL));
    }
    break;
    case SENSIBILITY_MEDIUM:
    if(!apds.setGestureGain(1)){
      message_request = message_request|FLAG_SETSENSIBILITYFAIL;
    }else{
      message_request = (message_request&(~FLAG_SETSENSIBILITYFAIL));
    }
    break;
    case SENSIBILITY_HIGH:
    if(!apds.setGestureGain(2)){
      message_request = message_request|FLAG_SETSENSIBILITYFAIL;
    }else{
      message_request = (message_request&(~FLAG_SETSENSIBILITYFAIL));
    }
    break;
    case SENSIBILITY_MAX:
    if(!apds.setGestureGain(3)){
      message_request = message_request|FLAG_SETSENSIBILITYFAIL;
    }else{
      message_request = (message_request&(~FLAG_SETSENSIBILITYFAIL));
    }
    break;
    default:
    apds.setGestureGain(0);
    message_request = message_request|FLAG_SETSENSIBILITYFAIL;
    break;
  }
}

