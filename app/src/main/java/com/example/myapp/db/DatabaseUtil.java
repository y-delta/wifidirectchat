package com.example.myapp.db;

import android.os.AsyncTask;

import com.example.myapp.MainActivity;
import com.example.myapp.connections.SendReceive;
import com.example.myapp.db.entity.ChatEntity;
import com.example.myapp.db.entity.GroupChatEntity;
import com.example.myapp.db.entity.LedgerEntity;

import java.util.Random;

/**
 * Generates dummy data and inserts them into the database.
 * Also have helper methods to do operation in a different thread
 */
public class DatabaseUtil {

    private static final String[] RECEIVER_MESSAGES = new String[]{
            "new messsage1","new messsage2","new messsage3","new messsage5","new messsage4"
    };

    //add message content here of type GroupChatEntity to retrieve from socket
    public static GroupChatEntity getMessage()
    {
        GroupChatEntity entry = new GroupChatEntity();

        String message = MainActivity.recievedGroupMessage;
        entry.setChatContent(message);

        return entry;
    }

    public static LedgerEntity getLedger()
    {
        LedgerEntity updatedLedger = new LedgerEntity();

        String location;
        String landmark;
        //fetch from mainActivity/SendRecieve and add to this object

        return updatedLedger;
    }

    public static String generateRandomReceiverMessage() {
        Random rnd = new Random();
        int commentsNumber = rnd.nextInt(5) + 1;
        String message = getMessage().getChatContent();

       // return RECEIVER_MESSAGES[commentsNumber];
        return message; //might crash
    }

    public static void addSenderChatToDataBase(AppDatabase db, ChatEntity chatEntitySender) {
        new addAsyncTask(db).execute(chatEntitySender);
    }

    public static void addReceiverChatToDataBase(AppDatabase db, ChatEntity chatEntityReceiver) {
        new addAsyncTask(db).execute(chatEntityReceiver);
    }

    public static void addSenderGroupChatToDataBase(AppDatabase db, GroupChatEntity groupChatEntitySender) {
        new addAsyncTask1(db).execute(groupChatEntitySender);
    }

    public static void addReceiverGroupChatToDataBase(AppDatabase db, GroupChatEntity groupChatEntityReceiver) {
        new addAsyncTask1(db).execute(groupChatEntityReceiver);
    }

    public static void addNewLedgerToDataBase(AppDatabase db, LedgerEntity ledgerEntitySender) {
        new addAsyncTask2(db).execute(ledgerEntitySender);
    }

    public static void addReceiverGroupChatToDataBase(AppDatabase db, LedgerEntity ledgerEntityReceiver) {
        new addAsyncTask2(db).execute(ledgerEntityReceiver);
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
