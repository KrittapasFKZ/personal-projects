const config = require('../config.json');
const path = require('path');
const fs = require("fs");

exports.run = async (bot, message, args) => {
    let now = new Date(Date.now());
    let now_hours = now.getHours().toString().padStart(2, '0');
    let now_mins = now.getMinutes().toString().padStart(2, '0');
    let now_seconds = now.getSeconds().toString().padStart(2, '0');

    function formatDate(date) {
        const options = { year: 'numeric', month: 'short', day: 'numeric', hour: 'numeric', minute: 'numeric', second: 'numeric' };
        return date.toLocaleDateString('en-US', options);
    };

    try {

        if (message.author.id == "605361556297089035") {
            message.delete();

            bot.BotLogs("SYSTEM", `\x1b[33mShutting down...`);

            async function SaveVoiceChannel() {
                return new Promise((resolve, reject) => {
                    const botMember = message.guild.members.cache.get("887531368836370483");
                    const voiceChannel = botMember.voice.channel;
                    let lastRestartTime = new Date()
                    let lastrt = formatDate(lastRestartTime)
                    config.rt_time = lastrt.toString()
                    if (voiceChannel) {
                        config.old_vc_id = voiceChannel.id
                    };
                    fs.writeFileSync('../config.json', JSON.stringify(config, null, 4));
                    resolve(lastrt);
                });
            };

            await SaveVoiceChannel()

            bot.BotLogs("SYSTEM", `\x1b[33mRestarting...`);

            bot.destroy();
            setTimeout(() => {
                process.exit();
            }, 2000);
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
    name: 'destroy',
    aliases: ['dc', 're', 'restart']
};