const { createAudioPlayer, createAudioResource, joinVoiceChannel, getVoiceConnection } = require('@discordjs/voice');
const config = require('./config.json');
const fs = require("fs");
const path = require('path');
const server_port = process.env.SERVER_PORT
 
////////////////////////////////////////////////
////////////////// Default /////////////////////
//////////////////////////////////////////////// 

let checkLoggedDate = false

function limitString(str) {
    return str.substring(0, 10).padEnd(10, ' ');
};

function checkDate() {
    if (checkLoggedDate) return
    let now = new Date();
    let options = { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' };
    let currentDate = now.toLocaleDateString('en-GB', options);

    if (currentDate !== config.lastLoggedDate) {
        checkLoggedDate = true
        BotLogs("SYSTEM", `\x1b[91m---------------------------------------------------------------`);
        BotLogs("SYSTEM", `\x1b[91mStarting a New Day: 📅 \x1b[97m${currentDate}`);
        BotLogs("SYSTEM", `\x1b[91m---------------------------------------------------------------`);
        config.lastLoggedDate = currentDate;
        fs.writeFileSync('./config.json', JSON.stringify(config, null, 4));
        checkLoggedDate = false
    };
};

checkDate();

////////////////////////////////////////////////
///////////////////// Main /////////////////////
//////////////////////////////////////////////// 

function getServerStatus(bot) {
    let active_servers = 0
    let inactive_servers = 0
    let total_servers = 0
    for (let [key, value] of bot.servers) {
        let tempGuild = bot.guilds.cache.get(value.id);
        const botMember = tempGuild.members.cache.get("887531368836370483");
        if (botMember) total_servers += 1;
        if (botMember.voice.channel) active_servers += 1;
    };
    inactive_servers = total_servers - active_servers
    BotLogs("SYSTEM", `\x1b[90mGathering info...`);

    setTimeout(() => {
        BotLogs("SYSTEM", `\x1b[96m---------------------------------------------------------------`);
        BotLogs("SYSTEM", `\x1b[96mTotal Guild: \x1b[93m⬤ ${total_servers} servers`);
        if (active_servers <= 0) {
            BotLogs("SYSTEM", `\x1b[96mCurrent Status: \x1b[31m⬤ ${inactive_servers} Offline servers`);
        } else {
            if (inactive_servers <= 0) {
                BotLogs("SYSTEM", `\x1b[96mCurrent Status: \x1b[92m⬤ ${active_servers} Active servers`);
            } else {
                BotLogs("SYSTEM", `\x1b[96mCurrent Status: \x1b[92m⬤ ${active_servers} Active servers \x1b[31m⬤ ${inactive_servers} Offline servers`);
            };
        };
        BotLogs("SYSTEM", `\x1b[96m---------------------------------------------------------------`);
    }, 500);

};
 
function BotLogs(host, msg) {
    checkDate();
    let now = new Date(Date.now());
    let hours = now.getHours();
    let period = hours >= 12 ? 'PM' : 'AM';
    let now_hours = now.getHours().toString().padStart(2, '0');
    let now_mins = now.getMinutes().toString().padStart(2, '0');
    let now_seconds = now.getSeconds().toString().padStart(2, '0');
    if (host == "SYSTEM") { 
        console.log(`\x1b[90m[\x1b[0m${now_hours}:${now_mins}:${now_seconds}\x1b[90m|\x1b[97mMain\x1b[90m] ${msg}`, "\x1b[0m");
    } else {
        if (host == "SERVER") {
            console.log(`\x1b[90m[\x1b[0m${now_hours}:${now_mins}:${now_seconds}\x1b[90m|\x1b[97mMain\x1b[90m] ${msg}`, "\x1b[0m");
        } else {
            let new_host = limitString(host);
            console.log(`\x1b[90m[\x1b[0m${now_hours}:${now_mins}:${now_seconds}\x1b[90m|\x1b[97m${new_host}\x1b[90m] ${msg}`, "\x1b[0m");
        };
    };
};

exports.BotLogs = BotLogs;
exports.getServerStatus = getServerStatus;