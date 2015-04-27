/*
 * Copyright 2015 andreas.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sensemusic;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import sense.jsense.SenseService;
import sense.jsense.util.GeoLoc;
import sense.jsense.util.SensorPub;
import sense.jsense.util.UpdateListener;

/**
 *
 * @author andreas
 */
public class SenseMusic {
    
    private final String WIFI_DISCONNECTED = "disconnected";
    private final String WIFI_HOME = "HALLNET_5";
    private final String WIFI_EDUROAM = "eduroam";
    
    private SenseService sense;
    
    private Date timeOfLocationUpdate = new Date();
    private Date timeOfWifiConnect = new Date();
    private Date timeOfWifiDisconnect = new Date();
    
    private final GeoLoc home = new GeoLoc(59.40365215, 17.94339358);
    private final GeoLoc electrum = new GeoLoc(59.404694, 17.949911);
    private GeoLoc userLocation;
    private String wifiSSID = WIFI_DISCONNECTED;
    
    private int pubCounter = 0;
    
    public SenseMusic() {
        sense = new SenseService("ec2.hallnet.eu", 1337, SenseService.INTERVAL_SLOW, true);
        
        sense.subscribe("name:PlaySomeMusic", new PlayMusicCommandListener());
        sense.subscribe("name:PhoneLocation", new PhoneLocationListener());
        sense.subscribe("name:PhoneWifiConnect", new PhoneWifiConnectListener());
        sense.subscribe("name: PhoneWifiDisconnect", new PhoneWifiDisconnectListener());
    }
    
    private class PlayMusicCommandListener implements UpdateListener {
        @Override
        public void onUpdate(SensorPub sp) {
            System.out.println("User wants to play some music!");
            System.out.println("Value: " + sp.getValue());
            
            if(userLocation != null) {
                System.out.println("Distance home: " + userLocation.distanceTo(home));
                System.out.println("Distance electrum: " + userLocation.distanceTo(electrum));
                
                if(userLocation.distanceTo(home) < 100 || wifiSSID.equals(WIFI_HOME)) {
                    System.out.println("I'm at home, probably want to play music on PC.");
                    sense.publish(new SenseMusicContext("I'm at home, probably want to play music on PC."));
                }
                else if(userLocation.distanceTo(electrum) < 100 || wifiSSID.equals(WIFI_EDUROAM)){
                    System.out.println("I'm in Electrum, laptop is a good candidate.");
                    sense.publish(new SenseMusicContext("I'm in Electrum, laptop is a good candidate."));
                }
                else {
                    System.out.println("I'm mobile. Should I just play back on the phone?");
                    sense.publish(new SenseMusicContext("I'm mobile. Should I just play back on the phone?"));
                }
            }
        }   
    }
    
    private class PhoneLocationListener implements UpdateListener {
        @Override
        public void onUpdate(SensorPub sp) {    
            System.out.println("New user location: " + sp.getValue());
            userLocation = new GeoLoc(sp.getValue().toString());
            timeOfLocationUpdate = new Date();
        }
    }
    
    private class PhoneWifiConnectListener implements UpdateListener {
        @Override
        public void onUpdate(SensorPub sp) {
            System.out.println("User connected to new ssid: " + sp.getValue());
            wifiSSID = sp.getValue().toString();
            timeOfWifiConnect = new Date();
        }
    }
    
    private class PhoneWifiDisconnectListener implements UpdateListener {
        @Override
        public void onUpdate(SensorPub sp) {
            System.out.println("User disconnected from ssid: " + sp.getValue());
            wifiSSID = WIFI_DISCONNECTED;
            timeOfWifiDisconnect = new Date();
        }
    }
    
    private class SenseMusicContext extends SensorPub {
        public SenseMusicContext(String description) {
            super("SenseMusicContext", description, SensorPub.TYPE_INTEGER, pubCounter, new Date());
            pubCounter++;
        }
    }
    
    public static void main(String[] args) {
        SenseMusic service = new SenseMusic();
        try {
            Object lock = new Object();
            synchronized (lock) {
                while(true)
                    lock.wait();
            }
        } catch (InterruptedException ex) {}
        
        System.out.println("I was interrupted...exit?");
    }
}
