const { createAudioPlayer, createAudioResource, joinVoiceChannel, getVoiceConnection } = require('@discordjs/voice');
const { EmbedBuilder } = require('discord.js');
const config = require('../config.json');
const fs = require("fs");
const path = require('path');

let checkInterval

exports.run = async (bot, message, args) => {

    try {

        const botMember = message.guild.members.cache.get("887531368836370483");
        const voiceChannel = botMember.voice.channel;

        if (!voiceChannel) {
            return message.reply(`No VC!`)
                .then(msg => {
                    setTimeout(() => {
                        if (msg) {
                            msg.delete().catch(console.error);
                            message.delete()
                        }
                    }, 3500);
                })
                .catch(console.error);
        };

        if (!voiceChannel.members.get(message.author.id)) {
            const EventEmbed_Failed = new EmbedBuilder()
                .setTitle(`Error Occurred`)
                .setDescription(`No Active Voice Channel!`)
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
                .setDescription(` `)
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

        if (!["gojo", "sukuna"].includes(query)) {
            const EventEmbed_Failed = new EmbedBuilder()
                .setTitle(`Error Occurred`)
                .setDescription(`Unknown Domain! \`gojo, sukuna\``)
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

        message.delete();

    } catch (error) {
        bot.BotLogs("SYSTEM", `\x1b[31m---------------------------------------------------------------`);
        bot.BotLogs("SYSTEM", `\x1b[31mError Occurred: \x1b[37m"${error}" \x1b[31mfrom \x1b[37m"${path.basename(__filename)}"`);
        bot.BotLogs("SYSTEM", `\x1b[31m---------------------------------------------------------------`);
    }

};

exports.help = {
    name: 'sound',
    aliases: ['track', 'vfx', 'sfx']
};