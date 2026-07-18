import Settings from "../config";
import Dungeon from "../../BloomCore/dungeons/Dungeon";
import { registerWhen } from "../utils/utils";

registerWhen(register('chat', (name, event) => {
    if (!Settings.dungeondeathMessage) return;
    if (!Dungeon.inDungeon) return;
    const message = ChatLib.getChatMessage(event)
    if (message.includes('reconnected') || message.includes('Cata Level')) return
    let text = Settings.dungeondeathMessageText
    if (text.includes('{name}')) {
        text = text.replace(/{name}/g, name)
    };
    if (text.includes(',')) {
        messagesArray = text.split(',')
        text = messagesArray[Math.floor(Math.random() * messagesArray.length)]
    };
    ChatLib.command(`pc ${text}`)
}).setCriteria(/^ ☠ (\S+) .+/), () => Settings.dungeondeathMessage && Dungeon.inDungeon)