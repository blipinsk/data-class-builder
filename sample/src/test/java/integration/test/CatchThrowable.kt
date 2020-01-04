package integration.test

object CatchThrowable {
    private var lastThrowable: Throwable? = null

    fun <T> catchThrowable(callable: () -> T): T? {
        lastThrowable = null

        try {
            return callable.invoke()
        } catch (e: Throwable) {
            lastThrowable = e
            return null
        }
    }

    fun caughtThrowable(): Throwable? {
        return lastThrowable
    }
}

