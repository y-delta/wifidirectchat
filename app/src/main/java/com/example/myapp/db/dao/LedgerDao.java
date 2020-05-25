package com.example.myapp.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.myapp.db.entity.LedgerEntity;

import java.util.List;

@Dao
public interface LedgerDao {
    @Query("SELECT * FROM ledger")
    LiveData<List<LedgerEntity>> loadAllChatHistory();

    @Query("SELECT COUNT(location) FROM ledger")
    LiveData<Integer> getLedgerCount();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(LedgerEntity products);
}
