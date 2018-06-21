package io.github.bananenpuree1997;

import java.io.File;
import java.net.URL;

import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.EndOfMediaEvent;
import javax.media.Manager;
import javax.media.Player;
import javax.media.RealizeCompleteEvent;
import javax.media.Time;

public final class SoundEffect {

    private final Player player;
    private final int durationMillis;

    public SoundEffect(String path, int durationMillis) {
        this.durationMillis = durationMillis;
        try {
            final URL url = new File(path).toURL();
            // Create the media player
            this.player = Manager.createPlayer(url);
            final Object lock = new Object();
            this.player.addControllerListener(new ControllerListener() {
                // @Override
                public void controllerUpdate(ControllerEvent event) {
                    if (!(event instanceof RealizeCompleteEvent)) {
                        return;
                    }
                    synchronized (lock) {
                        lock.notifyAll();
                    }
                }
            });
            this.player.realize();
            // Wait for the controller to be realized
            synchronized (lock) {
                lock.wait();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public SoundEffect(String path) {
        this(path, -1);
    }

    public void setVolume(float volume) {
        this.player.getGainControl().setLevel(volume);
    }

    public void loop() {
        play();
        this.player.addControllerListener(new ControllerListener() {
            // @Override
            public void controllerUpdate(ControllerEvent event) {
                if (!(event instanceof EndOfMediaEvent)) {
                    return;
                }
                play();
            }
        });
    }

    public void play() {
        this.player.stop();
        if (this.durationMillis != -1) {
            this.player.setStopTime(new Time(this.durationMillis));
        }
        this.player.setMediaTime(new Time(0));
        this.player.start();
    }

    public void stop() {
        this.player.stop();
    }

    public void dispose() {
        this.player.stop();
        this.player.deallocate();
    }
}
