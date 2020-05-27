package com.example.myapp.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.myapp.db.entity.GroupChatEntity;

import java.util.List;

@Dao
public interface GroupChatDao {
    @Query("SELECT * FROM group_chats")
    LiveData<List<GroupChatEntity>> loadAllChatHistory();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(GroupChatEntity products);
}
