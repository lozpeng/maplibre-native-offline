package org.cwcc.ani.map.utils

import timber.log.Timber

class GeoAniDebugTree:Timber.DebugTree() {
    override fun createStackElementTag(element: StackTraceElement) =
        "(${element.fileName}:${element.lineNumber})#${element.methodName}"


    override fun formatMessage(message: String, args: Array<out Any?>): String {
        return "[GeoAniDebugTree] $message"
    }
}