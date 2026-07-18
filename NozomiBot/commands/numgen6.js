const { EmbedBuilder } = require('discord.js');
const config = require('../config.json');
const path = require('path');

exports.run = async (bot, message, args) => {

    try {

        const TestEmbed = new EmbedBuilder()
            .setAuthor({ name: `MeguBot Commands`, iconURL: `${bot.user.displayAvatarURL()}` })
            .setDescription("Number Generator 0-99999")
            .setColor('#008cff')
            .setTimestamp()

        for (let i = 0; i < 5; i++) {
            TestEmbed.addFields({ name: ` `, value: `\`\`\`${Math.floor(Math.random() * 1000000).toString().padStart(6, '0')}\`\`\`` });
        };

        try { message.delete() } catch (error) { }
        let mention = `<@${message.author.id}>`;
        const MainEmbed = await message.channel.send({ content: mention, embeds: [TestEmbed] });

    } catch (error) {
        bot.BotLogs("SYSTEM", `\x1b[31m---------------------------------------------------------------`);
        bot.BotLogs("SYSTEM", `\x1b[31mError Occurred: \x1b[37m"${error}" \x1b[31mfrom \x1b[37m"${path.basename(__filename)}"`);
        bot.BotLogs("SYSTEM", `\x1b[31m---------------------------------------------------------------`);
    }

};

exports.help = {
    name: 'numgen6',
    aliases: ['gen6', 'num6']
};