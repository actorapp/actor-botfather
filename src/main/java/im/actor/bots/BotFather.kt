package im.actor.bots

import im.actor.bots.BotMessages
import im.actor.bots.framework.MagicForkScope
import im.actor.bots.framework.stateful.*
import im.actor.bots.framework.traits.initLocalize
import org.json.JSONArray
import org.json.JSONObject
import scala.Option
import java.util.*

class BotFatherBot(scope: MagicForkScope) : MagicStatefulBot(scope) {

    var selectedName: String? = null
    var selectedBotUID: Int? = null
    var selectedCommandName: String? = null

    var registeredBots = ArrayList<String>()

    override fun configure() {

        admins.add("steve")

        enablePersistent = false

        if (scope.peer.isGroup) {
            return
        }

        val stored = scope.forkKeyValue.getJSONValue("bots")
        if (stored != null) {
            val bots = stored.getJSONArray("bots")
            for (i in bots) {
                registeredBots.add(i as String)
            }
        }

        initLocalize("BotFather",
                listOf("en", "ru", "zn"),
                getUser(scope.peer.id).preferredLanguages.toTypedArray())
        enableSimpleMode(localized("message.start"), "message.unknown")

        command("/newbot") {

            before {
                selectedName = null
                goto("ask_name")
            }

            expectInput("ask_name") {
                before {
                    sendText(localized("message.new"))
                }
                received {
                    selectedName = text
                    goto("ask_username")
                }
                validate {
                    if (isCancel) {
                        sendText(localized("message.cancel"))
                        goto("main")
                        return@validate false
                    }
                    if (!isText) {
                        sendText(localized("message.new_invalid"))
                        return@validate false
                    }

                    return@validate true
                }
            }

            expectInput("ask_username") {

                before {
                    sendText(localized("message.new_username"))
                }

                received {
                    val username = if (text.startsWith("@")) text.substring(1) else text
                    val bot = createBot(username, selectedName!!)
                    if (bot != null) {
                        addUserBot(text)
                        sendText(localized("message.new_success").replace("\$token", bot.token()))
                        selectedName = null
                        goto("main")
                        return@received
                    }

                    sendText(localized("message.new_username_used"))
                }

                validate {

                    if (isCancel) {
                        sendText(localized("message.cancel"))
                        goto("main")
                        return@validate false
                    }

                    if (!isText) {
                        sendText(localized("message.new_username_invalid"))
                    }

                    if (text.length < 5) {
                        sendText(localized("message.new_username_short"))
                        return@validate false
                    }
                    if (text.length > 32) {
                        sendText(localized("message.new_username_long"))
                        return@validate false
                    }

                    return@validate true
                }
            }
        }

        command("/setphoto") {
            before {
                if (registeredBots.size == 0) {
                    sendText(localized("message.list_empty"))
                    goto("main")
                    return@before
                }

                var message = localized("message.edit_photo")
                for (s in registeredBots) {
                    message += "\n - [@$s](send:$s)"
                }
                sendText(message)
                goto("pick_bot")
            }

            pickBotAction("pick_bot", this) {
                sendText(localized("message.edit_photo_ask"))
                goto("pick_photo")
            }

            expectInput("pick_photo") {
                received {
                    if (changeUserAvatar(selectedBotUID!!, doc.fileId(), doc.accessHash())) {
                        sendText(localized("message.success"))
                    } else {
                        sendText(localized("message.edit_photo_error"))
                    }
                    selectedBotUID = null
                    goto("main")
                }
                validate {
                    if (isCancel) {
                        sendText(localized("message.cancel"))
                        goto("main")
                        return@validate false
                    }
                    if (!isPhoto) {
                        sendText(localized("message.edit_photo_ask_invalid"))
                        return@validate false
                    }
                    return@validate true
                }
            }
        }

        command("/setname") {
            before {
                if (registeredBots.size == 0) {
                    sendText(localized("message.list_empty"))
                    goto("main")
                    return@before
                }

                var message = localized("message.edit_name")
                for (s in registeredBots) {
                    message += "\n - [@$s](send:$s)"
                }
                sendText(message)

                goto("pick_bot")
            }

            pickBotAction("pick_bot", this) {
                sendText(localized("message.edit_name_ask"))
                goto("pick_name")
            }

            expectInput("pick_name") {
                received {
                    if (changeUserName(selectedBotUID!!, text)) {
                        sendText(localized("message.success"))
                    } else {
                        sendText("message.edit_name_error")
                    }
                    selectedBotUID = null
                    goto("main")
                }
                validate {
                    if (isCancel) {
                        sendText(localized("message.cancel"))
                        return@validate true
                    }
                    if (!isText) {
                        sendText(localized("message.edit_name_ask_invalid"))
                        return@validate false
                    }
                    return@validate true
                }
            }
        }

        command("/setabout") {
            before {
                if (registeredBots.size == 0) {
                    sendText(localized("message.list_empty"))
                    goto("main")
                    return@before
                }

                var message = localized("message.edit_about")
                for (s in registeredBots) {
                    message += "\n - [@$s](send:$s)"
                }
                sendText(message)

                goto("pick_bot")
            }

            pickBotAction("pick_bot", this) {
                sendText(localized("message.edit_about_ask"))
                goto("pick_name")
            }

            expectInput("pick_name") {
                received {
                    if (changeUserAbout(selectedBotUID!!, text)) {
                        sendText(localized("message.success"))
                    } else {
                        sendText(localized("message.edit_about_error"))
                    }
                    selectedBotUID = null
                    goto("main")
                }
                validate {
                    if (isCancel) {
                        sendText(localized("message.cancel"))
                        return@validate true
                    }
                    if (!isText) {
                        sendText(localized("message.edit_about_ask_invalid"))
                        return@validate false
                    }
                    return@validate true
                }
            }
        }

        oneShot("/list") {
            if (registeredBots.size == 0) {
                sendText(localized("message.list_empty"))
            } else {
                var message = localized("message.list")
                for (s in registeredBots) {
                    message += "\n - @$s"
                }
                sendText(message)
            }
        }

        command("/removecommand") {
            before {
                if (registeredBots.size == 0) {
                    sendText(localized("message.list_empty"))
                    goto("main")
                    return@before
                }

                var message = localized("message.remove_command")
                for (s in registeredBots) {
                    message += "\n - [@$s](send:$s)"
                }
                sendText(message)

                goto("pick_bot")
            }

            pickBotAction("pick_bot", this) {
                sendText(localized("message.remove_command_ask_command_name"))
                goto("remove_command_name")
            }

            expectInput("remove_command_name") {
                received {
                    if (removeSlashCommand(selectedBotUID!!, text)) {
                        sendText(localized("message.success"))
                    } else {
                        sendText(localized("message.remove_command_error"))
                    }
                    selectedCommandName = null
                    selectedBotUID = null
                    goto("main")
                }
                validate {
                    if (isCancel) {
                        sendText(localized("message.cancel"))
                        return@validate true
                    }
                    if (!isText) {
                        sendText(localized("message.remove_command_ask_command_name_invalid"))
                        return@validate false
                    }
                    return@validate true
                }
            }
        }

        command("/addcommand") {
            before {
                if (registeredBots.size == 0) {
                    sendText(localized("message.list_empty"))
                    goto("main")
                    return@before
                }

                var message = localized("message.add_command")
                for (s in registeredBots) {
                    message += "\n - [@$s](send:$s)"
                }
                sendText(message)

                goto("pick_bot")
            }

            pickBotAction("pick_bot", this) {
                sendText(localized("message.add_command_ask_command_name"))
                goto("pick_command_name")
            }

            expectInput("pick_command_name") {
                received {
                    selectedCommandName = text
                    sendText(localized("message.add_command_ask_command_description"))
                    goto("pick_command_description")
                }
                validate {
                    if (isCancel) {
                        sendText(localized("message.cancel"))
                        return@validate true
                    }
                    if (!isText) {
                        sendText(localized("message.add_command_ask_command_name_invalid"))
                        return@validate false
                    }
                    return@validate true
                }
            }

            expectInput("pick_command_description") {
                received {
                    if (addSlashCommand(selectedBotUID!!, BotMessages.BotCommand(selectedCommandName, text, Option.empty()))) {
                        sendText(localized("message.success"))
                    } else {
                        sendText(localized("message.add_command_error"))
                    }
                    selectedCommandName = null
                    selectedBotUID = null
                    goto("main")
                }
                validate {
                    if (isCancel) {
                        sendText(localized("message.cancel"))
                        return@validate true
                    }
                    if (!isText) {
                        sendText(localized("message.add_command_ask_command_name_invalid"))
                        return@validate false
                    }
                    return@validate true
                }
            }
        }
    }

    fun pickBotAction(name: String, container: ExpectContainer, closure: () -> Unit) {
        container.apply {
            expectInput(name) {
                before {
                    selectedBotUID = null
                }
                received {
                    // Cancelling
                    if (isCommand) {
                        goto("main")
                        return@received
                    }

                    val username = if (text.startsWith("@")) text.substring(1) else text
                    val user = findUser(username)
                    if (user != null) {
                        if (user.isABot) {
                            if (!isAdminScope() && !registeredBots.contains(user.username().get())) {
                                sendText(localized("message.pick_not_your"))
                            } else {
                                selectedBotUID = user.id()
                                closure()
                            }
                        } else {
                            sendText(localized("message.pick_human"))
                        }
                    } else {
                        sendText(localized("message.pick_nothing_found").replace("\$text", username))
                    }
                }
                validate {
                    if (isCancel) {
                        sendText(localized("message.cancel"))
                        return@validate true
                    }
                    if (!isText) {
                        sendText(localized("message.new_invalid"))
                        return@validate false
                    }
                    return@validate true
                }
            }
        }
    }

    // Saving context

    override fun onSaveState(state: JSONObject) {
        super.onSaveState(state)

        if (selectedName != null) {
            state.put("createdName", selectedName)
        }
        if (selectedBotUID != null) {
            state.put("selectedBotUID", selectedBotUID!!)
        }
    }

    override fun onRestoreState(state: JSONObject) {
        super.onRestoreState(state)

        selectedName = state.optString("createdName")
        selectedBotUID = state.optInt("selectedBotUID")
    }

    fun addUserBot(userName: String) {
        registeredBots.add(userName)

        val array = JSONArray()
        for (s in registeredBots) {
            array.put(s)
        }
        val obj = JSONObject()
        obj.put("bots", array)

        scope.forkKeyValue.setJSONValue("bots", obj)
    }
}