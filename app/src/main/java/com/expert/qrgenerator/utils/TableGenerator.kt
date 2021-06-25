package com.expert.qrgenerator.utils

import android.content.Context
import android.database.Cursor
import android.util.Log
import com.expert.qrgenerator.model.ListItem
import com.expert.qrgenerator.model.Table
import com.expert.qrgenerator.model.TableObject
import com.expert.qrgenerator.sqlite.Database

class TableGenerator(context: Context) {

    private var database: Database = Database(context)

    fun insertData(tableName: String, data: List<Pair<String, String>>) {
        database.insertData(tableName,data)
    }

    fun generateTable(tableName: String){
        database.generateTable(tableName)
    }

    fun addNewColumn(tableName: String, column: Pair<String, String>,defaultVale:String){
        database.addNewColumn(tableName,column,defaultVale)
    }

    fun getTableColumns(tableName: String): Array<String>? {
        return database.getTableColumns(tableName)
    }

    fun getAllDatabaseTables():List<String>{
        return database.getAllDatabaseTables()
    }

    fun tableExists(tableName: String):Boolean{
        return database.tableExists(tableName)
    }

    fun getTableDate(tableName: String):List<TableObject>{
        return database.getTableDate(tableName)
    }

    fun insertDefaultTable(code_data:String,date:String){
        database.insertDefaultTable(code_data,date)
    }

    fun insertFieldList(fieldName:String,tableName: String,options:String){
        database.insertFieldList(fieldName,tableName,options)
    }

    fun getFieldList(fieldName: String,tableName: String):String{
        return database.getFieldList(fieldName,tableName)
    }

    fun insertList(listName:String):Long{
        return database.insertList(listName)
    }

    fun insertListValue(listId:Int,value:String){
        database.insertListValue(listId,value)
    }

    fun getList():List<ListItem>{
        return database.getList()
    }

    fun getListValues(listId:Int):String{
        return database.getListValues(listId)
    }

    fun getFieldListValues(listId:Int):List<String>{
        return database.getFieldListValues(listId)
    }
}