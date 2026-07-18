const { EmbedBuilder } = require('discord.js');
const config = require('../config.json');
const path = require('path');
const fs = require("fs");

exports.run = async (bot, message, args) => {
    let now = new Date(Date.now());
    let now_hours = now.getHours().toString().padStart(2, '0');
    let now_mins = now.getMinutes().toString().padStart(2, '0');
    let now_seconds = now.getSeconds().toString().padStart(2, '0');

    try {

        const TestEmbed = new EmbedBuilder()
            .setAuthor({ name: `MeguBot Commands`, iconURL: `${bot.user.displayAvatarURL()}` })
            .setThumbnail(bot.user.displayAvatarURL())
            .setDescription("This is all of available commands.")
            .setColor('#FF00FF')
            .addFields(
                { name: 'Prefix', value: `\`!\`` },
                { name: 'Voice', value: "`join`, `say`, `jp`, `en`, `cn`" },
                { name: 'Social', value: "`user`, `top`, `nick`" },
                { name: 'Fun', value: "`fish`, `farm`, `dungeon`, `sell`, `donate`, `shop`, `market`, `murasaki`, `domain`, `wolf`, `momoi`, `phone`, `viktor`, `lingagu`, `uiiaiu`" })
            .setTimestamp()

        return message.reply({ embeds: [TestEmbed] })
            .then(msg => {
                setTimeout(() => {
                    if (msg) {
                        msg.delete().catch(console.error);
                        message.delete();
                    }
                }, 15000);
            })
            .catch(console.error);

    } catch (error) {
        bot.BotLogs("SYSTEM", `\x1b[31m---------------------------------------------------------------`);
        bot.BotLogs("SYSTEM", `\x1b[31mError Occurred: \x1b[37m"${error}" \x1b[31mfrom \x1b[37m"${path.basename(__filename)}"`);
        bot.BotLogs("SYSTEM", `\x1b[31m---------------------------------------------------------------`);
    }

};

exports.help = {
    name: 'help',
    aliases: ['helps', 'commands', 'command']
};