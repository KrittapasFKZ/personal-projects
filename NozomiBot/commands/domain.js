const { createAudioPlayer, createAudioResource, joinVoiceChannel, getVoiceConnection } = require('@discordjs/voice');
const { EmbedBuilder } = require('discord.js');
const config = require('../config.json');
const fs = require("fs");
const path = require('path');

let checkInterval

exports.run = async (bot, message, args) => {

    try {

        if (config.domain_expanded) {
            return message.reply(`ตอนนี้ยังมีอาณาเขตกางอยู่!`)
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
            return message.reply(`You need to be in VC!`)
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

        if (!args[0]) {
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

        if (query == "gojo") {
            const tempFilePath = './sounds/domain_gojo.mp3';

            const connection = joinVoiceChannel({
                channelId: voiceChannel.id,
                guildId: message.guild.id,
                adapterCreator: message.guild.voiceAdapterCreator,
            });

            config.domain_expanded = true
            config.domain_owner = message.author.id
            fs.writeFileSync('../config.json', JSON.stringify(config, null, 4));

            bot.BotLogs(message.guild.name, `\x1b[35mDomain Expansion from \x1b[90m[\x1b[37m${message.author.tag}\x1b[90m]`)

            let owner = `${message.author.tag}'s Infinity Void Domain`
            setTimeout(() => {
                const audioPlayer = createAudioPlayer();
                const resource = createAudioResource(tempFilePath, {
                    inlineVolume: true
                });
                resource.volume?.setVolume(0.75);
                audioPlayer.play(resource);
                connection.subscribe(audioPlayer);
            }, 500);

            const CreateEmbed = new EmbedBuilder()
                .setAuthor({ name: `${owner}`, iconURL: `${message.author.displayAvatarURL()}` })
                .setImage(`https://i.pinimg.com/originals/14/fc/7d/14fc7d1120735dd8e2064a38913ea339.gif`)

            const main_embed = await message.channel.send({ embeds: [CreateEmbed] })

            async function Domain_Collapse(msg) {
                clearInterval(checkInterval);
                msg.delete()
                config.domain_expanded = false
                config.domain_owner = ""
                voiceChannel.members.forEach(m => {
                    if (m.user.id == "887531368836370483" || m.user.id == message.author.id) return
                    if (m.voice.serverMute) {
                        m.voice.setMute(false)
                            .catch(err => { })
                    };
                });
                fs.writeFileSync('../config.json', JSON.stringify(config, null, 4));
            };

            setTimeout(async () => {
                const EditEmbed = new EmbedBuilder()
                    .setAuthor({ name: `${owner}`, iconURL: `${message.author.displayAvatarURL()}` })
                    .setImage(`https://i.pinimg.com/736x/25/1f/49/251f49b9061e3ef0b3a862135258f151.jpg`)

                main_embed.edit({ embeds: [EditEmbed] })

                checkInterval = setInterval(async () => {

                    if (voiceChannel.members.get(message.author.id)) {
                        voiceChannel.members.forEach(m => {
                            if (m.user.id == "887531368836370483" || m.user.id == message.author.id) return
                            if (m.voice.serverMute) return
                            m.voice.setMute(true)
                                .catch(err => { })
                        });
                    } else {
                        Domain_Collapse(main_embed);
                    }

                }, 1000)

                await main_embed.react('❌');

                const filter = (reaction, user) => {
                    return user.id === message.author.id;
                };

                const collector = main_embed.createReactionCollector({
                    filter
                });

                collector.on('collect', async (reaction, user) => {
                    switch (reaction.emoji.name) {
                        case "❌":
                            if (user.tag === message.author.tag) {
                                Domain_Collapse(main_embed);
                                break;
                            };
                    }
                });

            }, 6900);
        };

        if (query == "sukuna") {
            const tempFilePath = './sounds/domain_sukuna.mp3';

            const connection = joinVoiceChannel({
                channelId: voiceChannel.id,
                guildId: message.guild.id,
                adapterCreator: message.guild.voiceAdapterCreator,
            });

            config.domain_expanded = true
            config.domain_owner = message.author.id
            fs.writeFileSync('../config.json', JSON.stringify(config, null, 4));

            bot.BotLogs(message.guild.name, `\x1b[35mDomain Expansion from \x1b[90m[\x1b[37m${message.author.tag}\x1b[90m]`)

            let owner = `${message.author.tag}'s Malevolent Shrine`
            setTimeout(() => {
                const audioPlayer = createAudioPlayer();
                const resource = createAudioResource(tempFilePath, {
                    inlineVolume: true
                });
                resource.volume?.setVolume(0.25);
                audioPlayer.play(resource);
                connection.subscribe(audioPlayer);
            }, 300);

            const CreateEmbed = new EmbedBuilder()
                .setAuthor({ name: `${owner}`, iconURL: `${message.author.displayAvatarURL()}` })
                .setImage(`https://giffiles.alphacoders.com/221/221618.gif`)

            const main_embed = await message.channel.send({ embeds: [CreateEmbed] })

            async function Domain_Collapse(msg) {
                msg.delete()
                config.domain_expanded = false
                config.domain_owner = ""
                fs.writeFileSync('../config.json', JSON.stringify(config, null, 4));
            };

            setTimeout(async () => {
                const EditEmbed = new EmbedBuilder()
                    .setAuthor({ name: `${owner}`, iconURL: `${message.author.displayAvatarURL()}` })
                    .setImage(`https://i.redd.it/i278jvdyli471.png`)

                main_embed.edit({ embeds: [EditEmbed] })

                setTimeout(async () => {
                    const members = voiceChannel.members.filter(member => !member.user.bot && member.id !== message.author.id);
                    const selectedMembers = members.random(members.size);
                    selectedMembers.forEach(member => {
                        member.voice.setChannel(message.guild.afkChannelId)
                            .catch(err => { })
                    });
                    selectedMembers.forEach(member => {
                        member.voice.setChannel(voiceChannel.id)
                            .catch(err => { })
                    });
                }, 2000);

                Domain_Collapse(main_embed);

            }, 11900);
        };

    } catch (error) {
        bot.BotLogs("SYSTEM", `\x1b[31m---------------------------------------------------------------`);
        bot.BotLogs("SYSTEM", `\x1b[31mError Occurred: \x1b[37m"${error}" \x1b[31mfrom \x1b[37m"${path.basename(__filename)}"`);
        bot.BotLogs("SYSTEM", `\x1b[31m---------------------------------------------------------------`);
    }

};

exports.help = {
    name: 'domain',
    aliases: ['ryouikitenkai', 'domainexpansion', 'domain_expansion']
};