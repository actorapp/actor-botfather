# Actor BotFather

Bot that is used to create other bots in an easy way on Actor Server.

## Create Bot Father on your Actor Server

BotFather need to run in priveledged mode to create other bots. To achieve this you need to create bot token via `actor-cli` with admin privilegies:

```
actor-cli create-bot --username botfather --name "Bot Father" --admin
```

## Bot Installation

Docker installation is prefferable way to start BotFather and you can do it with command:

```
docker run -e BOT_FATHER_USERNAME=<username_of_botfather> -e BOT_FATHER_TOKEN=<bot-token-from-actor-cli> -e BOT_FATHER_ENDPOINT=wss://api.actor.im actor/bot-father
```

| Argument        | Required           | Description  |
| ------------- |:-------------:| :-----|
| BOT_FATHER_USERNAME      | yes | Username of a BotFather |
| BOT_FATHER_TOKEN      | yes      |   Token for BotFather |
| BOT_FATHER_ENDPOINT | no      | Endpoint to server API. If no set will connect to main actor cloud. |

Alternative is building bot from sources and running app directly. BTW we don't recommend this and won't support such king of installations.

## Licesnse

MIT
