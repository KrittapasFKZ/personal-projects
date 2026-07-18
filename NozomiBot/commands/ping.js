const config = require('../config.json');
const { EmbedBuilder } = require('discord.js');
const fs = require("fs");
const path = require('path');

exports.run = async (bot, message, args) => {
    try {
        if (message.author.id == "605361556297089035") {

            const MainEmbed = await message.channel.send("🏓 Measuring latency... (5s test)");

            let samples = [];
            let apiSamples = [];

            // Take latency samples every second for 5 seconds
            for (let i = 0; i < 5; i++) {
                const start = Date.now();
                await new Promise(res => setTimeout(res, 1000)); // wait 1s
                const end = Date.now();

                const botLatency = end - start; // local latency calc
                samples.push(botLatency);

                const apiLatency = Math.round(bot.ws.ping);
                apiSamples.push(apiLatency);
            }

            // Calculate min, max, avg
            const min = Math.min(...samples);
            const max = Math.max(...samples);
            const avg = Math.round(samples.reduce((a, b) => a + b, 0) / samples.length);

            const apiMin = Math.min(...apiSamples);
            const apiMax = Math.max(...apiSamples);
            const apiAvg = Math.round(apiSamples.reduce((a, b) => a + b, 0) / apiSamples.length);

            const Board = new EmbedBuilder()
                .setAuthor({ name: `NozomiBot Latency`, iconURL: `${bot.user.displayAvatarURL()}` })
                .setThumbnail(bot.user.displayAvatarURL())
                .setColor('#FF00FF')
                .addFields(
                    { name: `:green_circle: Bot Latency`, value: `\`\`\`${MainEmbed.createdTimestamp - message.createdTimestamp}ms\`\`\`` },
                    { name: `:green_circle: API Latency`, value: `\`\`\`${apiMin}ms - ${apiMax}ms (${apiAvg}ms)\`\`\`` },
                );

            return await MainEmbed.edit({ content: "", embeds: [Board] })
                .then(msg => {
                    setTimeout(() => {
                        if (msg) {
                            msg.delete().catch(console.error);
                            message.delete().catch(console.error);
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
                            message.delete().catch(console.error);
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
    name: 'ping',
    aliases: ['latency']
};