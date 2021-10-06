package uz.lycr.lesson15firebasechatapp.models

class MessageGroupM {

    var id: String? = null
    var date: String? = null
    var message: String? = null
    var senderUid: String? = null
    var senderName: String? = null
    var senderImgUrl: String? = null
    var isSeen: Boolean? = null

    constructor()

    constructor(
        id: String?,
        date: String?,
        message: String?,
        senderUid: String?,
        senderName: String?,
        senderImgUrl: String?,
        isSeen: Boolean?
    ) {
        this.id = id
        this.date = date
        this.message = message
        this.senderUid = senderUid
        this.senderName = senderName
        this.senderImgUrl = senderImgUrl
        this.isSeen = isSeen
    }

}