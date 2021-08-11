package com.expert.qrgenerator.activities

class UrlData {
    var link: String? = null
    var type: String? = null

    constructor() {}
    constructor(link: String?, type: String?) {
        this.link = link
        this.type = type
    }
}