package com.example.myapp.db.entity;

import androidx.navigation.NavOptionsDsl;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

import java.util.Date;

/**
 * Represent a table in database
 */

@Entity(tableName = "chats", primaryKeys = {"sender", "receiver", "date"})
public class ChatEntity {
    private int id;
    private String chatType;
    private String chatContent;
    @NotNull
    private Boolean messageReceived = false;
    @NotNull private Date date;
    @NotNull private String sender;
    @NotNull private String receiver;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NotNull
    public String getSender() {return sender;}

    public void setSender(String sender) {this.sender = sender;}

    @NotNull
    public String getReceiver() {return receiver;}

    public void setReceiver(String receiver) {this.receiver = receiver;}

    public String getChatType() {
        return chatType;
    }

    public void setChatType(String chatType) {
        this.chatType = chatType;
    }

    public String getChatContent() {
        return chatContent;
    }

    public void setChatContent(String chatContent) {
        this.chatContent = chatContent;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Boolean getMessageReceived() {return messageReceived;}

    public void setMessageReceived(Boolean messageReceived) {this.messageReceived = messageReceived;}


    public ChatEntity() {
    }
}
