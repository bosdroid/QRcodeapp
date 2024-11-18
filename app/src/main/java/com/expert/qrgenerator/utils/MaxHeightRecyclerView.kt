package com.expert.qrgenerator.utils

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.R

class MaxHeightRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    private var maxHeight: Int = 0

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.MaxHeightRecyclerView,
            0, 0
        ).apply {
            try {
                maxHeight = getDimensionPixelSize(R.styleable.MaxHeightRecyclerView_maxHeight, 0)
            } finally {
                recycle()
            }
        }
    }

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        val maxHeightSpec = if (maxHeight > 0) {
            MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST)
        } else {
            heightSpec
        }
        super.onMeasure(widthSpec, maxHeightSpec)
    }
}
