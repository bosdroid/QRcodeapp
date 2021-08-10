package com.expert.qrgenerator.view.activities

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.expert.qrgenerator.R
import com.expert.qrgenerator.adapters.TableDetailAdapter
import com.expert.qrgenerator.model.TableObject
import com.expert.qrgenerator.utils.TableGenerator
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import java.io.File
import java.util.*


class TableViewActivity : BaseActivity(), TableDetailAdapter.OnItemClickListener,
    View.OnClickListener{

    private lateinit var context: Context
    private lateinit var toolbar: Toolbar
    private lateinit var tableGenerator: TableGenerator
    private lateinit var tableMainLayout: TableLayout
    private var tableName: String = ""
    private var dataList = mutableListOf<TableObject>()
    private var sortingImages = mutableListOf<AppCompatImageView>()
    private lateinit var csvExportImageView: AppCompatImageView
    private lateinit var quickEditCheckbox: MaterialCheckBox
    private var currentColumn = ""
    private var currentOrder = ""
    private var quickEditFlag = false
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

    }

    override fun onResume() {
        super.onResume()
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
        for (i in 0 until columns!!.size + 1) {
            if (i == 0) {
                val headerLayout =
                    LayoutInflater.from(context).inflate(R.layout.header_table_row_cell, null)
                headerLayout.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.purple_dark
                    )
                )
                val sortImageView =
                    headerLayout.findViewById<AppCompatImageView>(R.id.sort_image)
                sortImageView.visibility = View.INVISIBLE
                tableHeaders.addView(headerLayout)
            } else {
                val headerLayout =
                    LayoutInflater.from(context).inflate(R.layout.header_table_row_cell, null)
                headerLayout.layoutParams = layoutParams
                val textView = headerLayout.findViewById<MaterialTextView>(R.id.header_cell_name)
                val sortImageView =
                    headerLayout.findViewById<AppCompatImageView>(R.id.sort_image)
                sortImageView.visibility = View.VISIBLE
                sortImageView.id = i
                sortingImages.add(sortImageView)

                headerLayout.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.purple_dark
                    )
                )

                textView.text = columns[i - 1].toUpperCase(Locale.ENGLISH)
                headerLayout.id = i - 1
                headerLayout.tag = columns[i - 1].toLowerCase(Locale.ENGLISH)
                headerLayout.setOnClickListener(this)
                tableHeaders.addView(headerLayout)
            }
        }

        tableMainLayout.addView(tableHeaders)

        csvExportImageView.setOnClickListener {
            exportCsv(tableName)
        }

        // QUICK EDIT TABLE CHECKBOX LISTENER
        quickEditCheckbox = findViewById(R.id.quick_edit_table_view_checkbox)
        quickEditCheckbox.setOnCheckedChangeListener { buttonView, isChecked ->
            quickEditFlag = isChecked
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
        }
        if (tableMainLayout.childCount > 1) {
            tableMainLayout.removeViews(1, tableMainLayout.childCount - 1)
        }

        dataList.addAll(tempList)
        tableMainLayout.weightSum = dataList.size * 2F

        if (dataList.isNotEmpty()) {
            startLoading(context)
            for (j in 0 until dataList.size) {

                val textViewIdLayout =
                    LayoutInflater.from(context).inflate(R.layout.table_row_cell, null)
                val textViewId = textViewIdLayout.findViewById<MaterialTextView>(R.id.cell_value)
                val data = dataList[j]
                val tableRow = TableRow(context)
                tableRow.id = j
                tableRow.tag = "row"
                tableRow.setOnClickListener(this)

                val moreLayout =
                    LayoutInflater.from(context).inflate(R.layout.table_more_option_layout, null)
                moreLayout.layoutParams = TableRow.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                val moreImage = moreLayout.findViewById<AppCompatImageView>(R.id.cell_more_image)
                moreImage.id = j
                moreImage.tag = "more"
                moreImage.setOnClickListener(this)
                tableRow.addView(moreLayout)

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

                val textViewQuantityLayout =
                    LayoutInflater.from(context).inflate(R.layout.table_row_cell, null)
                textViewQuantityLayout.layoutParams = layoutParams
                val textViewQuantity =
                    textViewQuantityLayout.findViewById<MaterialTextView>(R.id.cell_value)
                 textViewQuantity.text = "${data.quantity}"

                tableRow.addView(textViewQuantityLayout)

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
            if (quickEditFlag) {
                openQuickEditDialog(item)
            } else {
                val intent = Intent(context, CodeDetailActivity::class.java)
                intent.putExtra("TABLE_NAME", tableName)
                intent.putExtra("TABLE_ITEM", item)
                startActivity(intent)
            }


        } else if (view.tag == "qe") {
            val position = view.id
            val triple = barcodeEditList[position]
            triple.first.setText("")
        } else if (view.tag == "more") {
            val position = view.id
            val itemDetail = dataList[position]
            val popup = PopupMenu(context, view)
            popup.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
                override fun onMenuItemClick(item: MenuItem?): Boolean {
                    return when (item!!.itemId) {
                        R.id.pp_remove -> {
                            removeItem(itemDetail.id,position)
                            true
                        }
                        R.id.pp_copy -> {
                            copyToClipBoard(itemDetail.toString())
                            true
                        }
                        else -> false
                    }
                }

            })
            popup.inflate(R.menu.table_pop_up_menu)
            popup.show()
        } else {
            if (dataList.isNotEmpty()) {
                val tag = view.tag.toString().toLowerCase(Locale.ENGLISH)
                if (currentOrder.isEmpty()) {
                    if (tag == "id") {
                        currentOrder = "DESC"
                    } else {
                        currentOrder = "ASC"
                    }

                } else {
                    currentOrder = if (currentColumn == tag && currentOrder == "DESC") {
                        "ASC"
                    } else {
                        "DESC"
                    }
                }
                currentColumn = tag

                val image = sortingImages[view.id]
                updateSortingImage(image, currentOrder)
                getTableData(tableName, currentColumn, currentOrder)

            }

        }

    }

    private fun removeItem(id:Int,position: Int) {
        MaterialAlertDialogBuilder(context)
            .setMessage(getString(R.string.remove_item_alert_message_text))
            .setNegativeButton(getString(R.string.cancel_text)){dialog,which->
                dialog.dismiss()
            }.setPositiveButton(getString(R.string.remove_text)){dialog,which->
                dialog.dismiss()
                val isSuccess = tableGenerator.removeItem(tableName,id)
                if (isSuccess){
                    dataList.removeAt(position)
                    Toast.makeText(context,getString(R.string.remove_item_success_text),Toast.LENGTH_SHORT).show()
                    getTableData(tableName,"","")
                }
            }
            .create().show()
    }


    private var barcodeEditList =
        mutableListOf<Triple<TextInputEditText, AppCompatImageView, String>>()
    private var counter: Int = 0
    private var detailList = mutableListOf<Pair<String, String>>()
    private fun openQuickEditDialog(item: TableObject) {

        val quickEditParentLayout =
            LayoutInflater.from(context).inflate(R.layout.update_quick_edit_table_layout, null)
        val cancelDialogBtn =
            quickEditParentLayout.findViewById<MaterialButton>(R.id.quick_edit_dialog_cancel_btn)
        val updateDialogBtn =
            quickEditParentLayout.findViewById<MaterialButton>(R.id.quick_edit_dialog_update_btn)
        val quickEditWrapperLayout =
            quickEditParentLayout.findViewById<LinearLayout>(R.id.quick_edit_parent_layout)

        val codeDataLayout = LayoutInflater.from(context)
            .inflate(R.layout.quick_edit_single_layout, quickEditWrapperLayout, false)
        val codeDataValue =
            codeDataLayout.findViewById<TextInputEditText>(R.id.quick_edit_barcode_detail_text_input_field)
        val codeDataClearBrushView =
            codeDataLayout.findViewById<AppCompatImageView>(R.id.quick_edit_barcode_detail_cleaning_text_view)
        codeDataClearBrushView.id = counter
        codeDataClearBrushView.tag = "qe"

        barcodeEditList.add(
            Triple(
                codeDataValue,
                codeDataClearBrushView,
                "code_data"
            )
        )
        codeDataClearBrushView.setOnClickListener(this)
        codeDataValue.setText(item.code_data)
        codeDataValue.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateDialogBtn.isEnabled = true
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })
        quickEditWrapperLayout.addView(codeDataLayout)
        val dateLayout = LayoutInflater.from(context)
            .inflate(R.layout.quick_edit_single_layout, quickEditWrapperLayout, false)
        val dateValue =
            dateLayout.findViewById<TextInputEditText>(R.id.quick_edit_barcode_detail_text_input_field)
        val dateClearBrushView =
            dateLayout.findViewById<AppCompatImageView>(R.id.quick_edit_barcode_detail_cleaning_text_view)
        counter += 1
        dateClearBrushView.id = counter
        dateClearBrushView.tag = "qe"

        barcodeEditList.add(Triple(dateValue, dateClearBrushView, "date"))
        dateClearBrushView.setOnClickListener(this)
        dateValue.setText(item.date)
        dateValue.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateDialogBtn.isEnabled = true
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })
        quickEditWrapperLayout.addView(dateLayout)
        val imageLayout = LayoutInflater.from(context)
            .inflate(R.layout.quick_edit_single_layout, quickEditWrapperLayout, false)
        val imageValue =
            imageLayout.findViewById<TextInputEditText>(R.id.quick_edit_barcode_detail_text_input_field)
        val imageClearBrushView =
            imageLayout.findViewById<AppCompatImageView>(R.id.quick_edit_barcode_detail_cleaning_text_view)
        counter += 1
        imageClearBrushView.id = counter
        imageClearBrushView.tag = "qe"

        barcodeEditList.add(Triple(imageValue, imageClearBrushView, "image"))
        imageClearBrushView.setOnClickListener(this)
        imageValue.setText(item.image)
        imageValue.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateDialogBtn.isEnabled = true
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })
        quickEditWrapperLayout.addView(imageLayout)

        for (i in 0 until item.dynamicColumns.size) {
            val item1 = item.dynamicColumns[i]

            val layout = LayoutInflater.from(context)
                .inflate(R.layout.quick_edit_single_layout, quickEditWrapperLayout, false)
            val value =
                layout.findViewById<TextInputEditText>(R.id.quick_edit_barcode_detail_text_input_field)
            val clearBrushView =
                layout.findViewById<AppCompatImageView>(R.id.quick_edit_barcode_detail_cleaning_text_view)
            counter += 1
            clearBrushView.id = counter
            clearBrushView.tag = "qe"

            barcodeEditList.add(Triple(value, clearBrushView, item1.first))
            clearBrushView.setOnClickListener(this)
            value.setText(item1.second)
            value.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    updateDialogBtn.isEnabled = true
                }

                override fun afterTextChanged(s: Editable?) {
                }

            })
            quickEditWrapperLayout.addView(layout)

        }
        counter = 0

        val builder = MaterialAlertDialogBuilder(context)
        builder.setView(quickEditParentLayout)
        builder.setCancelable(false)
        val alert = builder.create()
        alert.show()

        cancelDialogBtn.setOnClickListener { alert.dismiss() }
        updateDialogBtn.setOnClickListener {
            startLoading(context)
            var flag = false

            for (i in 0 until barcodeEditList.size) {
                val triple = barcodeEditList[i]
                val value = triple.first.text.toString().trim()
                if (value.isEmpty()) {
                    flag = false
                    detailList.clear()
                    break
                } else {
                    flag = true
                    detailList.add(Pair(triple.third, value))
                }
            }
            if (flag) {
                alert.dismiss()
                if (detailList.isNotEmpty()) {
                    val isSuccess = tableGenerator.updateData(tableName, detailList, item.id)
                    if (isSuccess) {
                        dismiss()
                        getTableData(tableName, "", "")
                    } else {
                        dismiss()
                        showAlert(context, getString(R.string.database_update_failed_error))
                    }
                }
            } else {
                dismiss()
                showAlert(context, getString(R.string.empty_text_error))
            }

        }

    }

    private fun updateSortingImage(imageView: AppCompatImageView, order: String) {
        for (i in 0 until sortingImages.size) {
            val sImage = sortingImages[i]
            if (imageView.id == sImage.id && currentOrder == order) {
                sImage.setColorFilter(Color.WHITE)
                if (currentOrder.toLowerCase(Locale.ENGLISH) == "asc") {
                    sImage.setImageResource(R.drawable.ic_sort_asc)
                } else {
                    sImage.setImageResource(R.drawable.ic_sort_desc)
                }

            } else {
                sImage.setColorFilter(Color.parseColor("#808080"))
            }
        }
    }

    private fun copyToClipBoard(content: String) {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Barcode Detail", content)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Copied", Toast.LENGTH_LONG).show()

    }

    private fun exportCsv(tableName: String) {
        if (dataList.isNotEmpty()) {
            startLoading(context)
            val columns = tableGenerator.getTableColumns(tableName)
            val builder = StringBuilder()
            builder.append(columns!!.joinToString(","))

            for (j in 0 until dataList.size) {
                var image = ""
                val data = dataList[j]
                if (data.image.contains(",")) {
                    val temp = data.image.replace(",", ", ")
                    image = "\"$temp\""
                } else {
                    image = data.image
                }
                builder.append("\n${data.id},${data.code_data},${data.date},$image")
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
        } else {
            showAlert(context, getString(R.string.table_export_error_text))
        }
    }



}