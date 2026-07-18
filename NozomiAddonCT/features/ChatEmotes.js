import Settings from "../config";

let replacements = {
    '<3': '❤',
    'o/': '( ﾟ◡ﾟ)/',
    ':star:': '✮',
    ':yes:': '✔',
    ':no:': '✖',
    ':java:': '☕',
    ':arrow:': '➜',
    ':shrug:': '¯\\_(\u30c4)_/¯',
    ':tableflip:': '(╯°□°）╯︵ ┻━┻',
    ':fliptable:': '(╯°□°）╯︵ ┻━┻',
    ':totem:': '☉_☉',
    ':typing:': '✎...',
    ':maths:': '√(π+x)=L',
    ':snail:': "@'-'",
    ':thinking:': '(0.o?)',
    ':gimme:': '༼つ◕_◕༽つ',
    ':wizard:': '(' - ')⊃━☆ﾟ.*･｡ﾟ',
    ':pvp:': '⚔',
    ':peace:': '✌', 
    ':puffer:': "<('O')>",
    'h/': 'ヽ(^◇^*)/',
    ':sloth:': '(・⊝・)',
    ':dog:': '(ᵔᴥᵔ)',
    ':dj:': 'ヽ(⌐■_■)ノ♬',
    ':yey:': 'ヽ (◕◡◕) ﾉ',
    ':snow:': '☃',
    ':dab:': '<o/',
    ':cat:': '= ＾● ⋏ ●＾ =',
    ':cute:': '(✿◠‿◠)',
    ':skull:': '☠',
}

let replaced = false
register('messageSent', (message, event) => {
    if (!Settings.chatEmotes) return
    if (message.startsWith('/') && !message.startsWith('/pc') && !message.startsWith('/ac') && !message.startsWith('/gc') && !message.startsWith('/msg') && !message.startsWith('/w') && !message.startsWith('/r')) return
    replaced = false
    message = message.split(' ')
    for (let i = 0; i < message.length; i++) {
        if (Object.keys(replacements).includes(message[i])) {
            replaced = true
            message[i] = replacements[message[i]]
        }
    }
    message = message.join(' ')
    if (!replaced) return
    cancel(event)
    ChatLib.say(message)
})