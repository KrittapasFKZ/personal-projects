const { createAudioPlayer, createAudioResource, joinVoiceChannel, getVoiceConnection } = require('@discordjs/voice');
const { EmbedBuilder } = require('discord.js');
const config = require('../config.json');
const fs = require("fs");
const path = require('path');

const { main_queue, addToQueue, getAudioQueue } = require('../audio_queue.js');
const bot_functions = require('../bot_functions.js');
 
exports.run = async (bot, message, args) => {

    try {

        if (message.author.id == "605361556297089035") { } else {
            const EventEmbed_Failed = new EmbedBuilder()
                .setTitle(`Access Denied`)
                .setDescription(`You don't have permission to use this.`)
                .setColor('#FF0000')
                .setTimestamp()

            return message.reply({ embeds: [EventEmbed_Failed] })
                .then(msg => {
                    setTimeout(() => {
                        if (msg) {
                            msg.delete().catch(console.error);
                            message.delete()
                        }
                    }, 5000);
                })
                .catch(console.error);
        };

        if (!args[0]) {
            const EventEmbed_Failed = new EmbedBuilder()
                .setTitle(`Error Occurred`)
                .setDescription(`Missing Arguments!`)
                .setColor('#FF0000')
                .setTimestamp()

            return message.reply({ embeds: [EventEmbed_Failed] })
                .then(msg => {
                    setTimeout(() => {
                        if (msg) {
                            msg.delete().catch(console.error);
                            message.delete()
                        }
                    }, 5000);
                })
                .catch(console.error);
        };
 
        const query = args[0].toLowerCase();

        if (!["status"].includes(query)) return

        if (query === "status") {
            message.delete();
            bot_functions.getServerStatus(bot);
        };

    } catch (error) {
        console.log(error)
        bot.BotLogs("SYSTEM", `\x1b[31m---------------------------------------------------------------`);
        bot.BotLogs("SYSTEM", `\x1b[31mError Occurred: \x1b[37m"${error}" \x1b[31mfrom \x1b[37m"${path.basename(__filename)}"`);
        bot.BotLogs("SYSTEM", `\x1b[31m---------------------------------------------------------------`);
    }

};

exports.help = {
    name: 'func',
    aliases: ['function']
};