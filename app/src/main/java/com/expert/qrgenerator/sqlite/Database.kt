package com.expert.qrgenerator.sqlite

import android.annotation.SuppressLint
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
        // Database version and name
        private const val databaseVersion = 2
        private const val databaseName = "magic_qr_database"

        // Default table and column names
        private const val COLUMN_ID = "id"
        private const val COLUMN_CODE_DATA = "code_data"
        private const val COLUMN_DATE = "date"
        private const val COLUMN_IMAGE = "image"
        private const val COLUMN_QUANTITY = "quantity"
        private const val DEFAULT_TABLE_NAME = "default_table"

        // Table and column names for list fields
        private const val LIST_FIELDS_TABLE_NAME = "list_fields"
        private const val LIST_COLUMN_ID = "id"
        private const val LIST_COLUMN_FIELD_NAME = "field_name"
        private const val LIST_COLUMN_TABLE_NAME = "table_name"
        private const val LIST_COLUMN_OPTIONS = "options"
        private const val LIST_COLUMN_TYPE = "type"

        // Table and column names for lists
        private const val L_TABLE_NAME = "list"
        private const val L_COLUMN_ID = "id"
        private const val L_COLUMN_LIST_NAME = "list_name"

        // Table and column names for list metadata
        private const val LIST_META_DATA_TABLE_NAME = "list_metadata"
        private const val LIST_META_DATA_COLUMN_ID = "id"
        private const val LIST_META_DATA_COLUMN_LIST_ID = "list_id"
        private const val LIST_META_DATA_COLUMN_VALUE = "value"
    }

    // Create tables if they don't already exist
    override fun onCreate(db: SQLiteDatabase?) {
        db ?: return

        // SQL statement to create the default table
        val defaultTable = """
            CREATE TABLE IF NOT EXISTS $DEFAULT_TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_CODE_DATA TEXT,
                $COLUMN_DATE TEXT,
                $COLUMN_IMAGE TEXT,
                $COLUMN_QUANTITY INTEGER DEFAULT 1
            )
        """

        // SQL statement to create the list fields table
        val listFieldTable = """
            CREATE TABLE IF NOT EXISTS $LIST_FIELDS_TABLE_NAME (
                $LIST_COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $LIST_COLUMN_FIELD_NAME TEXT,
                $LIST_COLUMN_TABLE_NAME TEXT,
                $LIST_COLUMN_OPTIONS TEXT,
                $LIST_COLUMN_TYPE TEXT
            )
        """

        // SQL statement to create the list table
        val listTable = """
            CREATE TABLE IF NOT EXISTS $L_TABLE_NAME (
                $L_COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $L_COLUMN_LIST_NAME TEXT
            )
        """

        // SQL statement to create the list metadata table
        val listMetaDataTable = """
            CREATE TABLE IF NOT EXISTS $LIST_META_DATA_TABLE_NAME (
                $LIST_META_DATA_COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $LIST_META_DATA_COLUMN_LIST_ID INTEGER,
                $LIST_META_DATA_COLUMN_VALUE TEXT
            )
        """

        // Execute SQL statements to create tables
        db.execSQL(defaultTable)
        db.execSQL(listFieldTable)
        db.execSQL(listTable)
        db.execSQL(listMetaDataTable)
    }

    // Upgrade database schema if version changes
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db ?: return
        // Drop existing tables
        db.execSQL("DROP TABLE IF EXISTS $DEFAULT_TABLE_NAME")
        // Recreate tables
        onCreate(db)
    }

    // Retrieve data from a specific table with optional sorting
    fun getTableData(tableName: String, column: String = "", order: String = ""): List<TableObject> {
        val db = this.readableDatabase
        val columns = getTableColumns(tableName) ?: return emptyList()
        val tableObjectList = mutableListOf<TableObject>()

        // Prepare ORDER BY clause if sorting is specified
        val orderBy = if (column.isNotEmpty() && order.isNotEmpty()) {
            " ORDER BY $column ${order.toUpperCase(Locale.ENGLISH)}"
        } else {
            ""
        }

        val selectQuery = "SELECT * FROM $tableName$orderBy"
        val cursor: Cursor = db.rawQuery(selectQuery, null)

        cursor.use {
            if (it.moveToFirst()) {
                do {
                    val tableObject = TableObject(
                        it.getInt(0),
                        it.getString(1),
                        it.getString(2),
                        it.getString(3) ?: ""
                    ).apply {
                        quantity = it.getInt(4)
                        // Add dynamic columns if present
                        if (columns.size >= 6) {
                            dynamicColumns.addAll(
                                (5 until columns.size).map { i -> Pair(columns[i], it.getString(i)) }
                            )
                        }
                    }
                    tableObjectList.add(tableObject)
                } while (it.moveToNext())
            }
        }

        db.close()
        return tableObjectList
    }

    // Insert a record into the default table
    fun insertDefaultTable(codeData: String, date: String) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_CODE_DATA, codeData)
            put(COLUMN_DATE, date)
        }
        db.insert(DEFAULT_TABLE_NAME, null, values)
        db.close()
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


    // Insert data into a specified table
    fun insertData(tableName: String, data: List<Pair<String, String>>) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            data.filter { it.second.isNotEmpty() }.forEach { put(it.first, it.second) }
        }
        db.insert(tableName, null, values)
        db.close()
    }

    // Update existing data in a specified table
    fun updateData(tableName: String, data: List<Pair<String, String>>, id: Int): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            data.filter { it.second.isNotEmpty() }.forEach { put(it.first, it.second) }
        }
        return db.update(tableName, values, "$COLUMN_ID=?", arrayOf(id.toString())) > 0
    }

    // Create a new table with the specified name
    fun generateTable(tableName: String) {
        val db = this.writableDatabase
        val sanitizedTableName = tableName.replace(" ", "_").toLowerCase(Locale.ENGLISH)
        val createTable = """
            CREATE TABLE IF NOT EXISTS $sanitizedTableName (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_CODE_DATA TEXT,
                $COLUMN_DATE TEXT,
                $COLUMN_IMAGE TEXT,
                $COLUMN_QUANTITY INTEGER DEFAULT 1
            )
        """
        db.execSQL(createTable)
    }

    // Add a new column to a specified table
    fun addNewColumn(tableName: String, column: Pair<String, String>, defaultValue: String = "") {
        val db = this.writableDatabase
        val sanitizedColumnName = column.first.replace(" ", "_")
        if (tableExists(tableName)) {
            val query = if (defaultValue.isNotEmpty()) {
                "ALTER TABLE ${tableName.toLowerCase(Locale.ENGLISH)} ADD COLUMN $sanitizedColumnName ${column.second} DEFAULT $defaultValue"
            } else {
                "ALTER TABLE ${tableName.toLowerCase(Locale.ENGLISH)} ADD COLUMN $sanitizedColumnName ${column.second}"
            }
            db.execSQL(query)
        }
    }

    // Retrieve the columns of a specified table
    fun getTableColumns(tableName: String): Array<String>? {
        val db = this.readableDatabase
        if (tableExists(tableName)) {
            val cursor: Cursor = db.rawQuery("SELECT * FROM $tableName WHERE 0", null)
            return cursor.columnNames
        }
        return null
    }

    // Get a list of all database tables excluding system tables and predefined ones
    @SuppressLint("Range")
    fun getAllDatabaseTables(): List<String> {
        val db = this.readableDatabase
        val tables = mutableListOf<String>()
        val cursor: Cursor = db.rawQuery(
            "SELECT name FROM sqlite_master WHERE type='table' AND name NOT IN('sqlite_sequence','android_metadata','codes_history','dynamic_qr_codes','list_fields','list','list_metadata')",
            null
        )

        cursor.use {
            if (it.moveToFirst()) {
                do {
                    tables.add(it.getString(it.getColumnIndex("name")))
                } while (it.moveToNext())
            }
        }

        return tables
    }

    // Check if a table exists in the database
    fun tableExists(tableName: String?): Boolean {
        val db = this.readableDatabase
        if (tableName == null || !db.isOpen) return false

        val cursor: Cursor = db.rawQuery(
            "SELECT COUNT(*) FROM sqlite_master WHERE type = ? AND name = ?",
            arrayOf("table", tableName)
        )
        cursor.use {
            return if (it.moveToFirst()) it.getInt(0) > 0 else false
        }
    }

    // Insert a field into the list fields table
    fun insertFieldList(fieldName: String, tableName: String, options: String, type: String) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(LIST_COLUMN_FIELD_NAME, fieldName)
            put(LIST_COLUMN_TABLE_NAME, tableName)
            put(LIST_COLUMN_OPTIONS, options)
            put(LIST_COLUMN_TYPE, type)
        }
        db.insert(LIST_FIELDS_TABLE_NAME, null, values)
        db.close()
    }

    /**
     * Retrieves a pair of field values from the database based on the given field name and table name.
     *
     * @param fieldName The name of the field to search for.
     * @param tableName The name of the table to search in.
     * @return A Pair of strings representing the field values, or null if no matching record is found.
     */
    fun getFieldList(fieldName: String, tableName: String): Pair<String, String>? {
        // Get a readable database instance
        val db = this.readableDatabase

        // Define the query to select the relevant fields from the database
        val selectQuery = """
        SELECT * 
        FROM $LIST_FIELDS_TABLE_NAME 
        WHERE ${LIST_COLUMN_FIELD_NAME} = ? 
          AND ${LIST_COLUMN_TABLE_NAME} = ?
    """.trimIndent()

        // Execute the query with parameters to avoid SQL injection
        val cursor: Cursor? = db.rawQuery(selectQuery, arrayOf(
            fieldName.toLowerCase(Locale.ENGLISH),
            tableName.toLowerCase(Locale.ENGLISH)
        ))

        // Initialize options to store the result
        var options: Pair<String, String>? = null

        // Process the result set
        cursor?.use {
            if (it.moveToFirst()) {
                // Retrieve the values from the cursor and create a Pair
                options = Pair(it.getString(3), it.getString(4))
            }
        }

        // Close the database connection
        db.close()

        // Return the result or null if no data is found
        return options
    }


    // Insert data into the list metadata table
    fun insertListData(listId: Int, value: String) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(LIST_META_DATA_COLUMN_LIST_ID, listId)
            put(LIST_META_DATA_COLUMN_VALUE, value)
        }
        db.insert(LIST_META_DATA_TABLE_NAME, null, values)
        db.close()
    }

    // Retrieve all values for a specific list ID from the list metadata table
    @SuppressLint("Range")
    fun getListValue(listId: Int): List<String> {
        val db = this.readableDatabase
        val values = mutableListOf<String>()
        val selectQuery = "SELECT * FROM $LIST_META_DATA_TABLE_NAME WHERE $LIST_META_DATA_COLUMN_LIST_ID = ?"
        val cursor: Cursor = db.rawQuery(selectQuery, arrayOf(listId.toString()))

        cursor.use {
            if (it.moveToFirst()) {
                do {
                    values.add(it.getString(it.getColumnIndex(LIST_META_DATA_COLUMN_VALUE)))
                } while (it.moveToNext())
            }
        }

        return values
    }

    // Retrieve all list names from the list table
//    @SuppressLint("Range")
//    fun getList(): List<String> {
//        val db = this.readableDatabase
//        val list = mutableListOf<String>()
//        val selectQuery = "SELECT * FROM $L_TABLE_NAME"
//        val cursor: Cursor = db.rawQuery(selectQuery, null)
//
//        cursor.use {
//            if (it.moveToFirst()) {
//                do {
//                    list.add(it.getString(it.getColumnIndex(L_COLUMN_LIST_NAME)))
//                } while (it.moveToNext())
//            }
//        }
//
//        return list
//    }

    // Function to get a list of field values for a given listId
    fun getFieldListValues(listId: Int): List<String> {
        val list = mutableListOf<String>()
        val db = this.readableDatabase
        val selectQuery = "SELECT * FROM $LIST_META_DATA_TABLE_NAME WHERE $LIST_META_DATA_COLUMN_LIST_ID=$listId"

        db.rawQuery(selectQuery, null).use { cursor ->
            while (cursor.moveToNext()) {
                list.add(cursor.getString(2)) // Assumes the required data is in the 3rd column (index 2)
            }
        }

        return list
    }

    // Function to update a specific column value in a table by id
    fun updateBarcodeDetail(tableName: String, column: String, value: String, id: Int): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(column, value)
        }
        return db.update(tableName, contentValues, "id=?", arrayOf(id.toString())) > 0
    }

    // Function to get details of a table row by id
    fun getUpdateBarcodeDetail(tableName: String, id: Int): TableObject? {
        val db = this.readableDatabase
        val columns = getTableColumns(tableName) ?: return null
        val selectQuery = "SELECT * FROM $tableName WHERE id=$id"
        val list = mutableListOf<Pair<String, String>>()
        var tableObject: TableObject? = null

        db.rawQuery(selectQuery, null).use { cursor ->
            if (cursor.moveToFirst()) {
                tableObject = TableObject(
                    cursor.getString(0).toInt(),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3) ?: ""
                ).apply {
                    quantity = cursor.getInt(4)
                    if (columns.size >= 6) {
                        for (i in 5 until columns.size) {
                            list.add(Pair(columns[i], cursor.getString(i)))
                        }
                        dynamicColumns.addAll(list)
                    }
                }
            }
        }

        return tableObject
    }

    // Function to remove an item from a table by id
    fun removeItem(tableName: String, id: Int): Boolean {
        val db = this.writableDatabase
        return db.delete(tableName, "id=?", arrayOf(id.toString())) > 0
    }

    // Function to delete an item from a table by code_data
    fun deleteItem(tableName: String, codeData: String): Boolean {
        val db = this.writableDatabase
        return db.delete(tableName, "code_data=?", arrayOf(codeData)) > 0
    }

    // Function to search for an item by code_data
    fun searchItem(tableName: String, codeData: String): Boolean {
        val db = this.readableDatabase
        val query = "SELECT 1 FROM $tableName WHERE code_data=?"

        db.rawQuery(query, arrayOf(codeData)).use { cursor ->
            return cursor.count > 0
        }
    }

    // Function to get a table object based on code_data
    fun getScanItem(tableName: String, codeData: String): TableObject? {
        val db = this.readableDatabase
        val columns = getTableColumns(tableName) ?: return null
        val list = mutableListOf<Pair<String, String>>()
        var tableObject: TableObject? = null

        val query = "SELECT * FROM $tableName WHERE code_data=?"
        db.rawQuery(query, arrayOf(codeData)).use { cursor ->
            if (cursor.moveToFirst()) {
                tableObject = TableObject(
                    cursor.getString(0).toInt(),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3) ?: ""
                ).apply {
                    quantity = cursor.getInt(4)
                    if (columns.size >= 6) {
                        for (i in 5 until columns.size) {
                            list.add(Pair(columns[i], cursor.getString(i)))
                        }
                        dynamicColumns.addAll(list)
                    }
                }
            }
        }

        return tableObject
    }

    // Function to update the quantity of an item by code_data
    fun updateScanQuantity(tableName: String, codeData: String, quantity: Int): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_QUANTITY, quantity)
        }
        return db.update(tableName, contentValues, "code_data=?", arrayOf(codeData)) > 0
    }

    // Function to get the quantity of an item by code_data
    fun getScanQuantity(tableName: String, codeData: String): String {
        val db = this.readableDatabase
        val query = "SELECT quantity FROM $tableName WHERE code_data=?"

        db.rawQuery(query, arrayOf(codeData)).use { cursor ->
            return if (cursor.moveToFirst()) {
                cursor.getString(cursor.getColumnIndexOrThrow("quantity"))
            } else {
                "-1"
            }
        }
    }

}
