package com.example.myapp.db.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

@Entity(tableName = "users")
public class UserEntity {

    @PrimaryKey
    @NotNull
    private String userId;
    private String username;
    private int id;

    public String getUserId() {return userId;}

    public void setUserId(String userId) {this.userId = userId;}

    public String getUsername() {return username;}

    public void setUsername(String username) {this.username = username;}

    public int getId() {return id;}

    public void setId(int id) {this.id = id;}

    public UserEntity(){}
}
