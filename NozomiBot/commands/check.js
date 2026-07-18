const { EmbedBuilder } = require('discord.js');
const config = require('../config.json');
const path = require('path');
const fs = require("fs");

let fishing_ponds_file = './database/fishing_ponds.json';
let fishing_ponds = new Map();

if (fs.existsSync(fishing_ponds_file)) {
    const rawData = fs.readFileSync(fishing_ponds_file);
    const eventData = JSON.parse(rawData);
    fishing_ponds = new Map(eventData);
}

exports.run = async (bot, message, args) => {
    let now = new Date(Date.now());
    let now_hours = now.getHours().toString().padStart(2, '0');
    let now_mins = now.getMinutes().toString().padStart(2, '0');
    let now_seconds = now.getSeconds().toString().padStart(2, '0');

    try {
        if (message.author.id == "605361556297089035") {

            const rawData = fs.readFileSync('./database/fishing_ponds.json');
            const fishingData = JSON.parse(rawData);

            const eventEmbed = new EmbedBuilder()
                .setTitle('Active Fishing Events')
                .setColor('#00FF00')
                .setTimestamp();

            if (fishingData.length === 0) {
                const EventEmbed_Failed = new EmbedBuilder()
                    .setTitle(`Error Occurred!`)
                    .setDescription(`❌ No Active Event!`)
                    .setColor('#FF0000')
                    .setTimestamp();

                return message.reply({ embeds: [EventEmbed_Failed] })
                    .then(msg => {
                        setTimeout(() => {
                            if (msg) {
                                msg.delete().catch(console.error);
                                message.delete();
                            }
                        }, 3000);
                    })
                    .catch(console.error);
            };

            async function LoadBoard() {
                fishingData.forEach(async ([userId, eventData]) => {
                    let usr = await bot.users.fetch(userId);
                    eventEmbed.addFields({ name: `${usr.tag}'s Fishing Event`, value: `Start Time: <t:${Math.floor(eventData.startTime / 1000)}:R>\nFinish Time: <t:${eventData.finishTime}:R>` });
                });
            };

            await LoadBoard();

            return message.reply({ embeds: [eventEmbed] })
                .then(msg => {
                    setTimeout(() => {
                        if (msg) {
                            msg.delete().catch(console.error);
                            message.delete();
                        }
                    }, 15000);
                })
                .catch(console.error);
        } else {
            return message.reply(`**No Permission.**`)
                .then(msg => {
                    setTimeout(() => {
                        if (msg) {
                            msg.delete().catch(console.error);
                            message.delete()
                        }
                    }, 3000);
                })
                .catch(console.error);
        }
    } catch (error) {
        bot.BotLogs("SYSTEM", `\x1b[31m---------------------------------------------------------------`, "\x1b[0m");
        bot.BotLogs("SYSTEM", `\x1b[31mError Occurred: \x1b[37m"${error}" \x1b[31mfrom \x1b[37m"${path.basename(__filename)}"`, "\x1b[0m");
        bot.BotLogs("SYSTEM", `\x1b[31m---------------------------------------------------------------`, "\x1b[0m");
    }
};

exports.fishing_ponds = fishing_ponds;

exports.help = {
    name: 'check',
    aliases: ['log']
};
