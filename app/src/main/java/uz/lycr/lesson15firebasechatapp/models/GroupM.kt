package uz.lycr.lesson15firebasechatapp.models

import java.io.Serializable

class GroupM : Serializable {

    var name: String? = null
    var about: String? = null
    var groupId: String? = null
    var creatorId: String? = null
    var photoUrl: String? = null

    constructor()

    constructor(name: String?, about: String?, groupId: String?, creatorId: String?, photoUrl: String?) {
        this.name = name
        this.about = about
        this.groupId = groupId
        this.creatorId = creatorId
        this.photoUrl = photoUrl
    }

}