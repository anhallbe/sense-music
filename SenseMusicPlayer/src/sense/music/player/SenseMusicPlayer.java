/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sense.music.player;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import sense.jsense.SenseService;
import sense.jsense.util.SensorPub;
import sense.jsense.util.UpdateListener;

/**
 * This is a very light-weight service that should run on my laptop/PC/phone.
 * It will simply listen for commands/events that tells it to start playing some music...
 * @author andreas
 */
public class SenseMusicPlayer {
    
    private SenseService sense;     //Service used for subscription
    private Clip clip;              //Audio clip
    
    public static final String CTX_LAPTOP = "laptop";   //Used if application is executing on laptop.
    public static final String CTX_PC = "pc";           //Used if application is executing on pc.
    public static final String CTX_PHONE = "phone";     //Used if application is executing on phone.
    
    
    public SenseMusicPlayer(String context) {
        sense = new SenseService("ec2.hallnet.eu", 1337, SenseService.INTERVAL_FAST, true);
        
        String subscriptionQuery = "name:SenseMusicContext AND ";
        
        try {
            clip = AudioSystem.getClip();
        } catch (LineUnavailableException ex) {
            Logger.getLogger(SenseMusicPlayer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        switch(context) {
            case CTX_LAPTOP:
                subscriptionQuery += "laptop";
                break;
            case CTX_PC: 
                subscriptionQuery += "home OR PC";
                break;
            case CTX_PHONE:
                subscriptionQuery += "mobile";
                break;
        }
        
        sense.subscribe(subscriptionQuery, new OnPlayMusicListener());
    }
    

    private class OnPlayMusicListener implements UpdateListener {
        @Override
        public void onUpdate(SensorPub sp) {
            Random rand = new Random();
            String track = "music/" + rand.nextInt(19) + ".wav";
            File trackFile = new File(Paths.get(track).toUri());
            System.out.println("I'm playing some music: (track " + track + ")");
            System.out.println("Source: " + trackFile.getAbsolutePath());
            
            try {
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(trackFile);
                if(clip.isOpen())
                    clip.close();
                clip.stop();
                clip.open(audioStream);
                clip.start();
            } catch (UnsupportedAudioFileException ex) {
                Logger.getLogger(SenseMusicPlayer.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(SenseMusicPlayer.class.getName()).log(Level.SEVERE, null, ex);
            } catch (LineUnavailableException ex) {
                Logger.getLogger(SenseMusicPlayer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static void main(String[] args) {
        SenseMusicPlayer player = new SenseMusicPlayer(SenseMusicPlayer.CTX_LAPTOP);
        try {
            Object lock = new Object();
            synchronized (lock) {
                while(true)
                    lock.wait();
            }
        } catch (InterruptedException ex) {}
    }
}
