package com.expert.qrgenerator.view.activities

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TableRow
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.R
import com.expert.qrgenerator.adapters.TableDetailAdapter
import com.expert.qrgenerator.model.TableObject
import com.expert.qrgenerator.utils.TableGenerator
import com.google.android.material.textview.MaterialTextView
import java.util.*


class TableViewActivity : BaseActivity(), TableDetailAdapter.OnItemClickListener,View.OnClickListener {

    private lateinit var context: Context
    private lateinit var toolbar: Toolbar
    private lateinit var tableGenerator: TableGenerator
    private lateinit var tableDetailRecyclerView: RecyclerView
    private lateinit var tableMainLayout: TableLayout
    private var tableName: String = ""
    private var dataList = mutableListOf<TableObject>()
    private lateinit var adapter: TableDetailAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_table_view)

        initViews()
        setUpToolbar()
        getTableData()
    }

    private fun initViews() {
        context = this
        tableGenerator = TableGenerator(context)
        toolbar = findViewById(R.id.toolbar)
        if (intent != null && intent.hasExtra("TABLE_NAME")) {
            tableName = intent.getStringExtra("TABLE_NAME")!!
        }
//        tableDetailRecyclerView = findViewById(R.id.tables_detail_recyclerview)
//        tableDetailRecyclerView.layoutManager = LinearLayoutManager(context)
//        tableDetailRecyclerView.hasFixedSize()
//        adapter = TableDetailAdapter(context, dataList as ArrayList<TableObject>)
//        tableDetailRecyclerView.adapter = adapter
//        adapter.setOnItemClickListener(this)
        tableMainLayout = findViewById(R.id.table_main)
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


    val layoutParams = TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 90, 2f)
    private fun getTableData() {
        layoutParams.setMargins(5, 5, 5, 5)
        dataList.addAll(tableGenerator.getTableDate(tableName))
        val columns = tableGenerator.getTableColumns(tableName)
        tableMainLayout.weightSum = dataList.size * 2F
        val tableHeaders = TableRow(context)
        for (i in columns!!.indices) {
            val textView = MaterialTextView(context)
            //textView.textSize = resources.getDimension(R.dimen.smallText)
            textView.layoutParams =
                TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 90, 2f)
            textView.setPadding(10, 10, 10, 10)
            textView.setBackgroundColor(ContextCompat.getColor(context, R.color.purple_dark))
            textView.gravity = Gravity.CENTER
            textView.setTextColor(Color.WHITE)
            textView.text = columns[i].toUpperCase(Locale.ENGLISH)
            tableHeaders.addView(textView)
        }

        tableMainLayout.addView(tableHeaders)

        if (dataList.isNotEmpty()) {
            for (j in 0 until dataList.size) {
                val data = dataList[j]
                val tableRow = TableRow(context)
                tableRow.id = j
                tableRow.setOnClickListener(this)
                val textViewId = MaterialTextView(context)

                textViewId.layoutParams = layoutParams
                textViewId.setPadding(10, 10, 10, 10)
                textViewId.gravity = Gravity.CENTER

                val textViewCodeDate = MaterialTextView(context)
                textViewCodeDate.layoutParams = layoutParams
                textViewCodeDate.setPadding(10, 10, 10, 10)
                textViewCodeDate.gravity = Gravity.CENTER

                val textViewDate = MaterialTextView(context)
                textViewDate.layoutParams = layoutParams
                textViewDate.setPadding(10, 10, 10, 10)
                textViewDate.gravity = Gravity.CENTER

                textViewId.text = "${data.id}"
                textViewCodeDate.text = data.code_data
                textViewDate.text = data.date
                tableRow.addView(textViewId)
                tableRow.addView(textViewCodeDate)
                tableRow.addView(textViewDate)


                val textViewImage = MaterialTextView(context)
                textViewDate.layoutParams = layoutParams
                textViewDate.setPadding(10, 10, 10, 10)
                textViewDate.gravity = Gravity.CENTER

                if (data.image.isNotEmpty() && data.image.length >= 20) {
                    textViewImage.text = data.image.substring(0, 20)
                } else {
                    textViewImage.text = data.image
                }
                tableRow.addView(textViewImage)


                if (data.dynamicColumns.size > 0) {
                    for (k in 0 until data.dynamicColumns.size) {
                        val item = data.dynamicColumns[k]
                        val textView = MaterialTextView(context)
                        textView.layoutParams = layoutParams
                        textView.setPadding(10, 10, 10, 10)
                        textView.gravity = Gravity.CENTER
                        textView.text = item.second
                        tableRow.addView(textView)
                    }

                }
                if (j % 2 == 0) {
                    tableRow.setBackgroundColor(Color.parseColor("#EAEAF6"))
                } else {
                    tableRow.setBackgroundColor(Color.parseColor("#f2f2f2"))
                }
                tableMainLayout.addView(tableRow)
            }
        }


//        dataList.addAll(tableGenerator.getTableDate(tableName))
//        adapter.notifyDataSetChanged()
    }

    override fun onItemClick(position: Int) {
        val tableObject = dataList[position]
        showAlert(context, tableObject.toString())
    }

    override fun onClick(v: View?) {
//        val position = v!!.id
//        val item = dataList[position]
//        if (item.image.isNotEmpty()){
//            showAlert(context,dataList[position].image)
//        }

    }

}