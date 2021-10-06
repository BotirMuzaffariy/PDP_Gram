package uz.lycr.lesson15firebasechatapp.models

import java.io.Serializable

class UserM : Serializable {
    var name: String? = null
    var photoUrl: String? = null
    var uid: String? = null
    var email: String? = null
    var isOnline: Boolean? = null

    constructor()

    constructor(
        name: String?,
        photoUrl: String?,
        email: String?,
        uid: String?,
        isOnline: Boolean?
    ) {
        this.name = name
        this.photoUrl = photoUrl
        this.email = email
        this.uid = uid
        this.isOnline = isOnline
    }

}