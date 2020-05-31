package com.example.myapp.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.myapp.db.entity.ChatEntity;

import java.util.List;

/**
 * Have method for accessing the database
 */
@Dao
public interface ChatDao {
    @Query("SELECT * FROM chats")
    LiveData<List<ChatEntity>> loadAllChatHistory();

    @Query("SELECT * FROM chats WHERE receiver=:contact UNION SELECT * from chats WHERE sender=:contact ORDER BY date")
    LiveData<List<ChatEntity>> loadAllChatHistoryByContact(String contact);

    @Query("UPDATE chats SET messageReceived=:value WHERE id=:id AND chatType LIKE '%sender'") //  WHERE date=MAX(date)
    void update(Boolean value, int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ChatEntity chats);

}
