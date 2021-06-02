package com.expert.qrgenerator.sqlite

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.expert.qrgenerator.model.TableObject
import java.util.*


class Database(context: Context) : SQLiteOpenHelper(context, databaseName, null, databaseVersion) {

    companion object {
        private const val databaseVersion = 1
        private const val databaseName = "magic_qr_database"
        private const val COLUMN_ID = "id"
        private const val COLUMN_CODE_DATA = "code_data"
        private const val COLUMN_DATE = "date"
        private const val DEFAULT_TABLE_NAME = "default_table"
    }

    override fun onCreate(db: SQLiteDatabase?) {
     val defaultTable = ("CREATE TABLE IF NOT EXISTS " + DEFAULT_TABLE_NAME + "("
             + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_CODE_DATA + " TEXT," + COLUMN_DATE + " TEXT)")
        db!!.execSQL(defaultTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $DEFAULT_TABLE_NAME")
        onCreate(db)
    }


    fun getTableDate(tableName: String): List<TableObject> {
        val db = this.readableDatabase
        val columns = getTableColumns(tableName)
        val selectQuery = "SELECT  * FROM $tableName"
        val tableObjectList = mutableListOf<TableObject>()
        val list = mutableListOf<Pair<String, String>>()
        var tableObject:TableObject?=null
        val cursor: Cursor = db.rawQuery(selectQuery, null)
        if (cursor.moveToFirst()) {
            do {
                tableObject = TableObject(
                    cursor.getString(0).toInt(),
                    cursor.getString(1),
                    cursor.getString(2)
                )

                if (columns!!.size >=7){
                    for (i in 7 until columns.size) {
                        val col = columns[i]
                        var pair: Pair<String, String>? = null
                        pair = if (i == 0) {
                            Pair(col, cursor.getString(i))
                        } else {
                            Pair(col, cursor.getString(i))
                        }
                        list.add(pair)
                    }
                    tableObject.dynamicColumns.addAll(list)
                }
                tableObjectList.add(tableObject)
            } while (cursor.moveToNext())
        }
        db.close()
        return tableObjectList

    }

    fun insertDefaultTable(code_data:String,date:String){
        val db = this.writableDatabase
        val values = ContentValues()
        values.put("code_data",code_data)
        values.put("date",date)
        db.insert(DEFAULT_TABLE_NAME, null, values)
        db.close()
    }

    fun insertData(tableName: String, data: List<Pair<String, String>>) {
        val db = this.writableDatabase
        val values = ContentValues()
        for (i in data.indices) {
            if (data[i].second.isEmpty()) {
                continue
            } else {
                values.put(data[i].first, data[i].second)
            }
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
            ("CREATE TABLE IF NOT EXISTS ${tName.toLowerCase(Locale.ENGLISH)} (id INTEGER PRIMARY KEY AUTOINCREMENT,code_data TEXT,date TEXT)")
        db.execSQL(createTable)
    }

    fun addNewColumn(tableName: String, column: Pair<String, String>, defaultValue: String) {
        val db = this.writableDatabase
        var cName = ""
        cName = if (column.first.contains(" ")) {
            column.first.replace(" ", "_")
        } else {
            column.first
        }
        if (tableExists(tableName)) {
            if (defaultValue.isNotEmpty()) {
                val query =
                    "ALTER TABLE ${tableName.toLowerCase(Locale.ENGLISH)} ADD COLUMN $cName ${column.second} DEFAULT $defaultValue"
                db.execSQL(query)
            } else {
                val query =
                    "ALTER TABLE ${tableName.toLowerCase(Locale.ENGLISH)} ADD COLUMN $cName ${column.second}"
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