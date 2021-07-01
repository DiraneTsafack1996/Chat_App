package com.example.dirane.Notifications

import com.google.android.gms.tasks.Task

class Token {
    var token: String? = null

    constructor(token: String?) {
        this.token = token
    }

    constructor() {}
    constructor(token: Task<String?>?) {}
}