package com.example.mediabender

import java.lang.RuntimeException

class DebounceException(debounceTime: Number): RuntimeException("Cannot run task within ${debounceTime}ms")