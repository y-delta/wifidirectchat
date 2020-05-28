package com.example.myapp.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.myapp.db.entity.UserEntity;

import java.util.List;

@Dao
public interface UserDao {
    @Query("SELECT * FROM users")
    LiveData<List<UserEntity>> loadAllChatHistory();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserEntity chats);
}
