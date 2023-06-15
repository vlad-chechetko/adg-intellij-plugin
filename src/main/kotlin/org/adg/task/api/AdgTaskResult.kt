package org.adg.task.api

class AdgTaskResult {
    var isSuccess: Boolean
    var messageToUser: String? = null

    constructor(success: Boolean) {
        isSuccess = success
    }

    constructor(success: Boolean, messageToUser: String?) {
        isSuccess = success
        this.messageToUser = messageToUser
    }
}