const { Client, ActivityType, GatewayIntentBits, Collection, EmbedBuilder, SlashCommandBuilder, NewsChannel } = require('discord.js');
const { createAudioPlayer, createAudioResource, joinVoiceChannel, getVoiceConnection } = require('@discordjs/voice');
require('dotenv').config();
const bot = new Client({
    intents: [
        GatewayIntentBits.Guilds,
        GatewayIntentBits.GuildMessages,
        GatewayIntentBits.MessageContent,
        GatewayIntentBits.GuildMembers,
        GatewayIntentBits.GuildInvites,
        GatewayIntentBits.GuildVoiceStates,
        GatewayIntentBits.DirectMessageReactions,
        GatewayIntentBits.GuildMessageReactions
    ],
}); 

const { inspect } = require('util')
const config = require('./config.json');
const fs = require("fs");
const path = require('path');
const notifier = require('node-notifier');
const pidusage = require('pidusage');
const { Player } = require("discord-player")
const { DefaultExtractors } = require('@discord-player/extractor');
const { main_queue, addToQueue, clearQueue, getVictims, serverQueue } = require('./audio_queue.js');
const bot_functions = require('./bot_functions.js');
const { error } = require('console');

const BotLogs = bot_functions.BotLogs;
bot.BotLogs = BotLogs;
bot.servers = new Map();
bot.player = new Player(bot, {
    ytdlOptions: {
        quality: "highestaudio",
        highWaterMark: 1 << 25
    }
});

bot.commands = new Collection();
bot.disable_commands = new Collection();
bot.snipes = new Map();

let first_join = true
let _proc = true
let anti_nat
let sub_monitor = false
let totalMemoryOver = 0
let cachedGuild;
let startTime;
config.old_vc_id = ""
config.domain_expanded = false
config.domain_owner = ""
fs.writeFileSync('./config.json', JSON.stringify(config, null, 4));

function limitString(str) {
    return str.substring(0, 10).padEnd(10, ' ');
};

bot.limitString = limitString;

function generateUUID() {
    const characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    let uuid = '';

    for (let i = 0; i < 10; i++) {
        const randomIndex = Math.floor(Math.random() * characters.length);
        uuid += characters[randomIndex];
    }

    return uuid;
}

function readVoiceChannelID(callback) {
    try {
        callback(config.old_vc_id)
    } catch (error) { };
};

function reconnectToVoiceChannel(channelID) {
    try {
        const voiceChannel = bot.channels.cache.get(`${channelID}`);
        if (voiceChannel) {
            first_join = false
            const connection = joinVoiceChannel({
                channelId: voiceChannel.id,
                guildId: cachedGuild.id,
                adapterCreator: cachedGuild.voiceAdapterCreator,
            });
            let channelMemberCount = voiceChannel.members.size;
            if (channelMemberCount <= 1) {
                try {
                    connection.disconnect()
                } catch (error) { }
            }
            config.old_vc_id = ""
            fs.writeFileSync('./config.json', JSON.stringify(config, null, 4));
        };
    } catch (error) { };
};

function formatDate(date) {
    const options = { year: 'numeric', month: 'short', day: 'numeric', hour: 'numeric', minute: 'numeric', second: 'numeric' };
    return date.toLocaleDateString('en-US', options);
};

function GetJSONVariables(id) {
    const variables = {
        "ID": `${id}`,
        "Level": 0,
        "Coins": 0,
        "XP": 0,
        "Multiplier": 1,
        "Tools": {
            "Rod": "🎣 Noob Rod",
            "WateringCan": "🚿 Noob Watering Can",
            "Bait": 0
        },
        "Inventory": {
            "Fish": 0,
            "Wheat": 0,
            "Potato": 0,
            "Carrot": 0,
            "Corn": 0
        },
        "Farming": {
            "CropDrop": 1,
            "Slot": 4,
            "Farmlands": []
        },
        "Dungeon": {
            "Completion": [],
            "Unit": [],
            "Party": []
        }
    };
    return variables
};

process.on('uncaughtException', error => {

    let current_count = 0;
    totalMemoryOver = 0

    //console.clear();

    console.log(error);

    let lastRestartTime = new Date();
    let lastrt = formatDate(lastRestartTime)

    ///////////////////////////////////////////// Error Message

    let err_msg = error.toString().replace(/^Error: /, "");
    let lower_msg = err_msg.toLowerCase()
    if (err_msg == "Expected token to be set for this request, but none was present") return
    if (err_msg == "RangeError: Shard 0 not found") return
    if (lower_msg.includes("getaddrinfo enotfound")) {
        err_msg = "No Internet Connection"
    };
    if (err_msg.includes("ConnectTimeoutError: Connect Timeout Error (attempted addresses:")) {
        err_msg = "No Internet Connection"
    };

    ///////////////////////////////////////////// Error Message

    current_count = Number(config.rt_count);
    current_count += 1;
    config.rt_count = current_count
    config.rt_time = lastrt.toString()
    config.domain_expanded = false
    config.domain_owner = ""

    fs.writeFileSync('./config.json', JSON.stringify(config, null, 4));

    if (current_count > 1) {
        first_join = false
        BotLogs("SYSTEM", `\x1b[31mError Occurred! Reconnecting... \x1b[31m(x\x1b[33m${current_count}\x1b[31m)`);
    } else {
        config.rt_error = err_msg
        config.rt_attempt += 1
        BotLogs("SYSTEM", `\x1b[31m---------------------------------------------------------------`);
        BotLogs("SYSTEM", `\x1b[31mError Occurred: \x1b[97m"${err_msg}"`);
        BotLogs("SYSTEM", `\x1b[31m---------------------------------------------------------------`);

        BotLogs("SYSTEM", `\x1b[31mConnection lost!`);
        BotLogs("SYSTEM", `\x1b[33mAttempting to reconnect...`);

        fs.writeFileSync('./config.json', JSON.stringify(config, null, 4));
    };

    setTimeout(() => {
        process.exit();
    }, 2000);
});

async function clearChannel(channelId) {
    try {
        const channel = await bot.channels.fetch(channelId);
        const fetched = await channel.messages.fetch({ limit: 100 });
        await channel.bulkDelete(fetched);
    } catch (error) {
    };
};

bot.on('clientReady', async () => {

    await bot.player.extractors.loadMulti(DefaultExtractors);

    cachedGuild = bot.guilds.cache.get('467655562658578432');
    startTime = new Date();
    totalMemoryOver = 0

    let on_si = formatDate(startTime)
    if (config.rt_count >= 1) { } else {
        config.startTime = startTime
        config.online_since = on_si.toString()
    };
    fs.writeFileSync('./config.json', JSON.stringify(config, null, 4));

    //console.clear()

    async function LoadBuilding() {
        return new Promise((resolve, reject) => {

            let build = config.nozomibot.build

            if (config.rt_count >= 1) {
                first_join = false
            } else {
                now = new Date(Date.now());
                now_hours = now.getHours().toString().padStart(2, '0');
                now_mins = now.getMinutes().toString().padStart(2, '0');
                now_seconds = now.getSeconds().toString().padStart(2, '0');
                config.nozomibot.build += 1
                config.rt_attempt = 0
                BotLogs("SYSTEM", `\x1b[95mLoading \x1b[97mNozomiBot \x1b[95mVersion \x1b[97m${config.nozomibot.version} \x1b[95mBuild \x1b[97m${(build + 1)}\x1b[95m`);

                fs.writeFileSync('./config.json', JSON.stringify(config, null, 4));
            };

            setTimeout(() => {
                if (config.rt_count >= 1) { } else {
                    now = new Date(Date.now());
                    now_hours = now.getHours().toString().padStart(2, '0');
                    now_mins = now.getMinutes().toString().padStart(2, '0');
                    now_seconds = now.getSeconds().toString().padStart(2, '0');
                    BotLogs("SYSTEM", `\x1b[95m\x1b[97mNozomiBot \x1b[95mBuild Completed!`);
                };
                resolve(build);
            }, 500);

        });
    };

    async function LoadServer() {
        return new Promise((resolve, reject) => {

            let amount = 0

            bot.guilds.cache.forEach(guild => {
                //BotLogs("SYSTEM", `\x1b[35mLoaded \x1b[97m"${guild.name} | ${guild.id}"`);
                amount += 1
                serverQueue(guild.id);
                const server_constructor = {
                    guild: guild,
                    id: guild.id,
                    TEMP_FOUND_VC: true,
                    ForceJoin: true,
                    first_join: true
                };
                bot.servers.set(guild.id, server_constructor);
            });

            if (config.rt_count >= 1) { } else {
                now = new Date(Date.now());
                now_hours = now.getHours().toString().padStart(2, '0');
                now_mins = now.getMinutes().toString().padStart(2, '0');
                now_seconds = now.getSeconds().toString().padStart(2, '0');
                BotLogs("SYSTEM", `\x1b[95mLoading Environments...`);

            };

            setTimeout(() => {
                if (config.rt_count >= 1) { } else {
                    now = new Date(Date.now());
                    now_hours = now.getHours().toString().padStart(2, '0');
                    now_mins = now.getMinutes().toString().padStart(2, '0');
                    now_seconds = now.getSeconds().toString().padStart(2, '0');
                    BotLogs("SYSTEM", `\x1b[95mServer Registered: \x1b[97m${amount} \x1b[95mserver!`);
                };
                resolve(amount);
            }, 500);

        });
    };

    async function LoadCommands() {
        return new Promise((resolve, reject) => {
            now = new Date(Date.now());
            now_hours = now.getHours().toString().padStart(2, '0');
            now_mins = now.getMinutes().toString().padStart(2, '0');
            now_seconds = now.getSeconds().toString().padStart(2, '0');

            if (config.rt_count >= 1) { } else {
                //BotLogs("SYSTEM", `\x1b[95mLoading commands...`);
            };

            fs.readdir('./commands', (err, files) => {
                if (err) return console.log(err);
                let jsfile = files.filter(f => f.split(".").pop() == 'js');
                if (jsfile.length == 0) { return BotLogs("SYSTEM", `\x1b[95mNo Commands found!`) }

                var loadedfile = 0;

                jsfile.forEach(f => {
                    let props = require(`./commands/${f}`);
                    loadedfile = loadedfile + 1;
                    bot.commands.set(props.help.name, props)
                });

                setTimeout(() => {
                    if (config.rt_count >= 1) { } else {
                        now = new Date(Date.now());
                        now_hours = now.getHours().toString().padStart(2, '0');
                        now_mins = now.getMinutes().toString().padStart(2, '0');
                        now_seconds = now.getSeconds().toString().padStart(2, '0');
                        BotLogs("SYSTEM", `\x1b[95mCommands Loaded: \x1b[97m${loadedfile} \x1b[95mfiles!`);
                    };
                    resolve(loadedfile);
                }, 500);

            });

        });
    };

    async function CheckVoiceChannel() {

        for (let [key, value] of bot.servers) {

            let tempGuild = bot.guilds.cache.get(value.id);

            const botMember = tempGuild.members.cache.get("887531368836370483");
            if (botMember)
                if (botMember.voice.channel) {
                    if (botMember.voice.channel.members.size <= 1) { } else {
                        value.ForceJoin = false
                        value.first_join = false
                        const connection = joinVoiceChannel({
                            channelId: botMember.voice.channel.id,
                            guildId: tempGuild.id,
                            adapterCreator: tempGuild.voiceAdapterCreator,
                        });
                        BotLogs(value.guild.name, `\x1b[36mRejoining Previous VC \x1b[90m[\x1b[37m${botMember.voice.channel.name}\x1b[90m]`);
                    };
                } else {
                    const voiceChannels = tempGuild.channels.cache.filter(channel => channel.type === 2);
                    voiceChannels.forEach(voiceChannel => {
                        if (value.TEMP_FOUND_VC == false) return
                        if (value.ForceJoin) {
                            if (voiceChannel.members.size >= 1) {
                                if (value.TEMP_FOUND_VC == false) return
                                if (voiceChannel.id == tempGuild.afkChannelId) return
                                value.TEMP_FOUND_VC = false
                                const connection = joinVoiceChannel({
                                    channelId: voiceChannel.id,
                                    guildId: tempGuild.id,
                                    adapterCreator: tempGuild.voiceAdapterCreator,
                                });
                                BotLogs(value.guild.name, `\x1b[36mJoining Active VC \x1b[90m[\x1b[37m${voiceChannel.name}\x1b[90m]`);
                            };
                        };
                    });
                }

        };

    };

    async function backupDatabase() {
        return new Promise((resolve, reject) => {
            try {
                const sourceFile = path.join(__dirname, 'database', 'users.json');
                const backupDir = path.join(__dirname, 'backup');
                const backupFile = path.join(backupDir, 'user_backup.json');

                if (!fs.existsSync(backupDir)) {
                    fs.mkdirSync(backupDir, { recursive: true });
                };

                if (!fs.existsSync(sourceFile)) {
                    BotLogs("SYSTEM", `\x1b[31m---------------------------------------------------------------`);
                    BotLogs("SYSTEM", `\x1b[31mError: Source file does not exist: \x1b[37m"${sourceFile}"`);
                    BotLogs("SYSTEM", `\x1b[31m---------------------------------------------------------------`);
                    return reject(`Source file does not exist: ${sourceFile}`);
                };

                fs.readFile(sourceFile, 'utf8', (err, data) => {
                    if (err) {
                        BotLogs("SYSTEM", `\x1b[31m---------------------------------------------------------------`);
                        BotLogs("SYSTEM", `\x1b[31mError reading the source file: \x1b[37m"${err}"`);
                        BotLogs("SYSTEM", `\x1b[31m---------------------------------------------------------------`);
                        return reject(err);
                    };

                    try {
                        JSON.parse(data);
                    } catch (e) {
                        BotLogs("SYSTEM", `\x1b[31m---------------------------------------------------------------`);
                        BotLogs("SYSTEM", `\x1b[31mError: Source file is corrupted and cannot be parsed as JSON: \x1b[37m"${sourceFile}"`);
                        BotLogs("SYSTEM", `\x1b[31m---------------------------------------------------------------`);
                        return reject(`Source file is corrupted and cannot be parsed as JSON: ${sourceFile}`);
                    };

                    fs.copyFile(sourceFile, backupFile, (err) => {
                        if (err) {
                            BotLogs("SYSTEM", `\x1b[31m---------------------------------------------------------------`);
                            BotLogs("SYSTEM", `\x1b[31mError backing up the database: \x1b[37m"${err}"`);
                            BotLogs("SYSTEM", `\x1b[31m---------------------------------------------------------------`);
                            return reject(err);
                        } else {
                            let nowTime = new Date();
                            let last_backup = formatDate(nowTime)
                            config.last_backup = last_backup.toString()
                            fs.writeFileSync('./config.json', JSON.stringify(config, null, 4));
                            //BotLogs("SYSTEM", `\x1b[92mDatabase backed up successfully!`);
                            return resolve(backupFile);
                        }
                    });
                });
            } catch (error) {
                console.log(error);
                reject(error);
            };
        });
    };

    function checkMemoryUsage() {
        try {

            let memoryThresholdMB = 128;

            pidusage(process.pid, (err, stats) => {
                if (err) {
                    return;
                }

                let memoryUsageMB = stats.memory / (1024 * 1024);

                if (memoryUsageMB > memoryThresholdMB) {

                    totalMemoryOver++

                    if (totalMemoryOver >= 4) {
                        totalMemoryOver = 0
                        bot.destroy();
                        bot.BotLogs("SYSTEM", `\x1b[33mRestarting...`);
                        setTimeout(() => {
                            process.exit();
                        }, 2000);
                        return
                    }

                    BotLogs("SYSTEM", `\x1b[31m⚠️ Memory overload: \x1b[37m${memoryUsageMB.toFixed(2)} MB`);

                    notifier.notify({
                        title: `[SYSTEM ALERT]`,
                        message: `⚠️ Memory overload: ${memoryUsageMB.toFixed(2)} MB`,
                        icon: path.join(`D:/PanCakeBot/image`, 'icon.png'),
                        appID: "MeguBot Discord",
                        sound: true,
                        wait: false,
                    }, function (err, response) {
                        if (err) {
                            console.error(`Notification Error: ${err}`);
                        }
                    });
                };
            });

        } catch (error) { };
    };

    await LoadBuilding();
    await LoadServer();
    await LoadCommands();

    const console_server = require('./console_server.js');

    bot.user.setPresence({
        status: 'online',
        activities: [{
            name: `MeguBot | V ${config.nozomibot.version} B.${config.nozomibot.build}`,
            type: ActivityType.Custom
        }]
    });

    if (config.rt_count > 0) {
        BotLogs("SYSTEM", `\x1b[92m---------------------------------------------------------------`);
        BotLogs("SYSTEM", `\x1b[92mReconnected to Discord!`);
        BotLogs("SYSTEM", `\x1b[92m---------------------------------------------------------------`);
    } else {
        BotLogs("SYSTEM", `\x1b[92m---------------------------------------------------------------`);
        BotLogs("SYSTEM", `\x1b[92mConnected to Discord!`);
        BotLogs("SYSTEM", `\x1b[92m---------------------------------------------------------------`);
    };

    CheckVoiceChannel();
    readVoiceChannelID(reconnectToVoiceChannel);

    config.rt_count = 0
    config.audio_queue_logs = false
    config.online_ping = true
    config.tts_status = true
    config.fishing_status = true
    config.farming_status = true
    fs.writeFileSync('./config.json', JSON.stringify(config, null, 4));

    clearChannel("1225208114941399110");
    setInterval(async () => {
        try {

            bot.user.setPresence({
                status: 'online',
                activities: [{
                    name: `MeguBot | V ${config.nozomibot.version} B.${config.nozomibot.build}`,
                    type: ActivityType.Custom
                }]
            });

            if (config.online_ping) {
                clearChannel("1225208114941399110");
                bot.channels.fetch('1225208114941399110')
                    .then(channel => {
                        channel.send('IM ONLINE!!!');
                    });
            };

        } catch (error) { console.log(error) };
    }, 10000);

    setInterval(async () => {
        await backupDatabase();
    }, 300000);

    setInterval(() => {
        checkMemoryUsage();
    }, 5000);

    setInterval(() => {
        now = new Date(Date.now());
        now_hours = now.getHours().toString().padStart(2, '0');
        now_mins = now.getMinutes().toString().padStart(2, '0');
        now_seconds = now.getSeconds().toString().padStart(2, '0');
        const voiceChannels = cachedGuild.channels.cache.filter(channel => channel.type === 2);
        voiceChannels.forEach(voiceChannel => {

            if (voiceChannel.id == "542028952290721793" || voiceChannel.id == "954512531563618360") return
            if (voiceChannel.members.size >= 3) {

                let total_xp_text = " "

                voiceChannel.members.forEach(async (member) => {

                    if (member.user.id == "887531368836370483") return

                    try {

                        let us_user_id = member.user.id
                        let us_lv_xp = config.us.xp_per
                        let us_minXP = config.us.vc_min
                        let us_maxXP = config.us.vc_max

                        const variables = GetJSONVariables(us_user_id);

                        function us_read() {
                            const rawData = fs.readFileSync('./database/users.json');
                            return JSON.parse(rawData);
                        };

                        function us_write(users) {
                            fs.writeFileSync('./database/users.json', JSON.stringify(users, null, 4));
                        };

                        function updateUserData(userId, newData) {
                            let users = us_read();
                            if (!users[userId]) return;

                            for (const key in newData) {
                                if (Object.hasOwnProperty.call(newData, key)) {
                                    users[userId][key] = newData[key];
                                }
                            }

                            us_write(users);
                        };

                        function us_new(us_user_id, variables) {
                            let users = us_read();
                            if (!users[us_user_id]) {
                                users[us_user_id] = variables;
                            } else { return }
                            us_write(users);
                        };

                        async function CheckLevelUp(users, userId) {
                            if (users[userId].Level >= config.us.max_level) {
                                return users;
                            };

                            const requiredXP = Number(Math.floor((users[userId].Level * config.us.xp_per) * Math.pow(config.us.xp_growth, users[userId].Level)));

                            if (users[userId].XP >= requiredXP) {
                                users[userId].XP -= requiredXP;
                                users[userId].Level += 1;
                                users[userId].Coins += (users[userId].Level * 100)

                                updateUserData(userId, {
                                    XP: users[userId].XP,
                                    Level: users[userId].Level,
                                    Coins: users[userId].Coins,
                                });

                                BotLogs("SYSTEM", `\x1b[90m[\x1b[37m${member.user.tag}\x1b[90m] \x1b[93mreached \x1b[1m\x1b[37mLevel ${users[userId].Level}\x1b[93m! GG GUYS!`);
                            };

                            return users;
                        };

                        async function addXP(userId) {
                            const xpToAdd = Math.floor(Math.random() * (us_maxXP - us_minXP + 1)) + us_minXP;
                            let users = us_read();

                            if (!users[userId] || users[userId].XP === undefined) {
                                return;
                            };

                            users[userId].XP += Number((xpToAdd * users[userId].Multiplier).toFixed(2));
                            updateUserData(userId, { XP: users[userId].XP });

                            total_xp_text += `<@${userId}> **(x${users[userId].Multiplier})**: + **${(xpToAdd * users[userId].Multiplier).toFixed(2)}**\n`;

                            let oldLevel = users[userId].Level;
                            let startLevel = oldLevel;
                            let endLevel = oldLevel;

                            while (true) {
                                users = us_read();
                                users = await CheckLevelUp(users, userId);

                                if (oldLevel === users[userId].Level) {
                                    break;
                                };

                                endLevel = users[userId].Level;
                                oldLevel = users[userId].Level;
                            };

                            if (startLevel == endLevel) return;

                            let levelUpDescription = `ยินดีด้วยมึงอัพเลเวลการพูดมากเป็น **Level ${startLevel} -> ${endLevel}**! แหลงมากจริงมึงนิ!`;

                            const levelUpEmbed = new EmbedBuilder()
                                .setAuthor({ name: `${member.user.tag}`, iconURL: `${member.user.displayAvatarURL()}` })
                                .setThumbnail(member.user.displayAvatarURL())
                                .setDescription(levelUpDescription)
                                .setColor("#E4FF00")
                                .setTimestamp();

                            const mention = `<@${member.user.id}>`;
                            try {
                                const channel = await bot.channels.fetch('890270038525902940');
                                const msg = await channel.send({ content: mention, embeds: [levelUpEmbed] });
                                setTimeout(() => {
                                    if (msg) {
                                        msg.delete().catch(console.error);
                                    }
                                }, 25000);
                            } catch (error) {
                                console.error(error);
                            };

                        };

                        us_new(us_user_id, variables);
                        addXP(us_user_id);

                    } catch (error) { console.log(error) };

                });

                const XPMessage = new EmbedBuilder()
                    .setAuthor({ name: `XP from Active Voice Chat`, iconURL: `${bot.user.displayAvatarURL()}` })
                    .setThumbnail(bot.user.displayAvatarURL())
                    .setDescription(total_xp_text)
                    .setColor("#FFBF00")
                    .setTimestamp()

                bot.channels.fetch('1234508652199542814')
                    .then(channel => {
                        channel.send({ embeds: [XPMessage] });
                    });

            };
        });
    }, 60000);

    ////////////////////////////////////////////

    try {
        const rawData = fs.readFileSync('./database/fishing_ponds.json');
        const fishingData = JSON.parse(rawData);

        if (fishingData.length === 0) return

        let commandFile = bot.commands.get(`force_fish`);

        fishingData.forEach(async ([userId, eventData]) => {

            let fetchChannel = await bot.channels.fetch(eventData.channelId);
            let fetchMessage = null;

            async function GetMessage() {
                try {
                    let target = await fetchChannel.messages.fetch(eventData.messageId);
                    if (target) {
                        return target;
                    } else {
                        return null;
                    };
                } catch (error) { return null }
            };

            fetchMessage = await GetMessage();
            if (fetchMessage == null) { } else {
                fetchMessage.delete()
            };

            commandFile.run(bot, eventData.message, "force_fish");

        });

    } catch (error) {
        console.log(error)
    };

    ////////////////////////////////////////////

});

bot.on('messageDelete', function (message, channel) {

    bot.snipes.set(message.channel.id, {
        content: message.content,
        author: message.author,
        image: message.attachments.first() ? message.attachments.first().proxyURL : null

    })

    async function SendSnipedMSG() {
        const msg = bot.snipes.get(message.channel.id);
        if (msg.author.bot) return;

        let isGif = "false"
        if (message.content.includes("https://tenor.com/view")) {
            isGif = "true"
        } else {
            if (message.content.includes("gif")) {
                isGif = "true"
            }
        }

        if (isGif == "true") return;

        if (!msg) return message.channel.send("There is nothing to snipe!");

        if (msg.content.length >= 1) {

            const SnipedMessage = new EmbedBuilder()
                .setAuthor({ name: `Message Deleted by: ${msg.author.tag}`, iconURL: `${msg.author.displayAvatarURL()}` })
                .setDescription(msg.content)
                .setColor("#DE3163")
                .setImage(msg.image)
                .setTimestamp()

            bot.channels.fetch('1078286177108508682')
                .then(channel => {
                    channel.send({ embeds: [SnipedMessage] });
                });
            bot.channels.fetch('1079718526023118858')
                .then(channel => {
                    channel.send({ embeds: [SnipedMessage] });
                });

        } else {

            const SnipedMessage = new EmbedBuilder()
                .setAuthor({ name: `Message Deleted by: ${msg.author.tag}`, iconURL: `${msg.author.displayAvatarURL()}` })
                .setColor("#DE3163")
                .setImage(msg.image)
                .setTimestamp()

            bot.channels.fetch('1078286177108508682')
                .then(channel => {
                    channel.send({ embeds: [SnipedMessage] });
                });
            bot.channels.fetch('1079718526023118858')
                .then(channel => {
                    channel.send({ embeds: [SnipedMessage] });
                });

        };

    };

    let prefix = config.prefix;
    if (message.content.startsWith(prefix)) { return };

    SendSnipedMSG();

});

bot.on('inviteCreate', async (invite) => {
    BotLogs(invite.guild.name, `\x1b[94mInvite Code: \x1b[90m[\x1b[37m${invite.code}\x1b[90m] \x1b[94mwas created by \x1b[90m[\x1b[37m${invite.inviter.tag}\x1b[90m]`)
});

bot.on('guildMemberAdd', (member) => {
    BotLogs(member.guild.name, `\x1b[90m[\x1b[37m${member.user.tag}\x1b[90m] \x1b[92mhas joined from the guild \x1b[90m[\x1b[37m${member.guild.name}\x1b[90m]`)
});

bot.on('guildMemberRemove', (member) => {
    BotLogs(member.guild.name, `\x1b[90m[\x1b[37m${member.user.tag}"\x1b[90m] \x1b[92mhas left from the guild \x1b[90m[\x1b[37m${member.guild.name}\x1b[90m]`)
});

bot.on('messageCreate', async (message) => {

    if (message) { } else { return }
    if (message.author.bot) return;

    let prefix = config.prefix;
    let Content = message.content
    let MessageArray = Content.split(' ');
    let cmd = MessageArray[0].slice(prefix.length);
    let args = MessageArray.slice(1);

    if (message.guild.id == "467655562658578432" || message.guild.id == "689443742289100834") {

        if (message.content.startsWith(prefix)) {
            let commandFile = bot.commands.get(cmd.toLowerCase());
            if (!commandFile) {
                bot.commands.forEach(command => {
                    if (command.help.aliases && command.help.aliases.includes(cmd.toLowerCase())) {
                        commandFile = command;
                    }
                });
            }
            if (commandFile) {
                commandFile.run(bot, message, args);
                //bot.BotLogs("SYSTEM", `\x1b[90m[\x1b[0m${message.author.tag}\x1b[90m] has excuted [\x1b[0m${commandFile.help.name}\x1b[90m]`)
                return
            } else {
                return message.reply(`Unknown Command! Type \`!help\` for help,`)
                    .then(msg => {
                        setTimeout(() => {
                            if (msg) {
                                msg.delete().catch(console.error);
                                message.delete()
                            }
                        }, 3500);
                    })
                    .catch(console.error);
            }
        };

    } else return

    bot.snipes.set(message.channel.id, {
        content: message.content,
        author: message.author,
        image: message.attachments.first() ? message.attachments.first().proxyURL : null
    })

    let isGif = "false"

    if (message.content.includes("https://tenor.com/view")) {
        isGif = "true"
    } else {
        if (message.content.includes(".gif")) {
            isGif = "true"
        } else {
            if (message.content.includes(".giphy")) {
                isGif = "true"
            };
        };
    };

    let abcde = false

    if (message.content.length == 18) {
    } else {
        if (abcde) {
        } else {
            try {
                let output
                let comma
                const cleanInput = message.content.replace(/,/g, '');
                const sanitizedInput = cleanInput.replace(/x/g, '*').replace(/X/g, '*').replace(/%/g, '/100')
                const result = await eval(sanitizedInput)
                output = result;

                var short_answer;
                async function shortanswer() {
                    if (short_answer >= 1000000000000000000) {
                        short_answer = (short_answer / 1000000000000000000).toFixed(2).replace(/\.0$/, '') + 'QT';
                        return
                    }
                    if (short_answer >= 1000000000000000) {
                        short_answer = (short_answer / 1000000000000000).toFixed(2).replace(/\.0$/, '') + 'Q';
                        return
                    }
                    if (short_answer >= 1000000000000) {
                        short_answer = (short_answer / 1000000000000).toFixed(2).replace(/\.0$/, '') + 'T';
                        return
                    }
                    if (short_answer >= 1000000000) {
                        short_answer = (short_answer / 1000000000).toFixed(2).replace(/\.0$/, '') + 'B';
                        return
                    }
                    if (short_answer >= 1000000) {
                        short_answer = (short_answer / 1000000).toFixed(2).replace(/\.0$/, '') + 'M';
                        return
                    }
                    if (short_answer >= 1000) {
                        short_answer = (short_answer / 1000).toFixed(2).replace(/\.0$/, '') + 'K';
                        return
                    }
                    return short_answer;
                };
                function addCommas(number) {
                    const [integerPart, decimalPart] = String(number).split('.');
                    const formattedIntegerPart = integerPart.replace(/\B(?=(\d{3})+(?!\d))/g, ',');
                    const formattedNumber = decimalPart ? `${formattedIntegerPart}.${decimalPart}` : formattedIntegerPart;
                    return String(formattedNumber);
                }

                let makecomma = Number(output)
                comma = addCommas(makecomma);
                short_answer = Number(output.toFixed(2));
                shortanswer();

                if (typeof result !== "string") {
                    output = inspect(result)
                };

                if (message.content == output) return;

                let Question = "```" + message.content + "```"

                let Final = "```" + `Flat: ${output}\nCommas: ${comma}\nShort: ${short_answer}` + "```"

                if (output == "Infinity") {
                    message.channel.send(`**From:** <@${message.author.id}>\n**Question:**\n${Question}\n**Result:**\n**undefined**`)
                        .then(msg => {
                            setTimeout(() => {
                                if (msg) {
                                    msg.delete().catch(console.error);
                                }
                                if (message && message.deletable) {
                                    message.delete().catch(console.error);
                                }
                            }, 15000)

                        })
                        .catch(console.error);
                    return;
                };

                if (output >= 0) {
                    message.channel.send(`**From:** <@${message.author.id}>\n**Question:**\n${Question}\n**Result:**\n${Final}`)
                        .then(msg => {
                            setTimeout(() => {
                                if (msg) {
                                    msg.delete().catch(console.error);
                                }
                                if (message && message.deletable) {
                                    message.delete().catch(console.error);
                                }
                            }, 15000)

                        })
                        .catch(console.error);
                    return;
                }
                if (output <= 0) {
                    message.channel.send(`**From:** <@${message.author.id}>\n**Question:**\n${Question}\n**Result:**\n${Final}`)
                        .then(msg => {
                            setTimeout(() => {
                                if (msg) {
                                    msg.delete().catch(console.error);
                                }
                                if (message && message.deletable) {
                                    message.delete().catch(console.error);
                                }
                            }, 15000)

                        })
                        .catch(console.error);
                    return;
                };

            } catch (error) {

            };
        };
    };

    if (message.author.bot) return;

    let tts_channel = ""
    if (fs.existsSync(`./database/variables/${message.guild.id}.json`)) {
        const rawData = fs.readFileSync(`./database/variables/${message.guild.id}.json`);
        const jsonData = JSON.parse(rawData);
        tts_channel = jsonData.channel.tts
        if (message.channel.id == tts_channel) {
            try {

                let Content = message.content
                let MessageArray = Content.split(' ');

                const textToSpeak = MessageArray.join(' ');
                let new_text = "";
                if (textToSpeak.length > 45) {
                    new_text = textToSpeak.substring(0, 45);
                    new_text += "..."
                } else {
                    new_text = textToSpeak
                };

                const botMember = message.guild.members.cache.get("887531368836370483");
                const voiceChannel = botMember.voice.channel;

                if (voiceChannel) {
                    const connection = joinVoiceChannel({
                        channelId: voiceChannel.id,
                        guildId: message.guild.id,
                        adapterCreator: message.guild.voiceAdapterCreator,
                    });

                    if (config.audio_queue_logs == false) {
                        bot.BotLogs(message.guild.name, `\x1b[90m[\x1b[37m${message.author.tag}\x1b[90m] \x1b[35mused TTS for \x1b[90m[\x1b[37m${new_text}\x1b[90m]`);
                    };

                    let queue_constructor = {
                        uuid: `${generateUUID()}`,
                        name: `${textToSpeak}`,
                        lang: `th`,
                        type: "TTS",
                        guild: message.guild,
                        sender: message.author,
                        voice_channel: voiceChannel,
                        connection: connection
                    }
                    addToQueue(message.guild.id, queue_constructor);
                };

            } catch (error) {
                console.log(error);
            };
        };
    };

    const msg = bot.snipes.get(message.channel.id);
    if (message.guild.id == "467655562658578432") {
        if (msg.content.length >= 1) {

            const SentMessage = new EmbedBuilder()
                .setAuthor({ name: `Message Sent by: ${msg.author.tag}`, iconURL: `${msg.author.displayAvatarURL()}` })
                .setDescription(msg.content)
                .setColor("#2ECC71")
                .setImage(msg.image)
                .setTimestamp()

            bot.channels.fetch('1077894797806796841')
                .then(channel => {
                    channel.send({ embeds: [SentMessage] });
                });

            bot.channels.fetch('1079718474584166411')
                .then(channel => {
                    channel.send({ embeds: [SentMessage] });
                });

        } else {

            const SentMessage = new EmbedBuilder()
                .setAuthor({ name: `Message Sent by: ${msg.author.tag}`, iconURL: `${msg.author.displayAvatarURL()}` })
                .setColor("#2ECC71")
                .setImage(msg.image)
                .setTimestamp()

            bot.channels.fetch('1077894797806796841')
                .then(channel => {
                    channel.send({ embeds: [SentMessage] });
                });

            bot.channels.fetch('1079718474584166411')
                .then(channel => {
                    channel.send({ embeds: [SentMessage] });
                });

        };
    };

    ////////////////////////////////////////////
    /////////////// User Systems ///////////////
    ////////////////////////////////////////////

    try {

        let us_user_id = message.author.id
        let us_lv_xp = config.us.xp_per
        let us_minXP = config.us.msg_min
        let us_maxXP = config.us.msg_max

        const variables = GetJSONVariables(us_user_id);

        function us_read() {
            const rawData = fs.readFileSync('./database/users.json');
            return JSON.parse(rawData);
        };

        function us_write(users) {
            fs.writeFileSync('./database/users.json', JSON.stringify(users, null, 4));
        };

        function updateUserData(userId, newData) {
            let users = us_read();
            if (!users[userId]) return;

            for (const key in newData) {
                if (Object.hasOwnProperty.call(newData, key)) {
                    users[userId][key] = newData[key];
                };
            };

            us_write(users);
        };

        function us_new(us_user_id, variables) {
            let users = us_read();
            if (!users[us_user_id]) {
                users[us_user_id] = variables;
            } else { return }
            us_write(users);
        };

        async function CheckLevelUp(users, userId) {
            if (users[userId].Level >= config.us.max_level) {
                return users;
            };

            const requiredXP = Number(Math.floor((users[userId].Level * config.us.xp_per) * Math.pow(config.us.xp_growth, users[userId].Level)));

            if (users[userId].XP >= requiredXP) {
                users[userId].XP -= requiredXP;
                users[userId].Level += 1;
                users[userId].Coins += (users[userId].Level * 100)

                updateUserData(userId, {
                    XP: users[userId].XP,
                    Level: users[userId].Level,
                    Coins: users[userId].Coins,
                });

                BotLogs("SYSTEM", `\x1b[90m[\x1b[37m${message.author.tag}\x1b[90m] \x1b[93mreached \x1b[1m\x1b[37mLevel ${users[userId].Level}\x1b[93m! GG GUYS!`);
            };

            return users;
        };

        async function addXP(userId) {
            const xpToAdd = Math.floor(Math.random() * (us_maxXP - us_minXP + 1)) + us_minXP;
            let users = us_read();

            if (!users[userId] || users[userId].XP === undefined) {
                return;
            };

            users[userId].XP += Number((xpToAdd * users[userId].Multiplier).toFixed(2));
            updateUserData(userId, { XP: users[userId].XP });

            let oldLevel = users[userId].Level;
            let startLevel = oldLevel;
            let endLevel = oldLevel;

            while (true) {
                users = us_read();
                users = await CheckLevelUp(users, userId);

                if (oldLevel === users[userId].Level) {
                    break;
                };

                endLevel = users[userId].Level;
                oldLevel = users[userId].Level;
            };

            if (startLevel == endLevel) return;

            let levelUpDescription = `ยินดีด้วยมึงอัพเลเวลการพูดมากเป็น **Level ${startLevel} -> ${endLevel}**! แหลงมากจริงมึงนิ!`;

            const levelUpEmbed = new EmbedBuilder()
                .setAuthor({ name: `${message.author.tag}`, iconURL: `${message.author.displayAvatarURL()}` })
                .setThumbnail(message.author.displayAvatarURL())
                .setDescription(levelUpDescription)
                .setColor("#E4FF00")
                .setTimestamp()

            const mention = `<@${message.author.id}>`;
            try {
                const channel = await bot.channels.fetch('890270038525902940');
                const msg = await channel.send({ content: mention, embeds: [levelUpEmbed] });
                setTimeout(() => {
                    if (msg) {
                        msg.delete().catch(console.error);
                    }
                }, 25000);
            } catch (error) {
                console.error(error);
            };

        };

        us_new(us_user_id, variables);
        addXP(us_user_id);

    } catch (error) { };

    ////////////////////////////////////////////
    /////////////// User Systems ///////////////
    ////////////////////////////////////////////

});

bot.on('voiceStateUpdate', async (oldState, newState, message) => {

    let getID = ""

    if (newState.channel) {
        getID = newState.channel.guild.id
    } else {
        if (oldState.channel) {
            getID = oldState.channel.guild.id
        };
    };

    let server = bot.servers.get(getID);
    let tempGuild = bot.guilds.cache.get(getID);

    if (newState.member.user.tag) {
        UserTag = newState.member.user.tag
        UserID = newState.member.user.id
    };
    if (oldState.member.user.tag) {
        UserTag = oldState.member.user.tag
        UserID = oldState.member.user.id
    };

    const _botMember = tempGuild.members.cache.get("887531368836370483");

    if (!fs.existsSync(`./database/nick/${getID}.json`)) {
        let nick = {
            tts_channel: "",
            users: []
        };
        fs.writeFileSync(`./database/nick/${getID}.json`, JSON.stringify(nick, null, 2));
        bot.BotLogs("SYSTEM", `\x1b[35mCreated new guild's nickname for \x1b[90m[\x1b[37m${getID}.json\x1b[90m]`);
    };

    if (_botMember.voice) {
        if (_botMember.voice.channel?.members.size <= 1) {
            const isOnlyBot = _botMember.voice.channel.members.size === 1 && _botMember.voice.channel.members.has(_botMember.id);

            if (isOnlyBot) {
                if (_proc) {
                    _botMember.voice.setChannel(null).catch(err => { });
                    _proc = false
                };

                setTimeout(() => {
                    const voiceChannels = tempGuild.channels.cache.filter(channel => channel.type === 2);
                    voiceChannels.forEach(voiceChannel => {
                        if (voiceChannel.members.size >= 1 && !(voiceChannel.members.size === 1 && voiceChannel.members.has(_botMember.id))) {
                            if (voiceChannel.id == "542028952290721793" || voiceChannel.id == "954512531563618360") return;

                            const connection = joinVoiceChannel({
                                channelId: voiceChannel.id,
                                guildId: tempGuild.id,
                                adapterCreator: tempGuild.voiceAdapterCreator,
                            });
                            BotLogs(tempGuild.name, `\x1b[36mJoining Current Active VC \x1b[90m[\x1b[37m${voiceChannel.name}\x1b[90m]`);
                        };
                    });
                }, 500);

            }
        } else {
            _proc = true
        };
    };

    try {

        if (newState.channel) {
            if (newState.channel.id == tempGuild.afkChannelId) {
                newState.setChannel(oldState.channel);
                BotLogs(tempGuild.name, `\x1b[90m[\x1b[37m${newState.member.user.tag}\x1b[90m] \x1b[36mfound in AFK Channel! \x1b[90m[\x1b[37m${newState.channel.name}\x1b[90m]`);

                const AFKEmbed = new EmbedBuilder()
                    .setAuthor({ name: `AFK Detected from: ${newState.member.user.tag}`, iconURL: `${newState.member.user.displayAvatarURL()}` })
                    .setDescription(`Server Name: **${tempGuild.name}**\nOld Channel: **${oldState.channel.name}**\nNew Channel: **${newState.channel.name}**`)
                    .setColor("#6495ED")
                    .setTimestamp()

                bot.channels.fetch('1078286215859687494')
                    .then(channel => {
                        channel.send({ embeds: [AFKEmbed] });
                    });
                bot.channels.fetch('1079718609665929216')
                    .then(channel => {
                        channel.send({ embeds: [AFKEmbed] });
                    });

            };
        };

    } catch (error) {
        console.log(error)
    };

    try {

        let disable = true

        function func_25() {
            return Math.random() < 0.10;
        };
        function func_5() {
            return Math.random() < 0.025;
        };

        if (newState.channel) {
            if (disable == false) {
                if (newState.channel.guild.id == "467655562658578432") {
                    if (newState.member.user.id == "702125001377054801") {
                        console.log("stupid nat");
                        let target = newState.channel.guild.members.cache.get("702125001377054801");
                        if (func_25()) {
                            target.voice.setChannel(null)
                                .catch(err => { console.error(err); });
                            bot.channels.fetch('890270038525902940')
                                .then(channel => {
                                    channel.send("Ew Kon E San get lost man").then(message => {
                                        setTimeout(() => {
                                            message.delete().catch(console.error);
                                        }, 3000);
                                    });
                                })
                                .catch(console.error);
                            bot.BotLogs(newState.channel.guild.name, `\x1b[35mKicking KonESan from \x1b[37m"${newState.channel.name}"\x1b[35m!`)
                        } else {
                            clearInterval(anti_nat);
                            anti_nat = setInterval(() => {
                                if (func_5) {
                                    clearInterval(anti_nat);
                                    target.voice.setChannel(null)
                                        .catch(err => { console.error(err); });
                                    bot.channels.fetch('890270038525902940')
                                        .then(channel => {
                                            channel.send("Ew Kon E San get lost man").then(message => {
                                                setTimeout(() => {
                                                    message.delete().catch(console.error);
                                                }, 3000);
                                            });
                                        })
                                        .catch(console.error);
                                    bot.BotLogs(newState.channel.guild.name, `\x1b[35mKicking KonESan from \x1b[37m"${newState.channel.name}"\x1b[35m!`)
                                };
                            }, 120000)
                        }
                    } else {
                        if (newState.member.user.id == "1080788320063340595") {
                            console.log("stupid nat");
                            let target = newState.channel.guild.members.cache.get("1080788320063340595");
                            if (func_25()) {
                                target.voice.setChannel(null)
                                    .catch(err => { console.error(err); });
                                bot.channels.fetch('890270038525902940')
                                    .then(channel => {
                                        channel.send("Ew Kon E San get lost man").then(message => {
                                            setTimeout(() => {
                                                message.delete().catch(console.error);
                                            }, 3000);
                                        });
                                    })
                                    .catch(console.error);
                                bot.BotLogs(newState.channel.guild.name, `\x1b[35mKicking KonESan from \x1b[37m"${newState.channel.name}"\x1b[35m!`)
                            } else {
                                clearInterval(anti_nat);
                                anti_nat = setInterval(() => {
                                    if (func_5) {
                                        clearInterval(anti_nat);
                                        target.voice.setChannel(null)
                                            .catch(err => { console.error(err); });
                                        bot.channels.fetch('890270038525902940')
                                            .then(channel => {
                                                channel.send("Ew Kon E San get lost man").then(message => {
                                                    setTimeout(() => {
                                                        message.delete().catch(console.error);
                                                    }, 3000);
                                                });
                                            })
                                            .catch(console.error);
                                        bot.BotLogs(newState.channel.guild.name, `\x1b[35mKicking KonESan from \x1b[37m"${newState.channel.name}"\x1b[35m!`)
                                    };
                                }, 120000)
                            }
                        };
                    }
                };
            }
        };

        if (oldState.channel == null) {

            if (newState.member.user.id == "852653820566831145") {

                const megu = new EmbedBuilder()
                    .setTitle(`NozomiBot VC Announcer`)
                    .setDescription(`<@852653820566831145> มันเข้าดิสแล้วโว้ยยยยยยยย `)
                    .setColor("#370D62")
                    .setImage(`https://i.pinimg.com/originals/df/05/a1/df05a177c8f9311bd7439fe26d9a7acd.gif`)
                    .setFooter({ text: `NozomiBot Beta` });

                notifier.notify({
                    title: `[${tempGuild.name}]`,
                    message: `MegunaV5Hz joined Voice Channel!\nไอเหี้ยมูกหน้าหีเข้าดิสแล้ว EZ EZ EZ`,
                    icon: path.join(`D:/PanCakeBot/image`, 'icon.png'),
                    appID: "MeguBot Discord",
                    sound: true,
                    wait: false,
                }, function (err, response) {
                    if (err) {
                        console.error(`Notification Error: ${err}`);
                    }
                });

                bot.channels.fetch('890270038525902940')
                    .then(channel => {
                        channel.send(`<@&861101451648172072>`)
                            .then(msg => {
                                setTimeout(() => {
                                    if (msg) {
                                        msg.delete().catch(console.error);
                                    }
                                }, 10000);
                            })
                            .catch(console.error);
                        channel.send({ embeds: [megu] })
                            .then(msg => {
                                setTimeout(() => {
                                    if (msg) {
                                        msg.delete().catch(console.error);
                                    }
                                }, 10000);
                            })
                            .catch(console.error);
                        BotLogs(tempGuild.name, `\x1b[36mAnnounced ⛩️ Megunakami Detector! \x1b[90m[\x1b[37m${newState.channel.name}\x1b[90m]`)
                    });

            };

        };

    } catch (error) {
        BotLogs("SYSTEM", `\x1b[31m---------------------------------------------------------------`);
        BotLogs("SYSTEM", `\x1b[31mError Occurred: \x1b[37m"${error}"`);
        BotLogs("SYSTEM", `\x1b[31m---------------------------------------------------------------`);
    };

    if (newState.channel && oldState.channel == null) {

        if (newState.channel.id == tempGuild.afkChannelId) return

        if (newState.channel.id == "1205221580502736917") {

            const userRoles = newState.member.roles.cache
            const numberOfRoles = userRoles.size

            const hasSpecificRole = userRoles.some(role => role.id === '865108173337001984');

            if (hasSpecificRole && numberOfRoles <= 2) {

                try {

                    const botMember = newState.channel.guild.members.cache.get("887531368836370483");
                    const voiceChannel = botMember.voice.channel;

                    const connection = joinVoiceChannel({
                        channelId: voiceChannel.id,
                        guildId: newState.channel.guild.id,
                        adapterCreator: newState.channel.guild.voiceAdapterCreator,
                    });

                    config.old_vc_id = voiceChannel.id
                    fs.writeFileSync('./config.json', JSON.stringify(config, null, 4));

                    BotLogs(newState.channel.guild.name, `\x1b[90m[\x1b[37m${newState.member.user.tag}\x1b[90m] \x1b[36mjoined Waiting Room!`);

                    let queue_constructor = {
                        uuid: `${generateUUID()}`,
                        name: `เพื่อนใครมา ไปดึงด้วยไอกาก`,
                        lang: `th`,
                        type: "TTS",
                        guild: newState.channel.guild,
                        sender: newState.member.user,
                        voice_channel: voiceChannel,
                        connection: connection
                    }
                    addToQueue(newState.channel.guild.id, queue_constructor);

                } catch (error) { }

            };

            return;

        };

        if (server.first_join) {

            const connection = joinVoiceChannel({
                channelId: newState.channel.id,
                guildId: newState.channel.guild.id,
                adapterCreator: newState.channel.guild.voiceAdapterCreator,
            });

            const voiceChannel = newState.channel;

            config.old_vc_id = newState.channel.id
            fs.writeFileSync('./config.json', JSON.stringify(config, null, 4));

            BotLogs(tempGuild.name, `\x1b[36mGreeting VC \x1b[90m[\x1b[37m${newState.channel.name}\x1b[90m]`);

            let queue_constructor = {
                uuid: `${generateUUID()}`,
                name: `สวัสดีชาวโลก`,
                lang: `th`,
                type: "TTS",
                guild: newState.channel.guild,
                sender: newState.member.user,
                voice_channel: voiceChannel,
                connection: connection
            }
            addToQueue(newState.channel.guild.id, queue_constructor);

            server.first_join = false

        } else {

            try {

                if (newState.member.user.id == "887531368836370483") return

                const botMember = newState.channel.guild.members.cache.get("887531368836370483");
                const voiceChannel = botMember.voice.channel;

                const rawData = fs.readFileSync(`./database/nick/${newState.channel.guild.id}.json`);
                const jsonData = JSON.parse(rawData);
                let target = "ใครไม่รู้"
                for (const user of jsonData.users) {
                    if (newState.member.user.id === user.id) {
                        target = `${user.name}`;
                        break;
                    }
                };

                if (!voiceChannel) {
                    BotLogs(newState.channel.guild.name, `\x1b[36mNew Active VC joining \x1b[90m[\x1b[37m${newState.channel.name}\x1b[90m]`);
                };

                const connection = joinVoiceChannel({
                    channelId: newState.channel.id,
                    guildId: newState.channel.guild.id,
                    adapterCreator: newState.channel.guild.voiceAdapterCreator,
                });

                config.old_vc_id = newState.channel.id
                fs.writeFileSync('./config.json', JSON.stringify(config, null, 4));

                BotLogs(newState.channel.guild.name, `\x1b[90m[\x1b[37m${newState.member.user.tag}\x1b[90m] \x1b[36mjoined Voice Channel! \x1b[90m[\x1b[37m${newState.channel.name}\x1b[90m]`);

                let queue_constructor = {
                    uuid: `${generateUUID()}`,
                    name: `${target} เข้าดิสมา`,
                    lang: `th`,
                    type: "TTS",
                    guild: newState.channel.guild,
                    sender: newState.member.user,
                    voice_channel: voiceChannel,
                    connection: connection
                }
                addToQueue(newState.channel.guild.id, queue_constructor);

            } catch (error) {
                console.error(error);
            }

        }

    } else if (newState.channel == null && oldState.channel) {

        if (newState.member.user.id == "887531368836370483") {
            BotLogs(oldState.channel.guild.name, `\x1b[36mBot left Voice Channel! \x1b[90m[\x1b[37m${oldState.channel.name}\x1b[90m]`);
            return;
        };

        if (oldState.channel.id == "542028952290721793" || oldState.channel.id == "954512531563618360" || oldState.channel.id == "1205221580502736917") return

        try {

            const victims = getVictims();
            const _isVictim = victims.get(newState.member.user.id);
            if (_isVictim) {
                try {
                    victims.delete(newState.member.user.id)
                    bot.channels.fetch('890270038525902940')
                        .then(channel => {

                            const leave_vc = new EmbedBuilder()
                                .setTitle(`NozomiBot Murasaki`)
                                .setDescription(`ไอโง่ ${newState.member.user} โดนปัดเป่าเรียบร้อย!!!`)
                                .setColor("#9B59B6")
                                .setThumbnail(`${newState.member.user.displayAvatarURL()}`)

                            channel.send({ embeds: [leave_vc] })
                                .then(msg => {
                                    setTimeout(() => {
                                        if (msg) {
                                            msg.delete().catch(console.error);
                                        }
                                    }, 5000)
                                })
                                .catch(console.error);

                        });
                } catch (error) { }
                return;
            };

            const rawData = fs.readFileSync(`./database/nick/${oldState.channel.guild.id}.json`);
            const jsonData = JSON.parse(rawData);
            let target = "ใครไม่รู้";
            for (const user of jsonData.users) {
                if (oldState.member.user.id === user.id) {
                    target = `${user.name}`;
                    break;
                };
            };

            const botMember = tempGuild.members.cache.get("887531368836370483");
            const voiceChannel = botMember.voice.channel;

            const connection = joinVoiceChannel({
                channelId: oldState.channel.id,
                guildId: oldState.channel.guild.id,
                adapterCreator: oldState.channel.guild.voiceAdapterCreator,
            });

            config.old_vc_id = oldState.channel.id
            fs.writeFileSync('./config.json', JSON.stringify(config, null, 4));

            let textToSpeak = `${target}บิดไปแล้ว`;
            if (botMember) {
                if (botMember.voice.channel) {

                    if (botMember.voice.channel.members.size < 3) {
                        textToSpeak = `มึงโดนเหลี่ยมแล้ว ว้ายยยยยยย`
                    } else {
                        textToSpeak = `${target}บิดไปแล้ว`
                    };

                };
            };

            BotLogs(oldState.channel.guild.name, `\x1b[90m[\x1b[37m${oldState.member.user.tag}\x1b[90m] \x1b[36mleft Voice Channel! \x1b[90m[\x1b[37m${oldState.channel.name}\x1b[90m]`);

            if (botMember) {
                if (botMember.voice.channel) {

                    if (botMember.voice.channel.members.size <= 1) {
                        notifier.notify({
                            title: `[${tempGuild.name}]`,
                            message: `No Active Voice Channel!\nI left from the Voice Channel!`,
                            icon: path.join(`D:/PanCakeBot/image`, 'icon.png'),
                            appID: "MeguBot Discord",
                            sound: true,
                            wait: false,
                        }, function (err, response) {
                            if (err) {
                                console.error(`Notification Error: ${err}`);
                            }
                        });
                        clearQueue(oldState.channel.guild.id);
                        botMember.voice.setChannel(null)
                            .catch(err => { })
                        return
                    };

                };
            };

            let queue_constructor = {
                uuid: `${generateUUID()}`,
                name: `${textToSpeak}`,
                lang: `th`,
                type: "TTS",
                guild: oldState.channel.guild,
                sender: oldState.member.user,
                voice_channel: voiceChannel,
                connection: connection
            }
            addToQueue(oldState.channel.guild.id, queue_constructor);

        } catch (error) {
            console.error(error);
        }

    }

    ///////////////////// Presence Detector /////////////////////

    try {

        if (newState.streaming && !oldState.streaming) {

            const botMember = newState.channel.guild.members.cache.get("887531368836370483");
            const voiceChannel = botMember.voice.channel;

            const rawData = fs.readFileSync(`./database/nick/${newState.channel.guild.id}.json`);
            const jsonData = JSON.parse(rawData);
            let target = "ใครไม่รู้"
            for (const user of jsonData.users) {
                if (oldState.member.user.id === user.id) {
                    target = `${user.name}`;
                    break;
                };
            };

            const connection = joinVoiceChannel({
                channelId: newState.channel.id,
                guildId: newState.channel.guild.id,
                adapterCreator: newState.channel.guild.voiceAdapterCreator,
            });

            config.old_vc_id = newState.channel.id;
            fs.writeFileSync('./config.json', JSON.stringify(config, null, 4));

            BotLogs(oldState.channel.guild.name, `\x1b[90m[\x1b[37m${oldState.member.user.tag}\x1b[90m] \x1b[36msharing their screen! \x1b[90m[\x1b[37m${oldState.channel.name}\x1b[90m]`);

            let queue_constructor = {
                uuid: `${generateUUID()}`,
                name: `${target}ได้ทำการแชร์จอ`,
                lang: `th`,
                type: "TTS",
                guild: newState.channel.guild,
                sender: newState.member.user,
                voice_channel: voiceChannel,
                connection: connection
            }
            addToQueue(newState.channel.guild.id, queue_constructor);

        };

    } catch (error) {
        console.error(error);
    };

    ///////////////////// Presence Detector /////////////////////

});

bot.login(process.env.DISCORD_TOKEN)
exports.bot = bot