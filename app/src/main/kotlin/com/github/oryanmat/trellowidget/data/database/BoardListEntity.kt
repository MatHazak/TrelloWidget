package com.github.oryanmat.trellowidget.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.github.oryanmat.trellowidget.data.model.BoardList
import com.github.oryanmat.trellowidget.data.model.Card
import com.github.oryanmat.trellowidget.util.Converters

@Entity(tableName = "boardList_table")
@TypeConverters(Converters::class)
data class BoardListEntity(
    @PrimaryKey val id: String,
    val name: String,
    val cards: List<Card>
) {
    fun toBoardList() = BoardList(id, name, cards)

    companion object {
        fun fromBoardList(boardList: BoardList) =
            BoardListEntity(boardList.id, boardList.name, boardList.cards)
    }
}
