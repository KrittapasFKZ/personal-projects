const { EmbedBuilder } = require('discord.js');
const config = require('../config.json');
const { main_queue } = require('../audio_queue.js');
const fs = require("fs");
const path = require('path');

exports.run = async (bot, message, args) => {
    try {
        if (message.author.id == "605361556297089035") {
            let target_event = args[0]
            target_event = target_event.toLowerCase()

            if (target_event == "tts") {
                if (config.tts_status) {
                    config.tts_status = false
                    fs.writeFileSync('../config.json', JSON.stringify(config, null, 4));
                    bot.BotLogs("SYSTEM", `\x1b[36m\x1b[37mTTS Command \x1b[36mhas been \x1b[31mDisabled\x1b[36m!`);
                    const Embed = new EmbedBuilder()
                        .setTitle(`Action Completed!`)
                        .setDescription(`❌ **TTS Command** has been **Disabled!**`)
                        .setColor('#FF0000')
                        .setTimestamp()
                    return message.reply({ embeds: [Embed] })
                        .then(msg => {
                            setTimeout(() => {
                                if (msg) {
                                    msg.delete().catch(console.error);
                                    message.delete()
                                }
                            }, 7000);
                        })
                        .catch(console.error);
                } else {
                    config.tts_status = true
                    fs.writeFileSync('../config.json', JSON.stringify(config, null, 4));
                    bot.BotLogs("SYSTEM", `\x1b[36m\x1b[37mTTS Command \x1b[36mhas been \x1b[32mEnabled\x1b[36m!`);
                    const Embed = new EmbedBuilder()
                        .setTitle(`Action Completed!`)
                        .setDescription(`✅ **TTS Command** has been **Enabled!**`)
                        .setColor('#00FF00')
                        .setTimestamp()
                    return message.reply({ embeds: [Embed] })
                        .then(msg => {
                            setTimeout(() => {
                                if (msg) {
                                    msg.delete().catch(console.error);
                                    message.delete()
                                }
                            }, 7000);
                        })
                        .catch(console.error);
                }
            } else {
                if (target_event == "fishing") {
                    if (config.fishing_status) {
                        config.fishing_status = false
                        fs.writeFileSync('../config.json', JSON.stringify(config, null, 4));
                        bot.BotLogs("SYSTEM", `\x1b[36m\x1b[37mFishing Simulator \x1b[36mhas been \x1b[31mDisabled\x1b[36m!`);
                        const Embed = new EmbedBuilder()
                            .setTitle(`Action Completed!`)
                            .setDescription(`❌ **Fishing Simulator** has been **Disabled!**`)
                            .setColor('#FF0000')
                            .setTimestamp()
                        return message.reply({ embeds: [Embed] })
                            .then(msg => {
                                setTimeout(() => {
                                    if (msg) {
                                        msg.delete().catch(console.error);
                                        message.delete()
                                    }
                                }, 7000);
                            })
                            .catch(console.error);
                    } else {
                        config.fishing_status = true
                        fs.writeFileSync('../config.json', JSON.stringify(config, null, 4));
                        bot.BotLogs("SYSTEM", `\x1b[36m\x1b[37mFishing Simulator \x1b[36mhas been \x1b[32mEnabled\x1b[36m!`);
                        const Embed = new EmbedBuilder()
                            .setTitle(`Action Completed!`)
                            .setDescription(`✅ **Fishing Simulator** has been **Enabled!**`)
                            .setColor('#00FF00')
                            .setTimestamp()
                        return message.reply({ embeds: [Embed] })
                            .then(msg => {
                                setTimeout(() => {
                                    if (msg) {
                                        msg.delete().catch(console.error);
                                        message.delete()
                                    }
                                }, 7000);
                            })
                            .catch(console.error);
                    }
                } else {
                    if (target_event == "online") {
                        if (config.online_ping) {
                            config.online_ping = false
                            fs.writeFileSync('../config.json', JSON.stringify(config, null, 4));
                            bot.BotLogs("SYSTEM", `\x1b[36m\x1b[37mOnline Ping \x1b[36mhas been \x1b[31mDisabled\x1b[36m!`);
                            const Embed = new EmbedBuilder()
                                .setTitle(`Action Completed!`)
                                .setDescription(`❌ **Online Ping** has been **Disabled!**`)
                                .setColor('#FF0000')
                                .setTimestamp()
                            return message.reply({ embeds: [Embed] })
                                .then(msg => {
                                    setTimeout(() => {
                                        if (msg) {
                                            msg.delete().catch(console.error);
                                            message.delete()
                                        }
                                    }, 7000);
                                })
                                .catch(console.error);
                        } else {
                            config.online_ping = true
                            fs.writeFileSync('../config.json', JSON.stringify(config, null, 4));
                            bot.BotLogs("SYSTEM", `\x1b[36m\x1b[37mOnline Ping \x1b[36mhas been \x1b[32mEnabled\x1b[36m!`);
                            const Embed = new EmbedBuilder()
                                .setTitle(`Action Completed!`)
                                .setDescription(`✅ **Online Ping** has been **Enabled!**`)
                                .setColor('#00FF00')
                                .setTimestamp()
                            return message.reply({ embeds: [Embed] })
                                .then(msg => {
                                    setTimeout(() => {
                                        if (msg) {
                                            msg.delete().catch(console.error);
                                            message.delete()
                                        }
                                    }, 7000);
                                })
                                .catch(console.error);
                        }
                    } else {
                        if (target_event == "audio") {
                            if (config.audio_queue_logs) {
                                config.audio_queue_logs = false
                                fs.writeFileSync('../config.json', JSON.stringify(config, null, 4));
                                bot.BotLogs("SYSTEM", `\x1b[36m\x1b[37mAudio Queue Logs \x1b[36mhas been \x1b[31mDisabled\x1b[36m!`);
                                const Embed = new EmbedBuilder()
                                    .setTitle(`Action Completed!`)
                                    .setDescription(`❌ **Audio Queue Logs** has been **Disabled!**`)
                                    .setColor('#FF0000')
                                    .setTimestamp()
                                return message.reply({ embeds: [Embed] })
                                    .then(msg => {
                                        setTimeout(() => {
                                            if (msg) {
                                                msg.delete().catch(console.error);
                                                message.delete()
                                            }
                                        }, 7000);
                                    })
                                    .catch(console.error);
                            } else {
                                config.audio_queue_logs = true
                                fs.writeFileSync('../config.json', JSON.stringify(config, null, 4));
                                bot.BotLogs("SYSTEM", `\x1b[36m\x1b[37mAudio Queue Logs \x1b[36mhas been \x1b[32mEnabled\x1b[36m!`);
                                const Embed = new EmbedBuilder()
                                    .setTitle(`Action Completed!`)
                                    .setDescription(`✅ **Audio Queue Logs** has been **Enabled!**`)
                                    .setColor('#00FF00')
                                    .setTimestamp()
                                return message.reply({ embeds: [Embed] })
                                    .then(msg => {
                                        setTimeout(() => {
                                            if (msg) {
                                                msg.delete().catch(console.error);
                                                message.delete()
                                            }
                                        }, 7000);
                                    })
                                    .catch(console.error);
                            }
                        } else {
                            if (target_event == "queue") {
                                const Embed = new EmbedBuilder()
                                    .setTitle(`Action Completed!`)
                                    .setDescription(`✅ Showing all audio queue...`)
                                    .setColor('#00FF00')
                                    .setTimestamp()
                                let Queue = main_queue.get(message.guild.id);
                                for (let i = 0; i < Queue.length; i++) {
                                    bot.BotLogs("SYSTEM", `${Queue[i].name}(#${Queue[i].uuid}) - ${Queue[i].sender}`);
                                    Embed.addFields(
                                        { name: `${Queue[i].name}(#${Queue[i].uuid})`, value: `By ${Queue[i].sender} ` },
                                    )
                                };
                                return message.reply({ embeds: [Embed] })
                                    .then(msg => {
                                        setTimeout(() => {
                                            if (msg) {
                                                msg.delete().catch(console.error);
                                                message.delete()
                                            }
                                        }, 7000);
                                    })
                                    .catch(console.error);
                            } else {

                            };
                        };
                    };
                };
            };

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
    name: 'event',
    aliases: ['func', 'ev']
};