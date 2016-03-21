package com.sam_chordas.android.stockhawk.touch_helper;

import android.support.v7.widget.RecyclerView;

/**
 * Created by Robert Mathew on 20/6/15.
 * credit to Paul Burke (ipaulpro)
 */
/**
 * Listener for manual initiation of a drag.
 */
public interface OnStartDragListener {

    /**
     * Called when a view is requesting a start of a drag.
     *
     * @param viewHolder The holder of the view to drag.
     */
    void onStartDrag(RecyclerView.ViewHolder viewHolder);

}
