package dev.jingfan.jchat.common.model.entity;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/** Serializable event or command result sent from the server to a dev.jingfan.jchat.client. */
public class Response implements Serializable {
    private static final long serialVersionUID = 1689541820872288991L;

    private ResponseStatus status;

    private ResponseType type;

    private HashMap<String, Object> dataMap;

    public Response(){
        this.status = ResponseStatus.OK;
        this.dataMap = new HashMap<>();
    }

    public ResponseStatus getStatus() {
        return status;
    }

    public void setStatus(ResponseStatus status) {
        this.status = status;
    }

    public ResponseType getType() {
        return type;
    }

    public void setType(ResponseType type) {
        this.type = type;
    }

    public Map<String, Object> getDataMap() {
        return dataMap;
    }

    public void setDataMap(Map<String, Object> dataMap) {
        this.dataMap = new HashMap<>(dataMap);
    }

    public void setData(String name, Object value){
        this.dataMap.put(name, value);
    }

    public Object getData(String name){
        return this.dataMap.get(name);
    }

    public void removeData(String name){
        this.dataMap.remove(name);
    }

    public void clearData(){
        this.dataMap.clear();
    }
}
