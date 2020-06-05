package com.example.myapp.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.myapp.db.dao.ChatDao;
import com.example.myapp.db.dao.GroupChatDao;
import com.example.myapp.db.dao.LedgerDao;
import com.example.myapp.db.dao.UserDao;
import com.example.myapp.db.entity.ChatEntity;
import com.example.myapp.db.entity.GroupChatEntity;
import com.example.myapp.db.entity.LedgerEntity;
import com.example.myapp.db.entity.UserEntity;

/**
 * Contains the config for the actual database
 */
@Database(entities = {ChatEntity.class, GroupChatEntity.class, LedgerEntity.class, UserEntity.class}, version = 1, exportSchema = false)
@TypeConverters(com.example.myapp.db.DateConverter.class)
public abstract class AppDatabase extends RoomDatabase {

    public static final String DATABASE_NAME = "app-db";

    private static AppDatabase INSTANCE;

    public static AppDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE =
                    Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, DATABASE_NAME)
                            .build();
        }
        return INSTANCE;
    }

    public abstract ChatDao chatDao();
    public abstract GroupChatDao groupChatDao();
    public abstract LedgerDao  ledgerDao();
    public abstract UserDao userDao();

}
