//const config = require('../config.json');
const { EmbedBuilder } = require('discord.js');
const fs = require("fs");
const path = require('path');
const pidusage = require('pidusage');

exports.run = async (bot, message, args) => {
    let now = new Date(Date.now());
    let now_hours = now.getHours().toString().padStart(2, '0');
    let now_mins = now.getMinutes().toString().padStart(2, '0');
    let now_seconds = now.getSeconds().toString().padStart(2, '0');

    function generateUUID() {
        const characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
        let uuid = '';
    
        for (let i = 0; i < 16; i++) {
            const randomIndex = Math.floor(Math.random() * characters.length);
            uuid += characters[randomIndex];
        }
    
        return uuid;
    }

    try {
        if (message.author.id == "605361556297089035") {

            let startTime = Date.now()
            let endTime = Number((startTime + 25000) / 1000).toFixed(0)
            let msg;
            let perf_uuid = generateUUID();
            let perf_total = 0
            let perf_count = 0

            bot.BotLogs("SYSTEM", `\x1b[34mMonitoring Performance \x1b[37mID: ${perf_uuid}`);

            pidusage(process.pid, (err, stats) => {
                if (err) {
                    return;
                };

                const Board = new EmbedBuilder()
                    .setAuthor({ name: `NozomiBot Monitor | ID: ${perf_uuid}`, iconURL: `${bot.user.displayAvatarURL()}` })
                    .setThumbnail(bot.user.displayAvatarURL())
                    .setColor('#FF00FF')
                    .addFields(
                        { name: `CPU Usage`, value: `\`\`\`${stats.cpu.toFixed(2)}%\`\`\`` },
                        { name: `Memory Usage`, value: `\`\`\`${(stats.memory / (1024 * 1024)).toFixed(2)} MB\`\`\`` },
                        { name: `Embed Delete`, value: `<t:${endTime}:R>` }
                    )

                message.delete();
                message.channel.send({ embeds: [Board] }).then(sentMsg => {
                    msg = sentMsg;
                });

            });

            let interval = setInterval(async () => {
                pidusage(process.pid, (err, stats) => {
                    if (err) {
                        return;
                    };

                    const Board = new EmbedBuilder()
                        .setAuthor({ name: `NozomiBot Monitor | ID: ${perf_uuid}`, iconURL: `${bot.user.displayAvatarURL()}` })
                        .setThumbnail(bot.user.displayAvatarURL())
                        .setColor('#FF00FF')
                        .addFields(
                            { name: `CPU Usage`, value: `\`\`\`${stats.cpu.toFixed(2)}%\`\`\`` },
                            { name: `Memory Usage`, value: `\`\`\`${(stats.memory / (1024 * 1024)).toFixed(2)} MB\`\`\`` },
                            { name: `Embed Delete`, value: `<t:${endTime}:R>` }
                        )

                    perf_total += Number((stats.memory / (1024 * 1024)).toFixed(2))
                    perf_count += 1
                    msg.edit({ embeds: [Board] }).catch(console.error);

                });
            }, 5000);

            setTimeout(() => {
                clearInterval(interval);
                msg.delete();
                bot.BotLogs("SYSTEM", `\x1b[34mCompleted Performance Monitor Average: \x1b[37m${(perf_total / perf_count).toFixed(2)} MB`);
            }, 25000);

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
        };
    } catch (error) {
        bot.BotLogs("SYSTEM", `\x1b[31m---------------------------------------------------------------`);
        bot.BotLogs("SYSTEM", `\x1b[31mError Occurred: \x1b[37m"${error}" \x1b[31mfrom \x1b[37m"${path.basename(__filename)}"`);
        bot.BotLogs("SYSTEM", `\x1b[31m---------------------------------------------------------------`);
    }
};

exports.help = {
    name: 'perf',
    aliases: ['performance', 'cpu', 'ram', 'mem']
};