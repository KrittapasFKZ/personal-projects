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

            const TestEmbed = new EmbedBuilder()
                .setAuthor({ name: `${message.author.tag}'s Dungeon`, iconURL: `${message.author.displayAvatarURL()}` })
                .setThumbnail(`https://images-wixmp-ed30a86b8c4ca887773594c2.wixmp.com/f/03c61bf1-2b1c-4a2f-bb3e-f16dea168d7a/dephfae-a49d5d4b-fb1e-45ae-b132-e45f00f81880.gif?token=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1cm46YXBwOjdlMGQxODg5ODIyNjQzNzNhNWYwZDQxNWVhMGQyNmUwIiwiaXNzIjoidXJuOmFwcDo3ZTBkMTg4OTgyMjY0MzczYTVmMGQ0MTVlYTBkMjZlMCIsIm9iaiI6W1t7InBhdGgiOiJcL2ZcLzAzYzYxYmYxLTJiMWMtNGEyZi1iYjNlLWYxNmRlYTE2OGQ3YVwvZGVwaGZhZS1hNDlkNWQ0Yi1mYjFlLTQ1YWUtYjEzMi1lNDVmMDBmODE4ODAuZ2lmIn1dXSwiYXVkIjpbInVybjpzZXJ2aWNlOmZpbGUuZG93bmxvYWQiXX0.oG22yuZd1JxdOC1cb5FpueSew_uPhlyFKTqH4tTGVGA`)
                .addFields(
                    { name: '**Your Party**', value: `Average Level: \`💠 0\`\nUnit Amount: \`0/4\`\nPhysical Power: \`💥 0\`\nMagical Power: \`💫 0\`\n` },
                    { name: `**Option**`, value: `Click ❌ to hide this message.\nClick ⚔️ to Start Dungeon.\nClick 👥 to Edit Party.` }
                )
                .setColor("#C0392B")
                .setTimestamp()

            return message.reply({ embeds: [TestEmbed] })
                .then(msg => {
                    setTimeout(() => {
                        if (msg) {
                            msg.delete().catch(console.error);
                            message.delete();
                        }
                    }, 30000);
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
    name: 'embed',
    aliases: ['emb', 'testembed', 'test_embed']
};