package com.example.myapp.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.myapp.db.entity.ChatEntity;

import java.util.List;

/**
 * Have method for accessing the database
 */
@Dao
public interface ChatDao {
    @Query("SELECT * FROM chats")
    LiveData<List<ChatEntity>> loadAllChatHistory();

    @Query("SELECT * FROM chats WHERE sender=:sender ORDER BY date")
    LiveData<List<ChatEntity>> loadAllChatHistoryByUser(String sender);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ChatEntity chats);
}
