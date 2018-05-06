#include <Wire.h>
#include <Arduino.h>
#include <ESP8266WiFi.h>
#include <ESP8266WiFiMulti.h>
#include <WiFiUdp.h>
#include <ESP8266HTTPClient.h>
#include <ArduinoJson.h>
#include <NTPClient.h>

#define myID "0"
#define URL "http://172.29.9.87:1323/info/"
#define ACCLIMIT 3
#define NTP_OFFSET   60 * 60      // In seconds
#define NTP_INTERVAL 60 * 1000    // In miliseconds
#define NTP_ADDRESS  "europe.pool.ntp.org"

// MPU6050 Slave Device Address
const uint8_t MPU6050SlaveAddress = 0x68;
// Select SDA and SCL pins for I2C communication 
const uint8_t scl = D6;
const uint8_t sda = D7;
// Sensitivity scale factor respective to full scale setting provided in datasheet 
const uint16_t AccelScaleFactor = 16384;
const uint16_t GyroScaleFactor = 131;
// MPU6050 few configuration register addresses
const uint8_t MPU6050_REGISTER_SMPLRT_DIV   =  0x19;
const uint8_t MPU6050_REGISTER_USER_CTRL    =  0x6A;
const uint8_t MPU6050_REGISTER_PWR_MGMT_1   =  0x6B;
const uint8_t MPU6050_REGISTER_PWR_MGMT_2   =  0x6C;
const uint8_t MPU6050_REGISTER_CONFIG       =  0x1A;
const uint8_t MPU6050_REGISTER_GYRO_CONFIG  =  0x1B;
const uint8_t MPU6050_REGISTER_ACCEL_CONFIG =  0x1C;
const uint8_t MPU6050_REGISTER_FIFO_EN      =  0x23;
const uint8_t MPU6050_REGISTER_INT_ENABLE   =  0x38;
const uint8_t MPU6050_REGISTER_ACCEL_XOUT_H =  0x3B;
const uint8_t MPU6050_REGISTER_SIGNAL_PATH_RESET  = 0x68;

ESP8266WiFiMulti WiFiMulti;
WiFiUDP ntpUDP;
NTPClient timeClient(ntpUDP, NTP_ADDRESS, NTP_OFFSET, NTP_INTERVAL);
int16_t AccelX, AccelY, AccelZ, GyroX, GyroY, GyroZ;
StaticJsonBuffer<200> jsonBuffer; // 200 bytes
JsonObject& root = jsonBuffer.createObject();

void setup() {

    Serial.begin(9600);
    Wire.begin(sda, scl);
    MPU6050_Init();
    WiFi.mode(WIFI_STA);
    WiFiMulti.addAP("Hack for Good", "hackforgood2018");
    timeClient.begin();
}

void loop() {

    double Ax, Ay, Az, T, Gx, Gy, Gz, A;
    unsigned long fall_time, regular_time;

    Read_RawValue(MPU6050SlaveAddress, MPU6050_REGISTER_ACCEL_XOUT_H);

    Ax = (double)AccelX/AccelScaleFactor;
    Ay = (double)AccelY/AccelScaleFactor;
    Az = (double)AccelZ/AccelScaleFactor;
    Gx = (double)GyroX/GyroScaleFactor;
    Gy = (double)GyroY/GyroScaleFactor;
    Gz = (double)GyroZ/GyroScaleFactor;

    A = (double)sqrt(Ax * Ax + Ay * Ay + Az * Az);
    fall_time += millis();
    regular_time += millis();

    if(fall_time >= 10000) {
        // Reset fall timer
        fall_time = 0;
        if (A >= ACCLIMIT){
            // Reset regular timer
            regular_time = 0;
            // Define json
            root["device"] = myID;
            root["fell"] = true;
            root["time"] = timeClient.getFormattedTime();
            root["ainfo"] = A;
            root.prettyPrintTo(Serial);
            // Send fall warning to server
            if(sendToServer(root) != HTTP_CODE_OK)
              Serial.println("Error: Could not send http post");
        }
    }

    if(regular_time >= 30000) {
        // Reset regular timer
        regular_time = 0;
        // Define json
        root["device"] = myID;
        root["fell"] = true;
        root["time"] = timeClient.getFormattedTime();
        root["ainfo"] = A;
        root.prettyPrintTo(Serial);
        // Send sensor information to server
        if(sendToServer(root) != HTTP_CODE_OK)
          Serial.println("Error: Could not send http post");
    }

    /*
    Serial.print("Ax: "); Serial.print(Ax);
    Serial.print(" Ay: "); Serial.print(Ay);
    Serial.print(" Az: "); Serial.print(Az);
    Serial.print(" T: "); Serial.print(T);
    Serial.print(" Gx: "); Serial.print(Gx);
    Serial.print(" Gy: "); Serial.print(Gy);
    Serial.print(" Gz: "); Serial.println(Gz);
    */
    
    timeClient.update();
    delay(100);
}

int sendToServer(JsonObject& root) {
  HTTPClient http;
  int httpCode;
  String jsonStr;
  String id(myID);
  String url(URL);
  if((WiFiMulti.run() == WL_CONNECTED)) {
    Serial.println("Wifi Connected");
    http.begin(url+id);
    root.printTo(jsonStr);
    http.addHeader("Content-Type", "application/json");
    httpCode = http.POST(jsonStr);
    http.end();
  } 
  return httpCode;
}

void I2C_Write(uint8_t deviceAddress, uint8_t regAddress, uint8_t data){
  Wire.beginTransmission(deviceAddress);
  Wire.write(regAddress);
  Wire.write(data);
  Wire.endTransmission();
}

// read all 14 register
void Read_RawValue(uint8_t deviceAddress, uint8_t regAddress){
  Wire.beginTransmission(deviceAddress);
  Wire.write(regAddress);
  Wire.endTransmission();
  Wire.requestFrom(deviceAddress, (uint8_t)14);
  AccelX = (((int16_t)Wire.read()<<8) | Wire.read());
  AccelY = (((int16_t)Wire.read()<<8) | Wire.read());
  AccelZ = (((int16_t)Wire.read()<<8) | Wire.read());
  GyroX = (((int16_t)Wire.read()<<8) | Wire.read());
  GyroY = (((int16_t)Wire.read()<<8) | Wire.read());
  GyroZ = (((int16_t)Wire.read()<<8) | Wire.read());
}

//configure MPU6050
void MPU6050_Init(){
  delay(150);
  I2C_Write(MPU6050SlaveAddress, MPU6050_REGISTER_SMPLRT_DIV, 0x07);
  I2C_Write(MPU6050SlaveAddress, MPU6050_REGISTER_PWR_MGMT_1, 0x01);
  I2C_Write(MPU6050SlaveAddress, MPU6050_REGISTER_PWR_MGMT_2, 0x00);
  I2C_Write(MPU6050SlaveAddress, MPU6050_REGISTER_CONFIG, 0x00);
  I2C_Write(MPU6050SlaveAddress, MPU6050_REGISTER_GYRO_CONFIG, 0x00);//set +/-250 degree/second full scale
  I2C_Write(MPU6050SlaveAddress, MPU6050_REGISTER_ACCEL_CONFIG, 0x00);// set +/- 2g full scale
  I2C_Write(MPU6050SlaveAddress, MPU6050_REGISTER_FIFO_EN, 0x00);
  I2C_Write(MPU6050SlaveAddress, MPU6050_REGISTER_INT_ENABLE, 0x01);
  I2C_Write(MPU6050SlaveAddress, MPU6050_REGISTER_SIGNAL_PATH_RESET, 0x00);
  I2C_Write(MPU6050SlaveAddress, MPU6050_REGISTER_USER_CTRL, 0x00);
}
