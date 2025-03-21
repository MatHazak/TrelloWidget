package com.github.oryanmat.trellowidget.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface BoardListDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(boardList: BoardListEntity)

    @Query("SELECT * FROM boardList_table WHERE id = :listId")
    fun getBoardListById(listId: String): BoardListEntity?

    @Query("DELETE FROM boardList_table WHERE id = :listId")
    suspend fun deleteBoardListById(listId: String)
}