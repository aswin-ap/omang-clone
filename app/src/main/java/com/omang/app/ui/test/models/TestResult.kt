package com.omang.app.ui.test.models


data class TestResult(
    var testid: String,
    var results: Results?,
    var testName: String,
    var startTime: String?,
    var type: Int,

    )