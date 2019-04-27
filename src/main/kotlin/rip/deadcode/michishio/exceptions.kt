package rip.deadcode.michishio


class MichishioException(messageKey: String, vararg args: Any) : RuntimeException(getMessage(messageKey, *args))
