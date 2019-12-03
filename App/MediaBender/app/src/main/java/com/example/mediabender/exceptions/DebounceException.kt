package com.example.mediabender.exceptions

import java.lang.RuntimeException

/**
 * A Custom exception to catch when an HTTP request is debounced
 */
class DebounceException(debounceTime: Number): RuntimeException("Cannot run task within ${debounceTime}ms")