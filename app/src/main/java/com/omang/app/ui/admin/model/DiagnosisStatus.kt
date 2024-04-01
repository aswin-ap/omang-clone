package com.omang.app.ui.admin.model

data class AppDiagnosisStatus(
    val position: Int,
    var value: DiagnosticsStatus,

    )


enum class DiagnosticsStatus {
    INITIAL, LOADING, SUCCESS, FAILED
}