@file:Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")

package icu.pboymt.mayer.logging

import icu.pboymt.mayer.MayerFloatingService
import org.tinylog.core.LogEntry
import org.tinylog.core.LogEntryValue
import org.tinylog.writers.Writer
import java.util.Map


class OverlayWindowWriter(@Suppress("UNUSED_PARAMETER") properties: Map<String, String>) : Writer {

    override fun getRequiredLogEntryValues(): MutableCollection<LogEntryValue> {
        return mutableListOf(LogEntryValue.LEVEL, LogEntryValue.MESSAGE)
    }

    override fun write(logEntry: LogEntry?) {
        if (logEntry != null) {
            MayerFloatingService.floatingTime = logEntry.timestamp.toDate().time
            MayerFloatingService.floatingText = logEntry.message
        }
    }

    override fun flush() {
        // Service does not need to be flushed
    }

    override fun close() {
        // Service does not need to be closed
    }


}