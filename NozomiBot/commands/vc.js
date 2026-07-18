const { createAudioPlayer, createAudioResource, joinVoiceChannel, getVoiceConnection } = require('@discordjs/voice');
const { EmbedBuilder } = require('discord.js');
const config = require('../config.json');
const path = require('path');
const fs = require("fs");

let current_mute = false

exports.run = async (bot, message, args) => {
    let now = new Date(Date.now());
    let now_hours = now.getHours().toString().padStart(2, '0');
    let now_mins = now.getMinutes().toString().padStart(2, '0');
    let now_seconds = now.getSeconds().toString().padStart(2, '0');

    try {

        const EventEmbed_Failed = new EmbedBuilder()
            .setTitle(`Command Usage`)
            .setDescription(`\`!vc <move/pull/kick>\``)
            .setColor('#FFFF00')
            .setTimestamp()

        if (!args[0]) return message.reply({ embeds: [EventEmbed_Failed] })
            .then(msg => {
                setTimeout(() => {
                    if (msg) {
                        msg.delete().catch(console.error);
                        message.delete();
                    }
                }, 5000);
            })
            .catch(console.error);

        if (args[0] == "move" || args[0] == "moveall") {

            const EventEmbed_Failed = new EmbedBuilder()
                .setTitle(`Command Usage`)
                .setDescription(`\`!vc move <channel_id>\``)
                .setColor('#FFFF00')
                .setTimestamp()

            let channelID

            if (!args[1]) return message.reply({ embeds: [EventEmbed_Failed] })
                .then(msg => {
                    setTimeout(() => {
                        if (msg) {
                            msg.delete().catch(console.error);
                            message.delete();
                        }
                    }, 5000);
                })
                .catch(console.error);

            channelID = args[1];
            const voiceChannel_new = message.guild.channels.cache.get(channelID);
            const voiceChannel_old = message.guild.channels.cache.get(message.member.voice.channel.id);

            if (!voiceChannel_new || !voiceChannel_old) return message.reply({ embeds: [EventEmbed_Failed] })
                .then(msg => {
                    setTimeout(() => {
                        if (msg) {
                            msg.delete().catch(console.error);
                            message.delete();
                        }
                    }, 5000);
                })
                .catch(console.error);

            message.member.voice.channel.members.forEach(m => {
                m.voice.setChannel(channelID)
                    .catch(err => { })
            });

            bot.BotLogs(message.guild.name, `\x1b[90m[\x1b[37m${message.author.tag}\x1b[90m] \x1b[35mmoving members from \x1b[90m[\x1b[37m${voiceChannel_old.name}\x1b[90m] \x1b[35mto \x1b[90m[\x1b[37m${voiceChannel_new.name}\x1b[90m]\x1b[35m!`)

            const EventEmbed_Completed = new EmbedBuilder()
                .setTitle(`Interaction Completed!`)
                .setDescription(`Moving **${voiceChannel_old.members.size} users** from **${voiceChannel_old.name}** to **${voiceChannel_new.name}**!`)
                .setColor('#00FF00')
                .setTimestamp()

            return message.reply({ embeds: [EventEmbed_Completed] })
                .then(msg => {
                    setTimeout(() => {
                        if (msg) {
                            msg.delete().catch(console.error);
                            message.delete();
                        }
                    }, 5000);
                })
                .catch(console.error);
        };

        if (args[0] == "pull" || args[0] == "pullall") {

            const EventEmbed_Failed = new EmbedBuilder()
                .setTitle(`Command Usage`)
                .setDescription(`\`!vc pull <channel_id>\``)
                .setColor('#FFFF00')
                .setTimestamp()

            let channelID

            if (!args[1]) return message.reply({ embeds: [EventEmbed_Failed] })
                .then(msg => {
                    setTimeout(() => {
                        if (msg) {
                            msg.delete().catch(console.error);
                            message.delete();
                        }
                    }, 5000);
                })
                .catch(console.error);

            channelID = args[1];
            const voiceChannel_new = message.guild.channels.cache.get(channelID);
            const voiceChannel_old = message.guild.channels.cache.get(message.member.voice.channel.id);

            if (!voiceChannel_new || !voiceChannel_old) return message.reply({ embeds: [EventEmbed_Failed] })
                .then(msg => {
                    setTimeout(() => {
                        if (msg) {
                            msg.delete().catch(console.error);
                            message.delete();
                        }
                    }, 5000);
                })
                .catch(console.error);

            voiceChannel_new.members.forEach(m => {
                m.voice.setChannel(message.member.voice.channel.id)
                    .catch(err => { })
            });

            bot.BotLogs(message.guild.name, `\x1b[90m[\x1b[37m${message.author.tag}\x1b[90m] \x1b[35mpulling members from \x1b[90m[\x1b[37m${voiceChannel_new.name}\x1b[90m] \x1b[35mto \x1b[90m[\x1b[37m${voiceChannel_old.name}\x1b[90m]\x1b[35m!`)

            const EventEmbed_Completed = new EmbedBuilder()
                .setTitle(`Interaction Completed!`)
                .setDescription(`Pulling **${voiceChannel_new.members.size} users** from **${voiceChannel_new.name}** to **${voiceChannel_old.name}**!`)
                .setColor('#00FF00')
                .setTimestamp()

            return message.reply({ embeds: [EventEmbed_Completed] })
                .then(msg => {
                    setTimeout(() => {
                        if (msg) {
                            msg.delete().catch(console.error);
                            message.delete();
                        }
                    }, 5000);
                })
                .catch(console.error);
        };

        if (args[0] == "kick" || args[0] == "kickall") {

            const EventEmbed_Failed = new EmbedBuilder()
                .setTitle(`Command Usage`)
                .setDescription(`\`!vc kick <channel_id>\``)
                .setColor('#FFFF00')
                .setTimestamp()

            let channelID

            if (!args[1]) {
                channelID = message.member.voice.channel.id
            } else {
                channelID = args[1];
            };

            const voiceChannel_new = message.guild.channels.cache.get(channelID);

            if (!voiceChannel_new) return message.reply({ embeds: [EventEmbed_Failed] })
                .then(msg => {
                    setTimeout(() => {
                        if (msg) {
                            msg.delete().catch(console.error);
                            message.delete();
                        }
                    }, 5000);
                })
                .catch(console.error);

            voiceChannel_new.members.forEach(m => {
                m.voice.setChannel(null)
                    .catch(err => { })
            });

            bot.BotLogs(message.guild.name, `\x1b[90m[\x1b[37m${message.author.tag}\x1b[90m] \x1b[35mkicking members from \x1b[90m[\x1b[37m${voiceChannel_new.name}\x1b[90m]\x1b[35m!`)

            const EventEmbed_Completed = new EmbedBuilder()
                .setTitle(`Interaction Completed!`)
                .setDescription(`Kicking **${voiceChannel_new.members.size} users** from **${voiceChannel_new.name}**!`)
                .setColor('#00FF00')
                .setTimestamp()

            return message.reply({ embeds: [EventEmbed_Completed] })
                .then(msg => {
                    setTimeout(() => {
                        if (msg) {
                            msg.delete().catch(console.error);
                            message.delete();
                        }
                    }, 5000);
                })
                .catch(console.error);
        };

        if (args[0] == "mute") {

            const EventEmbed_Failed = new EmbedBuilder()
                .setTitle(`Command Usage`)
                .setDescription(`\`!vc mute <channel_id>\``)
                .setColor('#FFFF00')
                .setTimestamp()

            let channelID

            if (!args[1]) {
                channelID = message.member.voice.channel.id
            } else {
                channelID = args[1];
            };

            const voiceChannel_new = message.guild.channels.cache.get(channelID);

            if (!voiceChannel_new) return message.reply({ embeds: [EventEmbed_Failed] })
                .then(msg => {
                    setTimeout(() => {
                        if (msg) {
                            msg.delete().catch(console.error);
                            message.delete();
                        }
                    }, 5000);
                })
                .catch(console.error);

            const EventEmbed_Completed = new EmbedBuilder()
                .setTitle(`Interaction Completed!`)
                .setColor('#00FF00')
                .setTimestamp()

            if (current_mute == false) {

                voiceChannel_new.members.forEach(m => {
                    if (m.user.id == "887531368836370483") return
                    m.voice.setMute(true)
                        .catch(err => { })
                });

                current_mute = true

                bot.BotLogs(message.guild.name, `\x1b[90m[\x1b[37m${message.author.tag}\x1b[90m] \x1b[35mmuted members from \x1b[90m[\x1b[37m${voiceChannel_new.name}\x1b[90m]\x1b[35m!`)

                EventEmbed_Completed.setDescription(`Muted **${voiceChannel_new.members.size} users** from **${voiceChannel_new.name}**!`);

            } else {

                voiceChannel_new.members.forEach(m => {
                    if (m.user.id == "887531368836370483") return
                    m.voice.setMute(false)
                        .catch(err => { })
                });

                current_mute = false

                bot.BotLogs(message.guild.name, `\x1b[90m[\x1b[37m${message.author.tag}\x1b[90m] \x1b[35munmuted members from \x1b[90m[\x1b[37m${voiceChannel_new.name}\x1b[90m]\x1b[35m!`)

                EventEmbed_Completed.setDescription(`Unmuted **${voiceChannel_new.members.size} users** from **${voiceChannel_new.name}**!`);

            };

            return message.reply({ embeds: [EventEmbed_Completed] })
                .then(msg => {
                    setTimeout(() => {
                        if (msg) {
                            msg.delete().catch(console.error);
                            message.delete();
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
    name: 'vc',
    aliases: ['voicechannel', 'voicechat', 'voice']
};