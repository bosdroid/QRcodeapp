package com.expert.qrgenerator.utils

import android.content.Context
import android.database.Cursor
import android.util.Log
import com.expert.qrgenerator.model.ListItem
import com.expert.qrgenerator.model.Table
import com.expert.qrgenerator.model.TableObject
import com.expert.qrgenerator.sqlite.Database

/**
 * Class responsible for managing database operations such as table generation, data insertion, updates, and retrieval.
 */
class TableGenerator(private val context: Context) {

    private val database: Database = Database(context)

    /**
     * Inserts data into a specified table.
     * @param tableName Name of the table to insert data into.
     * @param data List of key-value pairs representing column names and their corresponding values.
     */
    fun insertData(tableName: String, data: List<Pair<String, String>>) {
        database.insertData(tableName, data)
    }

    /**
     * Updates data in a specified table for a given row ID.
     * @param tableName Name of the table to update.
     * @param data List of key-value pairs representing column names and their new values.
     * @param id ID of the row to update.
     * @return Boolean indicating if the update was successful.
     */
    fun updateData(tableName: String, data: List<Pair<String, String>>, id: Int): Boolean {
        return database.updateData(tableName, data, id)
    }

    /**
     * Generates a new table with the specified name.
     * @param tableName Name of the table to create.
     */
    fun generateTable(tableName: String) {
        database.generateTable(tableName)
    }

    /**
     * Adds a new column to a specified table with a default value.
     * @param tableName Name of the table to modify.
     * @param column A pair representing the column name and its data type.
     * @param defaultValue Default value for the new column.
     */
    fun addNewColumn(tableName: String, column: Pair<String, String>, defaultValue: String) {
        database.addNewColumn(tableName, column, defaultValue)
    }

    /**
     * Retrieves the column names of a specified table.
     * @param tableName Name of the table to query.
     * @return Array of column names or null if the table does not exist.
     */
    fun getTableColumns(tableName: String): Array<String>? {
        return database.getTableColumns(tableName)
    }

    /**
     * Retrieves a list of all table names in the database.
     * @return List of table names.
     */
    fun getAllDatabaseTables(): List<String> {
        return database.getAllDatabaseTables()
    }

    /**
     * Checks if a table exists in the database.
     * @param tableName Name of the table to check.
     * @return Boolean indicating if the table exists.
     */
    fun tableExists(tableName: String): Boolean {
        return database.tableExists(tableName)
    }

    /**
     * Retrieves data from a specified table, sorted by a given column and order.
     * @param tableName Name of the table to query.
     * @param column Column to sort by.
     * @param order Sorting order (e.g., "ASC" or "DESC").
     * @return List of TableObject instances containing the retrieved data.
     */
    fun getTableData(tableName: String, column: String, order: String): List<TableObject> {
        return database.getTableData(tableName, column, order)
    }

    /**
     * Inserts default data into a default table.
     * @param codeData Data to insert.
     * @param date Date of insertion.
     */
    fun insertDefaultTable(codeData: String, date: String) {
        database.insertDefaultTable(codeData, date)
    }

    /**
     * Inserts a new field list into a specified table.
     * @param fieldName Name of the field.
     * @param tableName Name of the table.
     * @param options Options for the field.
     * @param type Data type of the field.
     */
    fun insertFieldList(fieldName: String, tableName: String, options: String, type: String) {
        database.insertFieldList(fieldName, tableName, options, type)
    }

    /**
     * Retrieves field data from a specified table.
     * @param fieldName Name of the field to retrieve.
     * @param tableName Name of the table.
     * @return Pair containing field options and type, or null if the field does not exist.
     */
    fun getFieldList(fieldName: String, tableName: String): Pair<String, String>? {
        return database.getFieldList(fieldName, tableName)
    }

    /**
     * Inserts a new list into the database.
     * @param listName Name of the list.
     * @return ID of the newly inserted list.
     */
    fun insertList(listName: String): Long {
        return database.insertList(listName)
    }

    /**
     * Inserts a new value into a specified list.
     * @param listId ID of the list to insert the value into.
     * @param value Value to insert.
     */
    fun insertListValue(listId: Int, value: String) {
        database.insertListValue(listId, value)
    }

    /**
     * Retrieves all lists from the database.
     * @return List of ListItem instances representing the lists.
     */
    fun getList(): List<ListItem> {
        return database.getList()
    }

    /**
     * Retrieves values from a specified list.
     * @param listId ID of the list to retrieve values from.
     * @return Comma-separated string of values.
     */
    fun getListValues(listId: Int): String {
        return database.getListValues(listId)
    }

    /**
     * Retrieves field values from a specified list.
     * @param listId ID of the list to retrieve field values from.
     * @return List of field values.
     */
    fun getFieldListValues(listId: Int): List<String> {
        return database.getFieldListValues(listId)
    }

    /**
     * Updates barcode details in a specified table.
     * @param tableName Name of the table to update.
     * @param column Column to update.
     * @param value New value for the column.
     * @param id ID of the row to update.
     * @return Boolean indicating if the update was successful.
     */
    fun updateBarcodeDetail(tableName: String, column: String, value: String, id: Int): Boolean {
        return database.updateBarcodeDetail(tableName, column, value, id)
    }

    /**
     * Retrieves barcode details for a specified table and row ID.
     * @param tableName Name of the table to query.
     * @param id ID of the row to retrieve.
     * @return TableObject instance containing the barcode details, or null if not found.
     */
    fun getUpdateBarcodeDetail(tableName: String, id: Int): TableObject? {
        return database.getUpdateBarcodeDetail(tableName, id)
    }

    /**
     * Removes an item from a specified table.
     * @param tableName Name of the table to remove the item from.
     * @param id ID of the item to remove.
     * @return Boolean indicating if the removal was successful.
     */
    fun removeItem(tableName: String, id: Int): Boolean {
        return database.removeItem(tableName, id)
    }

    /**
     * Deletes an item from a specified table by its code data.
     * @param tableName Name of the table to delete the item from.
     * @param codeData Code data of the item to delete.
     * @return Boolean indicating if the deletion was successful.
     */
    fun deleteItem(tableName: String, codeData: String): Boolean {
        return database.deleteItem(tableName, codeData)
    }

    /**
     * Searches for an item in a specified table by its code data.
     * @param tableName Name of the table to search.
     * @param codeData Code data of the item to search for.
     * @return Boolean indicating if the item was found.
     */
    fun searchItem(tableName: String, codeData: String): Boolean {
        return database.searchItem(tableName, codeData)
    }

    /**
     * Retrieves a scanned item from a specified table by its code data.
     * @param tableName Name of the table to query.
     * @param codeData Code data of the scanned item.
     * @return TableObject instance containing the scanned item, or null if not found.
     */
    fun getScanItem(tableName: String, codeData: String): TableObject? {
        return database.getScanItem(tableName, codeData)
    }

    /**
     * Updates the quantity of a scanned item in a specified table.
     * @param tableName Name of the table to update.
     * @param codeData Code data of the scanned item.
     * @param quantity New quantity to set.
     * @return Boolean indicating if the update was successful.
     */
    fun updateScanQuantity(tableName: String, codeData: String, quantity: Int): Boolean {
        return database.updateScanQuantity(tableName, codeData, quantity)
    }

    /**
     * Retrieves the quantity of a scanned item from a specified table.
     * @param tableName Name of the table to query.
     * @param codeData Code data of the scanned item.
     * @return Quantity of the scanned item.
     */
    fun getScanQuantity(tableName: String, codeData: String): String {
        return database.getScanQuantity(tableName, codeData)
    }
}
