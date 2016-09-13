package im.actor.bots

import im.actor.bots.framework.farm

fun main(args: Array<String>) {

    var bot_endpoint = System.getenv("BOT_FATHER_ENDPOINT")
    var bot_username = System.getenv("BOT_FATHER_USERNAME")
    var bot_token = System.getenv("BOT_FATHER_TOKEN")
    var bot_trace = System.getenv("BOT_FATHER_TRACE")

    if (bot_username == null) {
        throw RuntimeException("Please, set BOT_FATHER_USERNAME environment variable")
    }
    if (bot_token == null) {
        throw RuntimeException("Please, set BOT_FATHER_TOKEN environment variable")
    }

    farm("NewFarm") {
        if (bot_endpoint != null) {
            endpoint = bot_endpoint
        }
        bot(BotFatherBot::class) {
            name = bot_username
            token = bot_token
            traceHook = bot_trace
        }
    }
}