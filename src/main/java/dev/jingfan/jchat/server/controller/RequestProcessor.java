package dev.jingfan.jchat.server.controller;
import dev.jingfan.jchat.common.model.entity.*;
import dev.jingfan.jchat.common.protocol.ProtocolActions;
import dev.jingfan.jchat.server.DataBuffer;
import dev.jingfan.jchat.server.OnlineClientIOCache;
import dev.jingfan.jchat.server.model.service.UserService;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CopyOnWriteArrayList;

/** Handles the serialized request stream for one connected dev.jingfan.jchat.client. */
public class RequestProcessor implements Runnable {
    private final Socket currentClientSocket;
    private User authenticatedUser;

    public RequestProcessor(Socket currentClientSocket){
        this.currentClientSocket = currentClientSocket;
    }

    public void run() {
        boolean flag = true;
        try{
            OnlineClientIOCache currentClientIOCache = new OnlineClientIOCache(
                    new ObjectInputStream(currentClientSocket.getInputStream()),
                    new ObjectOutputStream(currentClientSocket.getOutputStream()));
            while(flag){

                Request request = (Request)currentClientIOCache.getOis().readObject();
                System.out.println("Server received request: " + request.getAction());

                String actionName = request.getAction();
                if (ProtocolActions.REGISTER.equals(actionName)) {
                    register(currentClientIOCache, request);
                } else if (ProtocolActions.LOGIN.equals(actionName)) {
                    login(currentClientIOCache, request);
                } else if (ProtocolActions.EXIT.equals(actionName)) {
                    flag = logout(currentClientIOCache, request);
                } else if (ProtocolActions.CHAT.equals(actionName)) {
                    chat(request);
                } else if (ProtocolActions.SHAKE.equals(actionName)) {
                    shake(request);
                } else if (ProtocolActions.FILE_OFFER.equals(actionName)) {
                    toSendFile(request);
                } else if (ProtocolActions.FILE_ACCEPT.equals(actionName)) {
                    agreeReceiveFile(request);
                } else if (ProtocolActions.FILE_REFUSE.equals(actionName)) {
                    refuseReceiveFile(request);
                }
            }
        } catch (Exception e) {
            System.err.println("Client connection ended: " + e.getMessage());
        } finally {
            cleanupUnexpectedDisconnect();
        }
    }

    private void refuseReceiveFile(Request request) throws IOException {
        FileInfo sendFile = (FileInfo)request.getAttribute("sendFile");
        Response response = new Response();
        response.setType(ResponseType.REFUSERECEIVEFILE);
        response.setData("sendFile", sendFile);
        response.setStatus(ResponseStatus.OK);

        OnlineClientIOCache ocic = DataBuffer.onlineUserIOCacheMap.get(sendFile.getFromUser().getId());
        this.sendResponse(ocic, response);
    }

    private void agreeReceiveFile(Request request) throws IOException {
        FileInfo sendFile = (FileInfo)request.getAttribute("sendFile");

        Response response = new Response();
        response.setType(ResponseType.AGREERECEIVEFILE);
        response.setData("sendFile", sendFile);
        response.setStatus(ResponseStatus.OK);
        OnlineClientIOCache sendIO = DataBuffer.onlineUserIOCacheMap.get(sendFile.getFromUser().getId());
        this.sendResponse(sendIO, response);

        Response response2 = new Response();
        response2.setType(ResponseType.RECEIVEFILE);
        response2.setData("sendFile", sendFile);
        response2.setStatus(ResponseStatus.OK);
        OnlineClientIOCache receiveIO = DataBuffer.onlineUserIOCacheMap.get(sendFile.getToUser().getId());
        this.sendResponse(receiveIO, response2);
    }

    public boolean logout(OnlineClientIOCache client_iocache, Request request) throws IOException{
        System.out.println(currentClientSocket.getInetAddress().getHostAddress()
                + ":" + currentClientSocket.getPort() + " disconnected");

        User user = (User)request.getAttribute("user");

        DataBuffer.onlineUserIOCacheMap.remove(user.getId());

        DataBuffer.onlineUsersMap.remove(user.getId());

        Response response = new Response();
        response.setType(ResponseType.LOGOUT);
        response.setData("logoutUser", user);
        client_iocache.getOos().writeObject(response);
        client_iocache.getOos().flush();
        currentClientSocket.close();

        DataBuffer.onlineUserTableModel.remove(user.getId());
        iteratorResponse(response);
        authenticatedUser = null;

        return false;
    }

    public void register(OnlineClientIOCache client_iocache, Request request) throws IOException {
        User user = (User)request.getAttribute("user");
        UserService userService = new UserService();
        userService.addUser(user);

        Response response = new Response();
        response.setStatus(ResponseStatus.OK);
        response.setData("user", user);

        client_iocache.getOos().writeObject(response);
        client_iocache.getOos().flush();

        DataBuffer.registeredUserTableModel.add(new String[]{
                String.valueOf(user.getId()),
                user.getNickname(),
                String.valueOf(user.getSex())
        });
    }

    public void login(OnlineClientIOCache client_iocache, Request request) throws IOException {
        String idStr = (String)request.getAttribute("id");
        String password = (String) request.getAttribute("password");
        UserService userService = new UserService();
        User user = userService.login(Long.parseLong(idStr), password);

        Response response = new Response();
        if(null != user){
            if(DataBuffer.onlineUsersMap.containsKey(user.getId())){
                response.setStatus(ResponseStatus.OK);
                response.setData("msg", "This account is already signed in elsewhere.");
                client_iocache.getOos().writeObject(response);
                client_iocache.getOos().flush();
            }else {
                DataBuffer.onlineUsersMap.put(user.getId(), user);
                authenticatedUser = user;

                response.setData("onlineUsers",
                        new CopyOnWriteArrayList<User>(DataBuffer.onlineUsersMap.values()));

                response.setStatus(ResponseStatus.OK);
                response.setData("user", user);
                client_iocache.getOos().writeObject(response);
                client_iocache.getOos().flush();

                Response response2 = new Response();
                response2.setType(ResponseType.LOGIN);
                response2.setData("loginUser", user);
                iteratorResponse(response2);

                DataBuffer.onlineUserIOCacheMap.put(user.getId(),client_iocache);

                DataBuffer.onlineUserTableModel.add(
                        new String[]{String.valueOf(user.getId()),
                                user.getNickname(),
                                String.valueOf(user.getSex())});
            }
        }else{
            response.setStatus(ResponseStatus.OK);
            response.setData("msg", "The account or password is incorrect.");
            client_iocache.getOos().writeObject(response);
            client_iocache.getOos().flush();
        }
    }

    public void chat(Request request) throws IOException {
        Message msg = (Message)request.getAttribute("msg");
        Response response = new Response();
        response.setStatus(ResponseStatus.OK);
        response.setType(ResponseType.CHAT);
        response.setData("txtMsg", msg);

        if(msg.getToUser() != null){
            OnlineClientIOCache io = DataBuffer.onlineUserIOCacheMap.get(msg.getToUser().getId());
            sendResponse(io, response);
        }else{
            for(Long id : DataBuffer.onlineUserIOCacheMap.keySet()){
                if(msg.getFromUser().getId() == id ){ continue; }
                sendResponse(DataBuffer.onlineUserIOCacheMap.get(id), response);
            }
        }
    }

    public static void board(String str) throws IOException {
        User user = new User(1,"admin");
        Message msg = new Message();
        msg.setFromUser(user);
        msg.setSendTime(new Date());

        DateFormat df = new SimpleDateFormat("HH:mm:ss");
        StringBuffer sb = new StringBuffer();
        sb.append(" ").append(df.format(msg.getSendTime())).append(" ");
        sb.append("System announcement\n  ").append(str).append("\n");
        msg.setMessage(sb.toString());

        Response response = new Response();
        response.setStatus(ResponseStatus.OK);
        response.setType(ResponseType.BOARD);
        response.setData("txtMsg", msg);

        for (Long id : DataBuffer.onlineUserIOCacheMap.keySet()) {
            sendResponse_sys(DataBuffer.onlineUserIOCacheMap.get(id), response);
        }
    }

    public static void remove(User user_) throws IOException{
        User user = new User(1,"admin");
        Message msg = new Message();
        msg.setFromUser(user);
        msg.setSendTime(new Date());
        msg.setToUser(user_);

        StringBuffer sb = new StringBuffer();
        DateFormat df = new SimpleDateFormat("HH:mm:ss");
        sb.append(" ").append(df.format(msg.getSendTime())).append(" ");
        sb.append("System notice\n  You have been signed out by the administrator.\n");
        msg.setMessage(sb.toString());

        Response response = new Response();
        response.setStatus(ResponseStatus.OK);
        response.setType(ResponseType.REMOVE);
        response.setData("txtMsg", msg);

        OnlineClientIOCache io = DataBuffer.onlineUserIOCacheMap.get(msg.getToUser().getId());
        sendResponse_sys(io, response);
    }

    public static void chat_sys(String str,User user_) throws IOException{
        User user = new User(1,"admin");
        Message msg = new Message();
        msg.setFromUser(user);
        msg.setSendTime(new Date());
        msg.setToUser(user_);

        DateFormat df = new SimpleDateFormat("HH:mm:ss");
        StringBuffer sb = new StringBuffer();
        sb.append(" ").append(df.format(msg.getSendTime())).append(" ");
        sb.append("System notice\n  ").append(str).append("\n");
        msg.setMessage(sb.toString());

        Response response = new Response();
        response.setStatus(ResponseStatus.OK);
        response.setType(ResponseType.CHAT);
        response.setData("txtMsg", msg);

        OnlineClientIOCache io = DataBuffer.onlineUserIOCacheMap.get(msg.getToUser().getId());
        sendResponse_sys(io, response);
    }

    public void shake(Request request)throws IOException {
        Message msg = (Message) request.getAttribute("msg");

        DateFormat df = new SimpleDateFormat("HH:mm:ss");
        StringBuffer sb = new StringBuffer();
        sb.append(" ").append(msg.getFromUser().getNickname())
                .append("(").append(msg.getFromUser().getId()).append(") ")
                .append(df.format(msg.getSendTime())).append("\n  sent you a window nudge.\n");
        msg.setMessage(sb.toString());

        Response response = new Response();
        response.setStatus(ResponseStatus.OK);
        response.setType(ResponseType.SHAKE);
        response.setData("ShakeMsg", msg);

        OnlineClientIOCache io = DataBuffer.onlineUserIOCacheMap.get(msg.getToUser().getId());
        sendResponse(io, response);
    }

    public void toSendFile(Request request)throws IOException{
        Response response = new Response();
        response.setStatus(ResponseStatus.OK);
        response.setType(ResponseType.TOSENDFILE);
        FileInfo sendFile = (FileInfo)request.getAttribute("file");
        response.setData("sendFile", sendFile);

        OnlineClientIOCache ioCache = DataBuffer.onlineUserIOCacheMap.get(sendFile.getToUser().getId());
        sendResponse(ioCache, response);
    }

    private void iteratorResponse(Response response) throws IOException {
        for(OnlineClientIOCache onlineUserIO : DataBuffer.onlineUserIOCacheMap.values()){
            ObjectOutputStream oos = onlineUserIO.getOos();
            synchronized (oos) {
                oos.writeObject(response);
                oos.flush();
            }
        }
    }

    private void sendResponse(OnlineClientIOCache onlineUserIO, Response response)throws IOException {
        if (onlineUserIO == null) {
            return;
        }
        ObjectOutputStream oos = onlineUserIO.getOos();
        synchronized (oos) {
            oos.writeObject(response);
            oos.flush();
        }
    }

    private static void sendResponse_sys(OnlineClientIOCache onlineUserIO, Response response)throws IOException {
        if (onlineUserIO == null) {
            return;
        }
        ObjectOutputStream oos = onlineUserIO.getOos();
        synchronized (oos) {
            oos.writeObject(response);
            oos.flush();
        }
    }

    private void cleanupUnexpectedDisconnect() {
        if (authenticatedUser == null) {
            return;
        }

        long userId = authenticatedUser.getId();
        DataBuffer.onlineUserIOCacheMap.remove(userId);
        DataBuffer.onlineUsersMap.remove(userId);
        DataBuffer.onlineUserTableModel.remove(userId);

        Response response = new Response();
        response.setType(ResponseType.LOGOUT);
        response.setData("logoutUser", authenticatedUser);
        try {
            iteratorResponse(response);
        } catch (IOException exception) {
            System.err.println("Could not broadcast disconnect: " + exception.getMessage());
        }
        try {
            currentClientSocket.close();
        } catch (IOException exception) {
            System.err.println("Could not close client socket: " + exception.getMessage());
        }
        authenticatedUser = null;
    }
}
