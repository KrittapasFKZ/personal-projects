const { createAudioPlayer, createAudioResource, joinVoiceChannel, getVoiceConnection } = require('@discordjs/voice');
const config = require('../config.json');
const path = require('path');
const fs = require("fs");

exports.run = async (bot, message, args) => {
    let now = new Date(Date.now());
    let now_hours = now.getHours().toString().padStart(2, '0');
    let now_mins = now.getMinutes().toString().padStart(2, '0');
    let now_seconds = now.getSeconds().toString().padStart(2, '0');

    try {
        const newName = args.join(' ');
        let tempName = "ไม่มีชื่อ";

        if (!fs.existsSync(`./database/nick/${message.guild.id}.json`)) {
            let nick = {
                users: []
            };
            fs.writeFileSync(`./database/nick/${message.guild.id}.json`, JSON.stringify(nick, null, 2));
            bot.BotLogs("SYSTEM",`\x1b[35mCreated new guild's nickname for \x1b[90m[\x1b[37m${message.guild.id}.json\x1b[90m]`);
        };

        const rawData = fs.readFileSync(`./database/nick/${message.guild.id}.json`);
        const jsonData = JSON.parse(rawData);

        for (const user of jsonData.users) {
            if (user.id === message.author.id) {
                tempName = user.name;
                break;
            }
        }

        if (!newName) {
            return message.reply(`Your nickname is **${tempName}**`)
                .then(msg => {
                    setTimeout(() => {
                        if (msg) {
                            msg.delete().catch(console.error);
                            message.delete();
                        }
                    }, 5000);
                })
                .catch(console.error);
        }

        let found = false;
        for (const user of jsonData.users) {
            if (user.id === message.author.id) {
                user.name = newName;
                found = true;
                break;
            }
        }

        if (!found) {
            jsonData.users.push({ id: message.author.id, name: newName });
        }; 

        fs.writeFileSync(`./database/nick/${message.guild.id}.json`, JSON.stringify(jsonData, null, 2));

        bot.BotLogs(message.guild.name, `\x1b[35m\x1b[90m[\x1b[37m${message.author.tag}\x1b[90m] \x1b[35mchanged their name to \x1b[90m[\x1b[37m${newName}\x1b[90m]`)

        return message.reply(`Your new nickname is **${newName}**`)
            .then(msg => {
                setTimeout(() => {
                    if (msg) {
                        msg.delete().catch(console.error);
                        message.delete();
                    }
                }, 3000);
            })
            .catch(console.error);

    } catch (error) {
        bot.BotLogs("SYSTEM", `\x1b[31m---------------------------------------------------------------`);
        bot.BotLogs("SYSTEM", `\x1b[31mError Occurred: \x1b[37m"${error}" \x1b[31mfrom \x1b[37m"${path.basename(__filename)}"`);
        bot.BotLogs("SYSTEM", `\x1b[31m---------------------------------------------------------------`);
    }

};

exports.help = {
    name: 'nick',
    aliases: ['nickname', 'editnick', 'editname', 'editnickname']
};