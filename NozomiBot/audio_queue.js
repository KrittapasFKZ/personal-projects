const { createAudioPlayer, createAudioResource, joinVoiceChannel, getVoiceConnection } = require('@discordjs/voice');
const config = require('./config.json');
const fs = require("fs");
const path = require('path');
const { EmbedBuilder } = require('discord.js');
const EventEmitter = require('events');
const googleTTS = require("google-tts-api");

function limitString(str) {
    return str.substring(0, 10).padEnd(10, ' ');
};

function generateUUID() {
    const characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    let uuid = '';

    for (let i = 0; i < 10; i++) {
        const randomIndex = Math.floor(Math.random() * characters.length);
        uuid += characters[randomIndex];
    };

    return uuid;
};

////////////////////////////////////////////////
///////////////////// Main /////////////////////
//////////////////////////////////////////////// 

const main_queue = new Map();
const victims = new Map();

class QueueEmitter extends EventEmitter { }
const queueEmitter = new QueueEmitter();
const players = new Map();

const queue_constructor = [];

main_queue.set("Main", queue_constructor);

const ConsoleLog = (host, msg) => {
    if (config.audio_queue_logs == false) return
    let now = new Date(Date.now());
    let now_hours = now.getHours().toString().padStart(2, '0');
    let now_mins = now.getMinutes().toString().padStart(2, '0');
    let now_seconds = now.getSeconds().toString().padStart(2, '0');
    if (host == "SYSTEM") {
        console.log(`\x1b[90m[\x1b[0m${now_hours}:${now_mins}:${now_seconds}\x1b[90m|\x1b[97mAudio\x1b[90m] ${msg}`, "\x1b[0m");
    } else {
        let new_host = limitString(host);
        console.log(`\x1b[90m[\x1b[0m${now_hours}:${now_mins}:${now_seconds}\x1b[90m|\x1b[97m${new_host}\x1b[90m] ${msg}`, "\x1b[0m");
    };
};

function getOrCreateAudioPlayer(guildId) {
    if (!players.has(guildId)) {
        const audioPlayer = createAudioPlayer();
        const player_constructor = {
            player: audioPlayer,
            status: "IDLE",
            yt_id: "NONE"
        }
        players.set(guildId, player_constructor);
    };
    return players.get(guildId);
};

function serverQueue(id) {
    main_queue.set(id, queue_constructor);
    queueEmitter.emit('serverAdded', id, queue_constructor);
    return true
};

function getAudioQueue(queueName) {
    const audioPlayer = getOrCreateAudioPlayer(queueName);
    return `${audioPlayer.status}`;
};

function getVictims() {
    return victims;
};

function addToQueue(queueName, entry) {
    if (main_queue.has(queueName)) {
        const QueueOverflow = main_queue.has(queueName) && main_queue.get(queueName).length >= 5;
        if (QueueOverflow) {
            ConsoleLog(entry.guild.name, `\x1b[93mReached queue limit! \x1b[90m[\x1b[97mtotal: ${(main_queue.get(queueName).length)}\x1b[90m]`);
            return false;
        };
        const queue = main_queue.get(queueName);
        queue.push(entry);
        queueEmitter.emit('entryAdded', queueName, entry);
        return true;
    };
};

function clearQueue(queueName) {
    const audioPlayer = getOrCreateAudioPlayer(queueName);
    if (main_queue.has(queueName)) {
        main_queue.get(queueName).length = 0;
        audioPlayer.status = "IDLE";
        audioPlayer.player.stop();
        ConsoleLog(queueName, `\x1b[93mNo VC available. Clearing the server queue.`);
    };
}; 

async function processQueue(queueName, entry) {

    try {

        if (queueName == entry.guild.id) { } else return;

        const audioPlayer = getOrCreateAudioPlayer(queueName);
        audioPlayer.status = "PLAYING"

        const queue = main_queue.get(queueName);
        const index = queue.findIndex(item => item.uuid === entry.uuid);

        let tmp_msg = `\x1b[93mProcessing queue \x1b[90m[\x1b[97m${entry.sender.tag}(${entry.type}) - ${entry.name}\x1b[90m]`;

        if (main_queue.get(queueName).length >= 1) {
            tmp_msg += `\x1b[90m[\x1b[97m${(main_queue.get(queueName).length - 1)} left\x1b[90m]`;
        };

        ConsoleLog(entry.guild.name, tmp_msg);

        if (entry.type == "AUDIO_MP3") {
            const resource = createAudioResource(`./sounds/${entry.file}`, {
                inlineVolume: true
            });
            resource.volume?.setVolume(entry.volume);
            audioPlayer.player.play(resource);
            entry.connection.subscribe(audioPlayer.player);

            if (index !== -1) {
                queue.splice(index, 1);
            };
        } else {
            if (entry.type == "TTS") {
                const url = googleTTS.getAudioUrl(entry.name, {
                    lang: entry.lang,
                    slow: false,
                    host: "https://translate.google.com",
                });
                const tempFile = "./sounds/temp.mp3";
                const res = await fetch(url);
                const buffer = Buffer.from(await res.arrayBuffer());
                fs.writeFileSync(tempFile, buffer);
                const resource = createAudioResource(`./sounds/temp.mp3`, {
                    inlineVolume: true
                });
                resource.volume?.setVolume(0.75);
                audioPlayer.player.play(resource);
                entry.connection.subscribe(audioPlayer.player);
                if (index !== -1) {
                    queue.splice(index, 1);
                };
            } else {
                if (entry.type == "MURASAKI") {
                    const resource = createAudioResource(`./sounds/${entry.file}`, {
                        inlineVolume: true
                    });
                    resource.volume?.setVolume(entry.volume);
                    audioPlayer.player.play(resource);
                    entry.connection.subscribe(audioPlayer.player);

                    if (index !== -1) {
                        queue.splice(index, 1);
                    };

                    const randomMurasaki = async (randomMember, message) => {
                        try {
                            let checkInterval
                            const CreateEmbed = new EmbedBuilder()
                                .setAuthor({ name: `${message.author.tag}`, iconURL: `${message.author.displayAvatarURL()}` })
                                .setDescription(':rightwards_pushing_hand_tone2: :blue_circle:')
                                .setColor("#3498DB")

                            const main_embed = await message.channel.send({ embeds: [CreateEmbed] })

                            setTimeout(() => {
                                const EditEmbed = new EmbedBuilder()
                                    .setAuthor({ name: `${message.author.tag}`, iconURL: `${message.author.displayAvatarURL()}` })
                                    .setDescription(':rightwards_pushing_hand_tone2: :blue_circle: :red_circle: :leftwards_pushing_hand_tone2:')
                                    .setColor("#FF0000")
                                main_embed.edit({ embeds: [EditEmbed] })
                            }, 2000);

                            setTimeout(() => {
                                const EditEmbed = new EmbedBuilder()
                                    .setAuthor({ name: `${message.author.tag}`, iconURL: `${message.author.displayAvatarURL()}` })
                                    .setDescription(':rightwards_pushing_hand_tone2: :blue_circle: :red_circle: :leftwards_pushing_hand_tone2: :pinched_fingers_tone2:')
                                main_embed.edit({ embeds: [EditEmbed] })
                            }, 4000);

                            setTimeout(() => {
                                const EditEmbed = new EmbedBuilder()
                                    .setAuthor({ name: `${message.author.tag}`, iconURL: `${message.author.displayAvatarURL()}` })
                                    .setDescription(':rightwards_pushing_hand_tone2: :blue_circle: :red_circle: :leftwards_pushing_hand_tone2: :pinched_fingers_tone2: :purple_circle:')
                                    .setColor("#9B59B6")
                                main_embed.edit({ embeds: [EditEmbed] })
                            }, 6500);

                            setTimeout(() => {

                                if (randomMember) {
                                    checkInterval = setInterval(() => {
                                        if (randomMember.voice.channel == null) { } else {
                                            clearInterval(checkInterval);

                                            main_embed.delete()

                                            victims.set(randomMember.user.id, true)

                                            randomMember.voice.setChannel(null)
                                                .catch(err => { console.error(err); });

                                            const rawData = fs.readFileSync(`./database/nick/${message.guild.id}.json`);
                                            const jsonData = JSON.parse(rawData);
                                            let target = "เปรตนั้น";
                                            for (const user of jsonData.users) {
                                                if (randomMember.user.id === user.id) {
                                                    target = `${user.name}`;
                                                    break;
                                                }
                                            }

                                            setTimeout(() => {
                                                const textToSpeak = `${target}โดนมุราซากี้ลอยหายไปแล้ว`;
                                                let queue_constructor = {
                                                    uuid: `${generateUUID()}`,
                                                    name: `${textToSpeak}`,
                                                    lang: `th`,
                                                    type: "TTS",
                                                    guild: message.guild,
                                                    sender: message.author,
                                                    voice_channel: entry.voice_channel,
                                                    connection: entry.connection
                                                }
                                                addToQueue(queueName, queue_constructor);

                                                ConsoleLog(entry.guild.name, `\x1b[90m[\x1b[37m${randomMember.user.tag}\x1b[90m] \x1b[35mhas been killed by Murasaki from \x1b[90m[\x1b[37m${message.author.username}\x1b[90m]`)
                                            }, 500);

                                        }
                                    }, 1000);
                                }

                            }, ((8500) + (Math.floor(Math.random() * (2000 - 500 + 1)) + 500)));
                        } catch (error) {
                            console.log(error)
                        };
                    };

                    await randomMurasaki(entry.target, entry.message, queueName);

                } else {
                    if (entry.type == "YOUTUBE") {
                        audioPlayer.yt_id = `${entry.uuid}`
                        const resource = createAudioResource(`./sounds/youtube/${entry.file}`, {
                            inlineVolume: true
                        });
                        resource.volume?.setVolume(0.05);
                        audioPlayer.player.play(resource);
                        entry.connection.subscribe(audioPlayer.player);

                        if (index !== -1) {
                            queue.splice(index, 1);
                        };

                    } else {

                        audioPlayer.status = "IDLE"

                    };
                };
            };
        };

    } catch (error) {
        ConsoleLog("SYSTEM", `\x1b[31m---------------------------------------------------------------`);
        ConsoleLog("SYSTEM", `\x1b[31mError Occurred: \x1b[37m"${error}" \x1b[31mfrom \x1b[37m"${path.basename(__filename)}"`);
        ConsoleLog("SYSTEM", `\x1b[31m---------------------------------------------------------------`);
    };

};

queueEmitter.on('entryAdded', (queueName, entry) => {
    const audioPlayer = getOrCreateAudioPlayer(queueName);
    ConsoleLog(entry.guild.name, `\x1b[90m[\x1b[97m${entry.sender.tag}(${entry.type}) - ${entry.name}\x1b[90m] \x1b[93mhas been added to the queue \x1b[90m[\x1b[97mtotal: ${(main_queue.get(queueName).length)}\x1b[90m]`);
    if (audioPlayer.status === "IDLE") {
        processQueue(queueName, entry);
    };
});

queueEmitter.on('serverAdded', (id, queue) => {
    const audioPlayer = getOrCreateAudioPlayer(id);
    audioPlayer.player.on('stateChange', (oldState, newState) => {
        const isQueueEmpty = main_queue.has(id) && main_queue.get(id).length === 0;
        if (newState.status === 'idle') {
            audioPlayer.status = "IDLE"
            if (audioPlayer.yt_id == "NONE") { } else {
                if (fs.existsSync(`./sounds/youtube/${audioPlayer.yt_id}.mp3`)) {
                    const filePath = `./sounds/youtube/${audioPlayer.yt_id}.mp3`;
                    fs.unlink(filePath, (err) => {
                        if (err) {
                            throw err;
                        };
                    });
                };
                audioPlayer.yt_id == "NONE";
            };
            if (!isQueueEmpty) {
                processQueue(id, main_queue.get(id)[0]);
            };
        } else {
            if (newState.status === 'playing') {
                audioPlayer.status = "PLAYING"
            };
        };
    });
});

////////////////////////////////////////////////
///////////////////// Main /////////////////////
//////////////////////////////////////////////// 

exports.main_queue = main_queue;
exports.addToQueue = addToQueue;
exports.clearQueue = clearQueue;
exports.getAudioQueue = getAudioQueue;
exports.getVictims = getVictims;
exports.serverQueue = serverQueue;