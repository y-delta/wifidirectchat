package com.example.myapp.db;

import android.os.AsyncTask;
import android.provider.ContactsContract;

import com.example.myapp.MainActivity;
import com.example.myapp.connections.SendReceive;
import com.example.myapp.db.entity.ChatEntity;
import com.example.myapp.db.entity.GroupChatEntity;
import com.example.myapp.db.entity.LedgerEntity;
import com.example.myapp.db.entity.UserEntity;

import java.util.List;
import java.util.Random;

/**
 * Generates dummy data and inserts them into the database.
 * Also have helper methods to do operation in a different thread
 */
public class DatabaseUtil {

    public static void addSenderChatToDataBase(AppDatabase db, ChatEntity chatEntitySender) {
        new addAsyncTask(db).execute(chatEntitySender);
    }

    public static void addReceiverChatToDataBase(AppDatabase db, ChatEntity chatEntityReceiver) {
        new addAsyncTask(db).execute(chatEntityReceiver);
    }

    public static void addSenderGroupChatToDataBase(AppDatabase db, GroupChatEntity groupChatEntitySender) {
        new addAsyncTask1(db).execute(groupChatEntitySender);
    }

    public static void addUserToDataBase(AppDatabase db, UserEntity userEntity) {
        new addAsyncTask3(db).execute(userEntity);
    }

    public static void addNewLedgerToDataBase(AppDatabase db, LedgerEntity ledgerEntitySender) {
        new addAsyncTask2(db).execute(ledgerEntitySender);
    }

    public static void updateReceived(AppDatabase db, ChatEntity chatEntityReceiver) {
        new addAsyncTask4(db).execute(chatEntityReceiver);
    }

    /**
     * The operations accessing the db should be performed in a different thread other than main UI thread
     */
    private static class addAsyncTask extends AsyncTask<ChatEntity, Void, Void> {

        private AppDatabase db;

        addAsyncTask(AppDatabase appDatabase) {
            db = appDatabase;
        }

        @Override
        protected Void doInBackground(final ChatEntity... params) {
            insertChatData(db, params[0]);
            return null;
        }
    }

    private static class addAsyncTask1 extends AsyncTask<GroupChatEntity, Void, Void> {

        private AppDatabase db;

        addAsyncTask1(AppDatabase appDatabase) {
            db = appDatabase;
        }

        @Override
        protected Void doInBackground(GroupChatEntity... groupChatEntities) {
            insertGroupChatData(db, groupChatEntities[0]);
            return null;
        }
    }

    private static class addAsyncTask2 extends AsyncTask<LedgerEntity, Void, Void> {

        private AppDatabase db;

        addAsyncTask2(AppDatabase appDatabase) {
            db = appDatabase;
        }

        @Override
        protected Void doInBackground(LedgerEntity... ledgerEntities) {
            insertLedgerData(db, ledgerEntities[0]);
            return null;
        }
    }

    private static class addAsyncTask3 extends AsyncTask<UserEntity, Void, Void> {

        private AppDatabase db;

        addAsyncTask3(AppDatabase appDatabase) {
            db = appDatabase;
        }

        @Override
        protected Void doInBackground(UserEntity... userEntities) {
            insertUserName(db, userEntities[0]);
            return null;
        }

    }

    private static class addAsyncTask4 extends AsyncTask<ChatEntity, Void, Void> {

        private AppDatabase db;

        addAsyncTask4(AppDatabase appDatabase) {
            db = appDatabase;
        }

        @Override
        protected Void doInBackground(ChatEntity... chatEntities) {
            updateReceivedValue(db, chatEntities[0]);
            return null;
        }
    }

    public static void updateReceivedValue(AppDatabase db, ChatEntity chatEntity) {
        db.beginTransaction();
        try {
            db.chatDao().update(chatEntity.getMessageReceived(), chatEntity.getId());
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public static void insertUserName(AppDatabase db, UserEntity userEntity) {
        db.beginTransaction();
        try {
            db.userDao().insert(userEntity);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public static void insertChatData(AppDatabase db, ChatEntity chatEntity) {
        db.beginTransaction();
        try {
            db.chatDao().insert(chatEntity);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public static void insertGroupChatData(AppDatabase db, GroupChatEntity groupChatEntity) {
        db.beginTransaction();
        try {
            db.groupChatDao().insert(groupChatEntity);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public static void insertLedgerData(AppDatabase db, LedgerEntity ledgerEntity) {
        db.beginTransaction();
        try {
            db.ledgerDao().insert(ledgerEntity);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }



}
