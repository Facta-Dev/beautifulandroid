package example

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface InventoryState {
    data object Loading : InventoryState
    data object Empty : InventoryState
    data class Ready(val rows: List<String>) : InventoryState
    data class Error(val correlationId: String?) : InventoryState
}

class InventoryViewModel {
    private val mutableState = MutableStateFlow<InventoryState>(InventoryState.Loading)
    val state: StateFlow<InventoryState> = mutableState.asStateFlow()

    fun load(rows: List<String>) {
        mutableState.value = if (rows.isEmpty()) {
            InventoryState.Empty
        } else {
            InventoryState.Ready(rows)
        }
    }

    fun fail(correlationId: String?) {
        mutableState.value = InventoryState.Error(correlationId)
    }
}
