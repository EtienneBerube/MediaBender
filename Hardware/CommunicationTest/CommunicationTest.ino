

#define VOID    0x00 //000
#define NORTH   0x01 //001
#define SOUTH   0x02 //010
#define UP      0x03 //011
#define DOWN    0x04 //100
#define LEFT    0x05 //101
#define RIGHT   0x06 //110
#define UNASSIGNED1       0x07 //111
#define OVERFLOWEXCEPTION 0x08 //00001XXX
#define MEMALLOCEXCEPTION 0x10 //00010XXX
#define SENSORNOTFOUND    0x20 //00100XXX
#define FLAG1             0x40 //01000XXX
#define FLAG2             0x80 //10000XXX
static byte message = 0x00;
void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600); 
}

void loop() {
  updateMessage();
  if(Serial.available())  
  {  
   message = Serial.read();  
   Serial.print(message);  
  }  
}

void updateMessage()
{
  if(true)//TODO: Condition for movement
  {
    message=(message&RIGHT);
  }
  if(true)//TODO: Condition for OVERFLOWEXCEPTION
  {
    message=(message|OVERFLOWEXCEPTION);
  }else{
    message=(message^OVERFLOWEXCEPTION);
  }
  if(true)//TODO: Condition for MEMALLOCEXCEPTION
  {
    message=(message|MEMALLOCEXCEPTION);
  }else{
    message=(message^MEMALLOCEXCEPTION);
  }
}
