const { createAudioPlayer, createAudioResource, joinVoiceChannel, getVoiceConnection } = require('@discordjs/voice');
const config = require('../config.json');
const fs = require("fs");
const { EmbedBuilder } = require('discord.js');
const path = require('path');
const { exec } = require('child_process');

const { main_queue, addToQueue, getAudioQueue } = require('../audio_queue.js');

const cooldowns = new Map();
let cooldownTimeInSeconds = config.tts_cd

exports.run = async (bot, message, args) => {
    let now = new Date(Date.now());
    let now_hours = now.getHours().toString().padStart(2, '0');
    let now_mins = now.getMinutes().toString().padStart(2, '0');
    let now_seconds = now.getSeconds().toString().padStart(2, '0');

    function generateUUID() {
        const characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
        let uuid = '';

        for (let i = 0; i < 10; i++) {
            const randomIndex = Math.floor(Math.random() * characters.length);
            uuid += characters[randomIndex];
        }

        return uuid;
    }

    try {

        const botMember = message.guild.members.cache.get("887531368836370483");
        const voiceChannel = botMember.voice.channel;

        if (!voiceChannel) {
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
                .setTitle(`Command Usage`)
                .setDescription(`\`!yt <youtube_url>\``)
                .setColor('#FFFF00')
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

        let yt_url = args[0]

        let Total_Queue = main_queue.get(message.guild.id).length
        const Board = new EmbedBuilder()
        if (Total_Queue <= 0 && getAudioQueue(message.author.id) == "IDLE") {
            Board.setAuthor({ name: `NozomiBot - Audio Queue`, iconURL: `${bot.user.displayAvatarURL()}` })
                .setDescription(`🎵 Downloading Youtube Video - ${yt_url}`)
                .setColor('#F5B041')
        } else {
            Board.setAuthor({ name: `NozomiBot - Audio Queue`, iconURL: `${bot.user.displayAvatarURL()}` })
                .setDescription(`Your request has been queued! (total queue: ${Total_Queue})`)
                .setColor('#F5B041')
        };
        message.reply({ embeds: [Board] })
            .then(msg => {
                setTimeout(() => {
                    if (msg) {
                        msg.delete().catch(console.error);
                        message.delete()
                    }
                }, 7500);
            })
            .catch(console.error);

        cooldowns.set(message.author.id, Date.now() + cooldownTimeInSeconds * 1000);

        const connection = joinVoiceChannel({
            channelId: voiceChannel.id,
            guildId: message.guild.id,
            adapterCreator: message.guild.voiceAdapterCreator,
        });

        if (config.audio_queue_logs == false) {
            bot.BotLogs(message.guild.name, `\x1b[35m\x1b[90m[\x1b[37m${message.author.tag}\x1b[90m] \x1b[35mused Youtube Player for \x1b[90m[\x1b[37m${yt_url}\x1b[90m]`);
        };

        let uuid = generateUUID();
        let queue_constructor = {
            uuid: `${uuid}`,
            file: `${uuid}.mp3`,
            name: `${yt_url}`,
            type: "YOUTUBE",
            guild: message.guild,
            sender: message.author,
            voice_channel: voiceChannel,
            connection: connection
        };

        exec(`python3 yt_download.py ${yt_url} ${uuid}`, (error, stdout, stderr) => {
            if (error) {
                return;
            };
            addToQueue(message.guild.id, queue_constructor);
        });

    } catch (error) {
        bot.BotLogs("SYSTEM", `\x1b[31m---------------------------------------------------------------`);
        bot.BotLogs("SYSTEM", `\x1b[31mError Occurred: \x1b[37m"${error}" \x1b[31mfrom \x1b[37m"${path.basename(__filename)}"`);
        bot.BotLogs("SYSTEM", `\x1b[31m---------------------------------------------------------------`);
    }
};

exports.help = {
    name: 'yt',
    aliases: ['music', 'song']
};