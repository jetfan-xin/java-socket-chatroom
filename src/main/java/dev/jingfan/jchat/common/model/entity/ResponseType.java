package dev.jingfan.jchat.common.model.entity;

/** Asynchronous events understood by the desktop dev.jingfan.jchat.client. */
public enum ResponseType {
    TEXT,
    TOSENDFILE,
    AGREERECEIVEFILE,
    REFUSERECEIVEFILE,
    RECEIVEFILE,
    LOGIN,
    LOGOUT,
    CHAT,
    SHAKE,
    OTHER,
    BOARD,
    REMOVE
}
