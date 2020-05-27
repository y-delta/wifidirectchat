package com.example.myapp.db.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "group_chats")
public class GroupChatEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String chatType;
    private String chatContent;
    private Date date;
    private String senderId;
    private String messageId;
    private Boolean sentByMe;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getChatType() {return chatType;}

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

    public String getSenderId() {return senderId;}

    public void setSenderId(String senderId) { this.senderId = senderId;}

    public String getMessageId() {return messageId;}

    public void setMessageId(String messageId) {this.messageId = messageId;}

    public Boolean getSentByMe() {return sentByMe;}

    public void setSentByMe(Boolean sentByMe) {this.sentByMe = sentByMe;}

    public GroupChatEntity() {
    }
}
