package com.expert.qrgenerator.view.activities

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.TableLayout
import android.widget.TableRow
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.R
import com.expert.qrgenerator.adapters.TableDetailAdapter
import com.expert.qrgenerator.model.TableObject
import com.expert.qrgenerator.utils.TableGenerator
import com.google.android.material.textview.MaterialTextView
import java.io.File
import java.lang.Exception
import java.lang.StringBuilder
import java.util.*


class TableViewActivity : BaseActivity(), TableDetailAdapter.OnItemClickListener,
    View.OnClickListener {

    private lateinit var context: Context
    private lateinit var toolbar: Toolbar
    private lateinit var tableGenerator: TableGenerator
    private lateinit var tableMainLayout: TableLayout
    private var tableName: String = ""
    private var dataList = mutableListOf<TableObject>()
    private var sortingImages = mutableListOf<AppCompatImageView>()
    private lateinit var csvExportImageView: AppCompatImageView
    val layoutParams = TableRow.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT,
        2f
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_table_view)

        initViews()
        setUpToolbar()
        getTableData(tableName, "", "")
    }

    private fun initViews() {
        context = this
        tableGenerator = TableGenerator(context)
        toolbar = findViewById(R.id.toolbar)
        csvExportImageView = findViewById(R.id.export_csv)

        if (intent != null && intent.hasExtra("TABLE_NAME")) {
            tableName = intent.getStringExtra("TABLE_NAME")!!
        }

        tableMainLayout = findViewById(R.id.table_main)
        val columns = tableGenerator.getTableColumns(tableName)

        val tableHeaders = TableRow(context)
        for (i in columns!!.indices) {
            val headerLayout =
                LayoutInflater.from(context).inflate(R.layout.header_table_row_cell, null)
            headerLayout.layoutParams = layoutParams
            val textView = headerLayout.findViewById<MaterialTextView>(R.id.header_cell_name)
            val sortAscImageView =
                headerLayout.findViewById<AppCompatImageView>(R.id.sort_asc_image)
            sortAscImageView.id = i
            sortAscImageView.tag = "asc:${columns[i].toLowerCase(Locale.ENGLISH)}"
            sortAscImageView.setOnClickListener(this)
            val sortDescImageView =
                headerLayout.findViewById<AppCompatImageView>(R.id.sort_desc_image)
            sortDescImageView.id = i
            sortDescImageView.tag = "desc:${columns[i].toLowerCase(Locale.ENGLISH)}"
            sortDescImageView.setOnClickListener(this)
            sortingImages.add(sortAscImageView)
            sortingImages.add(sortDescImageView)
            headerLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.purple_dark))

            textView.text = columns[i].toUpperCase(Locale.ENGLISH)
            tableHeaders.addView(headerLayout)
        }

        tableMainLayout.addView(tableHeaders)

        csvExportImageView.setOnClickListener {
            exportCsv(tableName)
        }

    }

    private fun setUpToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar!!.title = tableName
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.black))
    }

    // THIS FUNCTION WILL HANDLE THE ON BACK ARROW CLICK EVENT
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            onBackPressed()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }


    private fun getTableData(tName: String, column: String, order: String) {
        val tempList = tableGenerator.getTableDate(tName, column, order)
        if (tempList.isNotEmpty()) {
            dataList.clear()
            if (tableMainLayout.childCount > 2) {
                tableMainLayout.removeViews(1, tableMainLayout.childCount - 1)
            }
        }
        dataList.addAll(tempList)
        tableMainLayout.weightSum = dataList.size * 2F

        if (dataList.isNotEmpty()) {
            startLoading(context)
            for (j in 0 until dataList.size) {
                val textViewIdLayout =
                    LayoutInflater.from(context).inflate(R.layout.table_row_cell, null)
                textViewIdLayout.layoutParams = layoutParams
                val textViewId = textViewIdLayout.findViewById<MaterialTextView>(R.id.cell_value)
                val data = dataList[j]
                val tableRow = TableRow(context)
                tableRow.id = j
                tableRow.tag = "row"
                tableRow.setOnClickListener(this)

                textViewId.text = "${data.id}"
                tableRow.addView(textViewIdLayout)
                val textViewCodeDateLayout =
                    LayoutInflater.from(context).inflate(R.layout.table_row_cell, null)
                textViewCodeDateLayout.layoutParams = layoutParams
                val textViewCodeDate =
                    textViewCodeDateLayout.findViewById<MaterialTextView>(R.id.cell_value)
                textViewCodeDate.text = data.code_data
                tableRow.addView(textViewCodeDateLayout)

                val textViewDateLayout =
                    LayoutInflater.from(context).inflate(R.layout.table_row_cell, null)
                textViewDateLayout.layoutParams = layoutParams
                val textViewDate =
                    textViewDateLayout.findViewById<MaterialTextView>(R.id.cell_value)
                textViewDate.text = data.date
                tableRow.addView(textViewDateLayout)

                val textViewImageLayout =
                    LayoutInflater.from(context).inflate(R.layout.table_row_cell, null)
                textViewImageLayout.layoutParams = layoutParams
                val textViewImage =
                    textViewImageLayout.findViewById<MaterialTextView>(R.id.cell_value)

                if (data.image.isNotEmpty() && data.image.length >= 20) {
                    textViewImage.text = data.image.substring(0, 20)
                } else {
                    textViewImage.text = data.image
                }
                tableRow.addView(textViewImageLayout)


                if (data.dynamicColumns.size > 0) {
                    for (k in 0 until data.dynamicColumns.size) {
                        val item = data.dynamicColumns[k]
                        val cell =
                            LayoutInflater.from(context).inflate(R.layout.table_row_cell, null)
                        cell.layoutParams = layoutParams
                        val textV = cell.findViewById<MaterialTextView>(R.id.cell_value)

                        textV.text = item.second
                        tableRow.addView(cell)
                    }

                }
                if (j % 2 == 0) {
                    tableRow.setBackgroundColor(Color.parseColor("#EAEAF6"))
                } else {
                    tableRow.setBackgroundColor(Color.parseColor("#f2f2f2"))
                }
                tableMainLayout.addView(tableRow)
            }
            dismiss()
        }

    }

    override fun onItemClick(position: Int) {
        val tableObject = dataList[position]
        showAlert(context, tableObject.toString())
    }

    override fun onClick(v: View?) {
        val view = v!!
        if (view.tag == "row") {
            val position = view.id
            val item = dataList[position]
            if (item.image.isNotEmpty()) {
                showAlert(context, dataList[position].image)
            }
        } else {
            if (dataList.isNotEmpty()) {
                val parts = view.tag.toString().split(":")
                if (parts.isNotEmpty() && parts.size == 2) {
                    val image = view as AppCompatImageView
                    updateSortingImage(image, parts[0])
                    getTableData(tableName, parts[1], parts[0])
                }
            }

        }

    }

    private fun updateSortingImage(imageView: AppCompatImageView, order: String) {
        for (i in 0 until sortingImages.size) {
            val sImage = sortingImages[i]
            if (imageView.id == sImage.id && sImage.tag.toString().split(":")[0] == order) {
                sImage.setColorFilter(Color.WHITE)
            } else {
                sImage.setColorFilter(Color.parseColor("#808080"))
            }
        }
    }


    private fun exportCsv(tableName: String) {
        if (dataList.isNotEmpty()) {
            startLoading(context)
            val columns = tableGenerator.getTableColumns(tableName)
            val builder = StringBuilder()
            builder.append(columns!!.joinToString(","))

            for (j in 0 until dataList.size) {

                val data = dataList[j]

                builder.append("\n${data.id},${data.code_data},${data.date},${data.image}")
                if (data.dynamicColumns.size > 0) {
                    for (k in 0 until data.dynamicColumns.size) {
                        val item = data.dynamicColumns[k]
                        if (k != data.dynamicColumns.size) {
                            builder.append(",")
                        }
                        builder.append(item.second)
                    }
                }
            }

            try {

                val out = openFileOutput("$tableName.csv", Context.MODE_PRIVATE)
                out.write((builder.toString()).toByteArray())
                out.close()

                val file = File(filesDir, "$tableName.csv")
                val path =
                    FileProvider.getUriForFile(context, "com.expert.qrgenerator.fileprovider", file)
                dismiss()
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/csv"
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                intent.putExtra(Intent.EXTRA_STREAM, path)
                startActivity(Intent.createChooser(intent, "Share with"))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        else{
            showAlert(context,"Table data not exported due to empty!")
        }
    }

}