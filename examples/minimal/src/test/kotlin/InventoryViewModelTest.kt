package example

import kotlin.test.Test
import kotlin.test.assertEquals

class InventoryViewModelTest {
    @Test
    fun emptyListBecomesEmptyState() {
        val viewModel = InventoryViewModel()

        viewModel.load(emptyList())

        assertEquals(InventoryState.Empty, viewModel.state.value)
    }

    @Test
    fun rowsBecomeReadyState() {
        val viewModel = InventoryViewModel()

        viewModel.load(listOf("first", "second"))

        assertEquals(InventoryState.Ready(listOf("first", "second")), viewModel.state.value)
    }

    @Test
    fun failureKeepsTheCorrelationId() {
        val viewModel = InventoryViewModel()

        viewModel.fail("c_01")

        assertEquals(InventoryState.Error("c_01"), viewModel.state.value)
    }
}
