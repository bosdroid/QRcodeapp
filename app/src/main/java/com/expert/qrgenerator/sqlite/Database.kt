package com.expert.qrgenerator.sqlite

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.expert.qrgenerator.model.ListItem
import com.expert.qrgenerator.model.TableObject
import java.util.*


class Database(context: Context) : SQLiteOpenHelper(context, databaseName, null, databaseVersion) {

    companion object {
        private const val databaseVersion = 2
        private const val databaseName = "magic_qr_database"
        private const val COLUMN_ID = "id"
        private const val COLUMN_CODE_DATA = "code_data"
        private const val COLUMN_DATE = "date"
        private const val COLUMN_IMAGE = "image"
        private const val COLUMN_QUANTITY = "quantity"
        private const val DEFAULT_TABLE_NAME = "default_table"

        private const val LIST_FIELDS_TABLE_NAME = "list_fields"
        private const val LIST_COLUMN_ID = "id"
        private const val LIST_COLUMN_FIELD_NAME = "field_name"
        private const val LIST_COLUMN_TABLE_NAME = "table_name"
        private const val LIST_COLUMN_OPTIONS = "options"
        private const val LIST_COLUMN_TYPE = "type"

        private const val L_TABLE_NAME = "list"
        private const val L_COLUMN_ID = "id"
        private const val L_COLUMN_LIST_NAME = "list_name"

        private const val LIST_META_DATA_TABLE_NAME = "list_metadata"
        private const val LIST_META_DATA_COLUMN_ID = "id"
        private const val LIST_META_DATA_COLUMN_LIST_ID = "list_id"
        private const val LIST_META_DATA_COLUMN_VALUE = "value"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val defaultTable = ("CREATE TABLE IF NOT EXISTS " + DEFAULT_TABLE_NAME + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_CODE_DATA + " TEXT," + COLUMN_DATE + " TEXT," + COLUMN_IMAGE + " TEXT," + COLUMN_QUANTITY + " INTEGER DEFAULT 1)")

        val listFieldTable =
            ("CREATE TABLE IF NOT EXISTS $LIST_FIELDS_TABLE_NAME($LIST_COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,$LIST_COLUMN_FIELD_NAME TEXT,$LIST_COLUMN_TABLE_NAME TEXT,$LIST_COLUMN_OPTIONS TEXT, $LIST_COLUMN_TYPE TEXT)")

        val listTable =
            ("CREATE TABLE IF NOT EXISTS $L_TABLE_NAME($L_COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,$L_COLUMN_LIST_NAME TEXT)")

        val listMetaDataTable =
            ("CREATE TABLE IF NOT EXISTS $LIST_META_DATA_TABLE_NAME($LIST_META_DATA_COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,$LIST_META_DATA_COLUMN_LIST_ID INTEGER,$LIST_META_DATA_COLUMN_VALUE TEXT)")

        db!!.execSQL(listFieldTable)
        db.execSQL(listTable)
        db.execSQL(listMetaDataTable)
        db.execSQL(defaultTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $DEFAULT_TABLE_NAME")
        onCreate(db)
    }


    fun getTableDate(tableName: String, column: String, order: String): List<TableObject> {
        val db = this.readableDatabase
        val columns = getTableColumns(tableName)
        val tableObjectList = mutableListOf<TableObject>()
        if (column.isEmpty() && order.isEmpty()) {
            val selectQuery = "SELECT  * FROM $tableName"

            val list = mutableListOf<Pair<String, String>>()
            var tableObject: TableObject? = null
            val cursor: Cursor = db.rawQuery(selectQuery, null)
            if (cursor.moveToFirst()) {
                do {
                    tableObject = TableObject(
                        cursor.getString(0).toInt(),
                        cursor.getString(1),
                        cursor.getString(2),
                        if (cursor.isNull(3)) "" else cursor.getString(3)
                    )
                    tableObject.quantity = cursor.getInt(4)
                    if (columns!!.size >= 6) {
                        for (i in 5 until columns.size) {
                            val col = columns[i]
                            var pair: Pair<String, String>? = null
                            pair = Pair(col, cursor.getString(i))

                            list.add(pair)
                        }
                        tableObject.dynamicColumns.addAll(list)
                        list.clear()
                    }
                    tableObjectList.add(tableObject)
                } while (cursor.moveToNext())
            }
        } else {
            val selectQuery =
                "SELECT  * FROM $tableName ORDER BY $column ${order.toUpperCase(Locale.ENGLISH)}"
            val list = mutableListOf<Pair<String, String>>()
            var tableObject: TableObject? = null
            val cursor: Cursor = db.rawQuery(selectQuery, null)
            if (cursor.moveToFirst()) {
                do {
                    tableObject = TableObject(
                        cursor.getString(0).toInt(),
                        cursor.getString(1),
                        cursor.getString(2),
                        if (cursor.isNull(3)) "" else cursor.getString(3)
                    )
                    tableObject.quantity = cursor.getInt(4)
                    if (columns!!.size >= 6) {
                        for (i in 5 until columns.size) {
                            val col = columns[i]
                            var pair: Pair<String, String>? = null
                            pair = Pair(col, cursor.getString(i))

                            list.add(pair)
                        }
                        tableObject.dynamicColumns.addAll(list)
                        list.clear()
                    }
                    tableObjectList.add(tableObject)
                } while (cursor.moveToNext())
            }
        }

        db.close()
        return tableObjectList

    }

    fun insertDefaultTable(code_data: String, date: String) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put("code_data", code_data)
        values.put("date", date)
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

    fun updateData(tableName: String, data: List<Pair<String, String>>, id: Int): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        for (i in data.indices) {
            if (data[i].second.isEmpty()) {
                continue
            } else {
                values.put(data[i].first, data[i].second)
            }
        }
        return db.update(tableName, values, "id=$id", null) > 0

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
            ("CREATE TABLE IF NOT EXISTS ${tName.toLowerCase(Locale.ENGLISH)} (id INTEGER PRIMARY KEY AUTOINCREMENT,code_data TEXT,date TEXT,image TEXT,quantity INTEGER DEFAULT 1)")
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
            "SELECT name FROM sqlite_master WHERE type='table' AND name NOT IN('sqlite_sequence','android_metadata','codes_history','dynamic_qr_codes','list_fields','list','list_metadata')",
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

    fun insertFieldList(fieldName: String, tableName: String, options: String, type: String) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(LIST_COLUMN_FIELD_NAME, fieldName)
        values.put(LIST_COLUMN_TABLE_NAME, tableName)
        values.put(LIST_COLUMN_OPTIONS, options)
        values.put(LIST_COLUMN_TYPE, type)
        db.insert(LIST_FIELDS_TABLE_NAME, null, values)
        db.close()
    }

    fun getFieldList(fieldName: String, tableName: String): Pair<String, String>? {
        val db = this.readableDatabase
        var options: Pair<String, String>? = null

        val selectQuery = "SELECT  * FROM $LIST_FIELDS_TABLE_NAME WHERE $LIST_COLUMN_FIELD_NAME='${
            fieldName.toLowerCase(
                Locale.ENGLISH
            )
        }' AND $LIST_COLUMN_TABLE_NAME='${tableName.toLowerCase(Locale.ENGLISH)}'"

        val cursor: Cursor? = db.rawQuery(selectQuery, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    options = Pair(cursor.getString(3), cursor.getString(4))

                } while (cursor.moveToNext())
            }
            db.close()

            return options
        } else {
            return null
        }

    }

    fun insertList(listName: String): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(L_COLUMN_LIST_NAME, listName)
        return db.insert(L_TABLE_NAME, null, values)
        //db.close()
    }

    fun insertListValue(listId: Int, value: String) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(LIST_META_DATA_COLUMN_LIST_ID, listId)
        values.put(LIST_META_DATA_COLUMN_VALUE, value)
        db.insert(LIST_META_DATA_TABLE_NAME, null, values)
        db.close()
    }

    fun getList(): List<ListItem> {
        val db = this.readableDatabase
        val list = mutableListOf<ListItem>()
        val selectQuery = "SELECT  * FROM $L_TABLE_NAME"
        val cursor: Cursor = db.rawQuery(selectQuery, null)
        if (cursor.moveToFirst()) {
            do {
                list.add(ListItem(cursor.getInt(0), cursor.getString(1)))
            } while (cursor.moveToNext())
        }
        db.close()
        return list
    }

    fun getListValues(listId: Int): String {
        val db = this.readableDatabase
        val list = mutableListOf<String>()
        var listOptions = ""
        val selectQuery =
            "SELECT  * FROM $LIST_META_DATA_TABLE_NAME WHERE $LIST_META_DATA_COLUMN_LIST_ID=$listId"
        val cursor: Cursor = db.rawQuery(selectQuery, null)
        if (cursor.moveToFirst()) {
            do {
                list.add(cursor.getString(2))
//                listOptions += cursor.getString(2) + ","
            } while (cursor.moveToNext())
        }
        db.close()
        listOptions = list.joinToString()
        Log.d("TEST199", listOptions)
        return listOptions
    }

    fun getFieldListValues(listId: Int): List<String> {
        val db = this.readableDatabase
        val list = mutableListOf<String>()
        val selectQuery =
            "SELECT  * FROM $LIST_META_DATA_TABLE_NAME WHERE $LIST_META_DATA_COLUMN_LIST_ID=$listId"
        val cursor: Cursor = db.rawQuery(selectQuery, null)
        if (cursor.moveToFirst()) {
            do {
                list.add(cursor.getString(2))
            } while (cursor.moveToNext())
        }
        db.close()

        return list
    }

    fun updateBarcodeDetail(tableName: String, column: String, value: String, id: Int): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(column, value)
        return db.update(tableName, contentValues, "id=$id", null) > 0
    }

    fun getUpdateBarcodeDetail(tableName: String, id: Int): TableObject? {
        val db = this.readableDatabase
        val columns = getTableColumns(tableName)
        val selectQuery = "SELECT  * FROM $tableName WHERE id=$id"
        val list = mutableListOf<Pair<String, String>>()
        var tableObject: TableObject? = null
        val cursor: Cursor = db.rawQuery(selectQuery, null)
        if (cursor.moveToFirst()) {
            do {
                tableObject = TableObject(
                    cursor.getString(0).toInt(),
                    cursor.getString(1),
                    cursor.getString(2),
                    if (cursor.isNull(3)) "" else cursor.getString(3)
                )
                tableObject.quantity = cursor.getInt(4)
                if (columns!!.size >= 6) {
                    for (i in 5 until columns.size) {
                        val col = columns[i]
                        var pair: Pair<String, String>? = null
                        pair = Pair(col, cursor.getString(i))

                        list.add(pair)
                    }
                    tableObject.dynamicColumns.addAll(list)
                }
            } while (cursor.moveToNext())
        }
        return tableObject
    }

    fun removeItem(tableName: String,id: Int):Boolean{
        val db = this.writableDatabase
        return db.delete(tableName, "id=$id", null) > 0
    }

    fun deleteItem(tableName: String,code_data: String):Boolean{
        val db = this.writableDatabase
        return db.delete(tableName, "code_data=$code_data", null) > 0
    }

    fun searchItem(tableName: String,code_data: String):Boolean{
        val db = this.readableDatabase
        val columns = getTableColumns(tableName)
        val list = mutableListOf<Pair<String, String>>()
        var tableObject: TableObject? = null
        val cursor = db.rawQuery("SELECT * FROM $tableName WHERE code_data=$code_data", null)
        if (cursor.moveToFirst()) {
            do {
                tableObject = TableObject(
                    cursor.getString(0).toInt(),
                    cursor.getString(1),
                    cursor.getString(2),
                    if (cursor.isNull(3)) "" else cursor.getString(3)
                )
                tableObject.quantity = cursor.getInt(4)
                if (columns!!.size >= 6) {
                    for (i in 5 until columns.size) {
                        val col = columns[i]
                        var pair: Pair<String, String>? = null
                        pair = Pair(col, cursor.getString(i))

                        list.add(pair)
                    }
                    tableObject.dynamicColumns.addAll(list)
                }
            } while (cursor.moveToNext())
        }
        return tableObject != null
    }

    fun getScanItem(tableName: String,code_data: String):TableObject?{
        val db = this.readableDatabase
        val columns = getTableColumns(tableName)
        val list = mutableListOf<Pair<String, String>>()
        var tableObject: TableObject? = null
        val cursor = db.rawQuery("SELECT * FROM $tableName WHERE code_data=$code_data", null)
        if (cursor.moveToFirst()) {
            do {
                tableObject = TableObject(
                    cursor.getString(0).toInt(),
                    cursor.getString(1),
                    cursor.getString(2),
                    if (cursor.isNull(3)) "" else cursor.getString(3)
                )
                tableObject.quantity = cursor.getInt(4)
                if (columns!!.size >= 6) {
                    for (i in 5 until columns.size) {
                        val col = columns[i]
                        var pair: Pair<String, String>? = null
                        pair = Pair(col, cursor.getString(i))

                        list.add(pair)
                    }
                    tableObject.dynamicColumns.addAll(list)
                }
            } while (cursor.moveToNext())
        }
        return tableObject
    }

    fun updateScanQuantity(tableName: String,code_data: String,quantity:Int):Boolean{
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COLUMN_QUANTITY, quantity)
        return db.update(tableName, contentValues, "code_data=$code_data", null) > 0
    }

    fun getScanQuantity(tableName: String,code_data: String):String{
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $tableName WHERE code_data=$code_data", null)
        if (cursor != null){
            cursor.moveToFirst()
            return cursor.getString(cursor.getColumnIndex("quantity"))
        }
        else {
            return "-1"
        }
    }

}