package com.expert.qrgenerator.interfaces

/**
 * Interface for handling actions when a type is selected.
 *
 * Implement this interface to define custom behavior when a type is selected.
 */
interface OnCompleteAction {

    /**
     * Called when a type is selected.
     *
     * @param data The data associated with the selection.
     * @param position The position of the selected item.
     * @param type The type of the selected item.
     */
    fun onTypeSelected(data: String, position: Int, type: String)
}
