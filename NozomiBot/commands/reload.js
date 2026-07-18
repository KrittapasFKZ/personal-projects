const { EmbedBuilder } = require('discord.js');
const config = require('../config.json');
const path = require('path');
const fs = require("fs");

exports.run = async (bot, message, args) => {
    let now = new Date(Date.now());
    let now_hours = now.getHours().toString().padStart(2, '0');
    let now_mins = now.getMinutes().toString().padStart(2, '0');
    let now_seconds = now.getSeconds().toString().padStart(2, '0');

    try {

        if (message.author.id == "605361556297089035") {

            if (!args[0]) {
                return message.reply(`Unknown file name!`)
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

            let target = args[0].toLowerCase(); 

            if (!fs.existsSync(`./commands/${target}.js`)) {
                return message.reply(`Command file doesn't exist!`)
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
 
            let commandName = args[0].toLowerCase()

            delete require.cache[require.resolve(`./${commandName}.js`)]
            bot.commands.delete(commandName)
            const pull = require(`./${commandName}.js`)
            bot.commands.set(commandName, pull)

            const EventEmbed_Completed = new EmbedBuilder()
                .setTitle(`Action Completed!`)
                .setDescription(`✅ **${commandName}.js** has been reloaded!`)
                .setColor('#00FF00')
                .setTimestamp()
                
            bot.BotLogs("SYSTEM", `\x1b[32m\x1b[90m[\x1b[37m${commandName}.js\x1b[90m] \x1b[32mhas been reloaded!`);

            return message.reply({ embeds: [EventEmbed_Completed] })
                .then(msg => {
                    setTimeout(() => {
                        if (msg) {
                            msg.delete().catch(console.error);
                            message.delete();
                        }
                    }, 3000);
                })
                .catch(console.error);

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
    name: 'reload',
    aliases: ['rl']
};