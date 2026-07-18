const { createAudioPlayer, createAudioResource, joinVoiceChannel, getVoiceConnection } = require('@discordjs/voice');
const { EmbedBuilder } = require('discord.js');
const config = require('../config.json');
const fs = require("fs");
const path = require('path');

exports.run = async (bot, message, args) => {

    try {

        if (!message.member.permissions.has("MANAGE_SERVER")) {
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

        if (!fs.existsSync(`./database/variables/${message.guild.id}.json`)) {
            let server = {
                channel: {
                    tts: "",
                    fishing: ""
                },
            };
            fs.writeFileSync(`./database/variables/${message.guild.id}.json`, JSON.stringify(server, null, 2));
            bot.BotLogs("SYSTEM", `\x1b[35mCreated new guild's variables for \x1b[90m[\x1b[37m${message.guild.id}.json\x1b[90m]`);
        };

        const rawData = fs.readFileSync(`./database/variables/${message.guild.id}.json`);
        const jsonData = JSON.parse(rawData);

        const query = args[0].toLowerCase();

        if (!["tts", "fishing"].includes(query)) {
            const EventEmbed_Failed = new EmbedBuilder()
                .setTitle(`Error Occurred`)
                .setDescription(`Unknown Arguments!`)
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

        if (query === "tts") {

            jsonData.channel.tts = message.channel.id;
            fs.writeFileSync(`./database/variables/${message.guild.id}.json`, JSON.stringify(jsonData, null, 2));

            bot.BotLogs(message.guild.name, `\x1b[35m\x1b[90m[\x1b[37m${message.author.tag}\x1b[90m] \x1b[35mhas registered channel for TTS \x1b[90m[\x1b[37m${message.channel.name}\x1b[90m]`)

            const EventEmbed_Failed = new EmbedBuilder()
                .setTitle(`Action Completed!`)
                .setDescription(`This channel has been registered for **TTS**`)
                .setColor('#00FF00')
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

        if (query === "fishing") {

            jsonData.channel.fishing = message.channel.id;
            fs.writeFileSync(`./database/variables/${message.guild.id}.json`, JSON.stringify(jsonData, null, 2));

            bot.BotLogs(message.guild.name, `\x1b[35m\x1b[90m[\x1b[37m${message.author.tag}\x1b[90m] \x1b[35mhas registered channel for Fishing \x1b[90m[\x1b[37m${message.channel.name}\x1b[90m]`)

            const EventEmbed_Failed = new EmbedBuilder()
                .setTitle(`Action Completed!`)
                .setDescription(`This channel has been registered for **Fishing**`)
                .setColor('#00FF00')
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

    } catch (error) {
        bot.BotLogs("SYSTEM", `\x1b[31m---------------------------------------------------------------`);
        bot.BotLogs("SYSTEM", `\x1b[31mError Occurred: \x1b[37m"${error}" \x1b[31mfrom \x1b[37m"${path.basename(__filename)}"`);
        bot.BotLogs("SYSTEM", `\x1b[31m---------------------------------------------------------------`);
    }

};

exports.help = {
    name: 'set'
};