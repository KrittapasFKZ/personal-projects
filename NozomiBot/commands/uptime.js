//const config = require('../config.json');
const { EmbedBuilder } = require('discord.js');
const fs = require("fs");
const path = require('path');

exports.run = async (bot, message, args) => {

    function formatTime(milliseconds) {
        const totalSeconds = Math.floor(milliseconds / 1000);
        const days = Math.floor(totalSeconds / (24 * 60 * 60));
        const hours = Math.floor((totalSeconds % (24 * 60 * 60)) / (60 * 60));
        const minutes = Math.floor((totalSeconds % (60 * 60)) / 60);
        const seconds = totalSeconds % 60;

        return `${days} days, ${hours} hours, ${minutes} minutes, ${seconds} seconds`;
    };

    function config_read() {
        const rawData = fs.readFileSync('./config.json');
        return JSON.parse(rawData);
    };

    try {
        if (message.author.id == "605361556297089035") {

            config = config_read();

            let currentTime = new Date();
            let startTime = new Date(config.startTime)
            let timeDifference = currentTime - startTime;
            let uptime = formatTime(timeDifference);

            const Board = new EmbedBuilder()
                .setAuthor({ name: `NozomiBot Uptime`, iconURL: `${bot.user.displayAvatarURL()}` })
                .setThumbnail(bot.user.displayAvatarURL())
                .setColor('#FF00FF')
                .addFields( 
                    { name: `Online since`, value: `\`\`\`${config.online_since}\`\`\`` },
                    { name: `Last Backup`, value: `\`\`\`${config.last_backup}\`\`\`` },
                    { name: `Last Restart`, value: `\`\`\`${config.rt_time}\`\`\`` },
                    { name: `Restart due to`, value: `\`\`\`${config.rt_error}\`\`\`` },
                    { name: `Error Count`, value: `\`\`\`${config.rt_attempt} times since start\`\`\`` },
                    { name: `Uptime`, value: `\`\`\`${uptime}\`\`\`` },
                )

            /*
        bot.BotLogs(message.guild.name, `\x1b[34m---------------------------------------------------------------`);
        bot.BotLogs(message.guild.name, `\x1b[34mOnline since: \x1b[37m${config.online_since}`);
        bot.BotLogs(message.guild.name, `\x1b[34mLast Backup: \x1b[37m${config.last_backup}`);
        bot.BotLogs(message.guild.name, `\x1b[34mLast Restart: \x1b[37m${config.rt_time}`);
        bot.BotLogs(message.guild.name, `\x1b[34mRestart due to: \x1b[37m"${config.rt_error}"`);
        bot.BotLogs(message.guild.name, `\x1b[34mUptime: \x1b[37m${uptime}`);
        bot.BotLogs(message.guild.name, `\x1b[34m---------------------------------------------------------------`);
            */
           
            return message.reply({ embeds: [Board] })
                .then(msg => {
                    setTimeout(() => {
                        if (msg) {
                            msg.delete().catch(console.error);
                            message.delete()
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
        };
    } catch (error) {
        bot.BotLogs("SYSTEM", `\x1b[31m---------------------------------------------------------------`);
        bot.BotLogs("SYSTEM", `\x1b[31mError Occurred: \x1b[37m"${error}" \x1b[31mfrom \x1b[37m"${path.basename(__filename)}"`);
        bot.BotLogs("SYSTEM", `\x1b[31m---------------------------------------------------------------`);
    }
};

exports.help = {
    name: 'uptime',
    aliases: ['ut', 'up']
};