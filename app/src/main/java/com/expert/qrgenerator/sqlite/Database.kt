package com.expert.qrgenerator.sqlite

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.*


class Database(context: Context) : SQLiteOpenHelper(context, databaseName, null, databaseVersion) {

    companion object {
        private const val databaseVersion = 1
        private const val databaseName = "magic_qr_generator_database"
        private const val KEY_ID = "id"
        private const val KEY_TABLE_NAME = "table_name"
        private const val TABLE = "tables"
    }

    override fun onCreate(db: SQLiteDatabase?) {
//        val createTable = ("CREATE TABLE " + TABLE + "("
//                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
//                + KEY_TABLE_NAME + " TEXT )")
//        db!!.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // Drop older table if existed
//        db!!.execSQL("DROP TABLE IF EXISTS $TABLE")
        onCreate(db)
    }


//    fun getAllTables(): List<Table> {
//        val db = this.readableDatabase
//        val selectQuery = "SELECT  * FROM $TABLE"
//        val tableList = mutableListOf<Table>()
//        val cursor: Cursor = db.rawQuery(selectQuery, null)
//        if (cursor.moveToFirst()) {
//            do {
//                val table = Table(cursor.getString(0).toInt(), cursor.getString(1))
//                tableList.add(table)
//            } while (cursor.moveToNext())
//        }
//        db.close()
//        return tableList
//
//    }

    fun insertData(tableName: String, data: List<Pair<String, String>>) {
        val db = this.writableDatabase
        val values = ContentValues()
        for (i in data.indices) {
            values.put(data[i].first, data[i].second)
        }
        db.insert(tableName, null, values)
        db.close()
    }

    fun generateTable(tableName: String) {
        val db = this.writableDatabase
        var tName = ""
        tName = if (tableName.contains(" ")) {
            tableName.replace(" ", "_")
        } else {
            tableName
        }
        val createTable =
            ("CREATE TABLE IF NOT EXISTS ${tName.toLowerCase(Locale.ENGLISH)} (id INTEGER PRIMARY KEY AUTOINCREMENT,code_data TEXT)")
        db.execSQL(createTable)
    }

    fun addNewColumn(tableName: String, column: Pair<String, String>,defaultValue:String) {
        val db = this.writableDatabase
        var cName = ""
        cName = if (column.first.contains(" ")) {
            column.first.replace(" ", "_")
        } else {
            column.first
        }
        if (tableExists(tableName)) {
            if (defaultValue.isNotEmpty()){
                val query = "ALTER TABLE ${tableName.toLowerCase(Locale.ENGLISH)} ADD COLUMN $cName ${column.second} DEFAULT $defaultValue"
                db.execSQL(query)
            }
            else
            {
                val query = "ALTER TABLE ${tableName.toLowerCase(Locale.ENGLISH)} ADD COLUMN $cName ${column.second}"
                db.execSQL(query)
            }
        }
    }

    fun getTableColumns(tableName: String): Array<String>? {
        val db = this.readableDatabase
        if (tableExists(tableName)) {
            val c: Cursor = db.rawQuery("SELECT * FROM $tableName WHERE 0", null)
            c.use { cursor ->
                return cursor.columnNames
            }
        } else {
            return null
        }
    }

    fun getAllDatabaseTables(): List<String> {
        val db = this.readableDatabase
        val list = mutableListOf<String>()
        val c: Cursor = db.rawQuery(
            "SELECT name FROM sqlite_master WHERE type='table' AND name NOT IN('sqlite_sequence','android_metadata','codes_history','dynamic_qr_codes')",
            null
        )

        if (c.moveToFirst()) {
            while (!c.isAfterLast) {
                list.add(c.getString(c.getColumnIndex("name")))
                c.moveToNext()
            }
        }
        return list
    }

    fun tableExists(tableName: String?): Boolean {
        val db = this.readableDatabase
        if (tableName == null || db == null || !db.isOpen) {
            return false
        }
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM sqlite_master WHERE type = ? AND name = ?",
            arrayOf("table", tableName)
        )
        if (!cursor.moveToFirst()) {
            cursor.close()
            return false
        }
        val count = cursor.getInt(0)
        cursor.close()
        return count > 0
    }

}