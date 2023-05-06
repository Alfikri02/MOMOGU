package com.example.momogu.utils

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.annotation.DimenRes
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ItemDecorationWithCenterMargin(context: Context, @DimenRes private val spacingRes: Int) :
    RecyclerView.ItemDecoration() {

    private val spacing: Int = context.resources.getDimensionPixelSize(spacingRes)

    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)

        val layoutManager = parent.layoutManager as? GridLayoutManager ?: return
        val spanCount = layoutManager.spanCount
        val position = parent.getChildAdapterPosition(view)
        val column = position % spanCount
        val row = position / spanCount

        outRect.left = spacing - column * spacing / spanCount
        outRect.right = spacing - column * spacing / spanCount

        if (row == 0) {
            outRect.top = spacing
        }
        outRect.bottom = spacing
    }
}
