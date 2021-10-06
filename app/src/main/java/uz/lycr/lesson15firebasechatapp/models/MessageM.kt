package uz.lycr.lesson15firebasechatapp.models

class MessageM {

    var id: String? = null
    var date: String? = null
    var message: String? = null
    var senderUid: String? = null
    var receiverUid: String? = null
    var isSeen: Boolean? = null

    constructor()

    constructor(
        id: String?,
        date: String?,
        message: String?,
        isSeen: Boolean?,
        senderUid: String?,
        receiverUid: String?
    ) {
        this.id = id
        this.date = date
        this.message = message
        this.isSeen = isSeen
        this.senderUid = senderUid
        this.receiverUid = receiverUid
    }

}