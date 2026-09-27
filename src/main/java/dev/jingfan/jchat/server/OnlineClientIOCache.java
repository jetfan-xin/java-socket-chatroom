package dev.jingfan.jchat.server;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
/** Holds the single object-stream pair associated with one connected dev.jingfan.jchat.client. */
public class OnlineClientIOCache {
    private final ObjectInputStream ois;
    private final ObjectOutputStream oos;

    public OnlineClientIOCache(ObjectInputStream ois, ObjectOutputStream oos){
        this.ois = ois;
        this.oos = oos;
    }
    public ObjectOutputStream getOos(){
        return oos;
    }
    public ObjectInputStream getOis() {
        return ois;
    }
}
