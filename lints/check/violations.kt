package check

// Deliberate violations for the CI harness. Do not copy this file.

fun magic(): Int = 12345  // MagicNumber

fun longFunction(seed: Int): Int {  // LongMethod / ReturnCount
    var total = seed
    if (total > 10) {
        if (total > 20) {
            if (total > 30) {
                if (total > 40) {
                    total += 1
                } else {
                    total += 2
                }
            } else {
                total += 3
            }
        } else {
            total += 4
        }
    } else {
        total += 5
    }
    return total
}

fun swallowed() {  // SwallowedException
    try {
        error("boom")
    } catch (exception: Exception) {
        // ignored
    }
}

// TODO: implement this before shipping  (ForbiddenComment)
fun pending(): Unit = Unit
