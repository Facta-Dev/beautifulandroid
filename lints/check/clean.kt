package check

data class Counter(val value: Int)

fun increment(counter: Counter): Counter = counter.copy(value = counter.value + 1)

fun describe(counter: Counter): String = when (counter.value) {
    0 -> "empty"
    else -> "ready"
}
