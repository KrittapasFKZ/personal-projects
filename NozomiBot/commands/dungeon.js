const { EmbedBuilder } = require('discord.js');
const config = require('../config.json');
const path = require('path');
const fs = require("fs");
const { clearInterval } = require('timers');
const { error } = require('console');

let talkedRecently = new Set();

exports.run = async (bot, message, args) => {
    let now = new Date(Date.now());
    let now_hours = now.getHours().toString().padStart(2, '0');
    let now_mins = now.getMinutes().toString().padStart(2, '0');
    let now_seconds = now.getSeconds().toString().padStart(2, '0');

    let DungeonGif = `https://images-wixmp-ed30a86b8c4ca887773594c2.wixmp.com/f/03c61bf1-2b1c-4a2f-bb3e-f16dea168d7a/dephfae-a49d5d4b-fb1e-45ae-b132-e45f00f81880.gif?token=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1cm46YXBwOjdlMGQxODg5ODIyNjQzNzNhNWYwZDQxNWVhMGQyNmUwIiwiaXNzIjoidXJuOmFwcDo3ZTBkMTg4OTgyMjY0MzczYTVmMGQ0MTVlYTBkMjZlMCIsIm9iaiI6W1t7InBhdGgiOiJcL2ZcLzAzYzYxYmYxLTJiMWMtNGEyZi1iYjNlLWYxNmRlYTE2OGQ3YVwvZGVwaGZhZS1hNDlkNWQ0Yi1mYjFlLTQ1YWUtYjEzMi1lNDVmMDBmODE4ODAuZ2lmIn1dXSwiYXVkIjpbInVybjpzZXJ2aWNlOmZpbGUuZG93bmxvYWQiXX0.oG22yuZd1JxdOC1cb5FpueSew_uPhlyFKTqH4tTGVGA`

    try {

        if (config.dungeon_status == false && message.author.id != "605361556297089035") {
            const EventEmbed_Failed = new EmbedBuilder()
                .setTitle(`Interaction Denied!`)
                .setDescription(`❌ ตอนนี้ระบบถูกปิดใช้งาน!`)
                .setColor('#FF0000')
                .setTimestamp()

            return message.reply({ embeds: [EventEmbed_Failed] })
                .then(msg => {
                    setTimeout(() => {
                        if (msg) {
                            msg.delete().catch(console.error);
                            try { message.delete() } catch (error) { };
                        }
                    }, 3000);
                })
                .catch(console.error);

        };

        if (talkedRecently.has(message.author.id)) {
            const EventEmbed_Failed = new EmbedBuilder()
                .setTitle(`Interaction Denied!`)
                .setDescription(`❌ ตอนนี้คุณกำลังใช้งานอยู่!`)
                .setColor('#FF0000')
                .setTimestamp()

            return message.reply({ embeds: [EventEmbed_Failed] })
                .then(msg => {
                    setTimeout(() => {
                        if (msg) {
                            msg.delete().catch(console.error);
                            try { message.delete() } catch (error) { };
                        }
                    }, 3000);
                })
                .catch(console.error);

        };

        function comma(x) {
            return x.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
        };

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

        function checkLevel(unit) {
            unit.Attributes.Health_Value = Math.floor(Number((unit.Base.Base_Health) + (((unit.Base.Base_Health * unit.Unit_Level) * 55) / 100)));
            unit.Attributes.Physical_Value = Math.floor(Number((unit.Base.Base_Physical) + (((unit.Base.Base_Physical * unit.Unit_Level) * 20) / 100)));
            unit.Attributes.Magical_Value = Math.floor(Number((unit.Base.Base_Magical) + (((unit.Base.Base_Magical * unit.Unit_Level) * 20) / 100)));
        };

        let userId = message.author.id
        let users = us_read();

        if (users[userId]) {

            message.delete()
            let mention = `<@${message.author.id}>`;

            talkedRecently.add(message.author.id);

            const EventEmbed_Load = new EmbedBuilder()
                .setTitle(`Loading...`)
                .setColor('#FFFF00')
                .setTimestamp()

            const MainEmbed = await message.channel.send({ content: mention, embeds: [EventEmbed_Load] });

            async function DungeonEvent_UpdateUnit() {

                function levelUpUnit(unit) {
                    while (true) {
                        let temp_max_xp = Number((unit.Unit_Level * 200) + (((unit.Unit_Level * 200) * 125) / 100));
                        if (unit.Unit_Level >= 40) break
                        if (unit.Unit_XP >= temp_max_xp) {
                            unit.Unit_Level += 1;
                            unit.Unit_XP -= temp_max_xp;
                        } else {
                            break;
                        }
                    }
                    checkLevel(unit);
                }

                let DUNGEON_UNIT_LIST = users[userId].Dungeon.Unit;
                let DUNGEON_PARTY_LIST = users[userId].Dungeon.Party;

                DUNGEON_PARTY_LIST.forEach(partyUnit => {
                    if (DUNGEON_PARTY_LIST.length <= 0) return
                    levelUpUnit(partyUnit);

                    let mainUnit = DUNGEON_UNIT_LIST.find(unit => unit.Unit_Name === partyUnit.Unit_Name);

                    if (mainUnit) {
                        mainUnit.Unit_Level = partyUnit.Unit_Level;
                        mainUnit.Unit_XP = partyUnit.Unit_XP;
                    };
                });

                users = us_read();
                updateUserData(userId, { Dungeon: { ...users[userId].Dungeon, Unit: users[userId].Dungeon.Unit = DUNGEON_UNIT_LIST } });
                us_write(users);

                users = us_read();
                updateUserData(userId, { Dungeon: { ...users[userId].Dungeon, Party: users[userId].Dungeon.Party = DUNGEON_PARTY_LIST } });
                us_write(users);

            };

            async function DungeonPage_Main() {

                let DUNGEON_COMPLETION = users[userId].Dungeon.Completion
                let DUNGEON_UNIT_LIST = users[userId].Dungeon.Unit
                let DUNGEON_PARTY_LIST = users[userId].Dungeon.Party

                let DUNGEON_AVERAGE_LEVEL = 0
                let DUNGEON_UNIT_AMOUNT = DUNGEON_PARTY_LIST.length
                let DUNGEON_PHYSICAL_POWER = 0
                let DUNGEON_MAGICAL_POWER = 0

                ////////////////////////////////////////////////

                let DUNGEON_TEMP_ICON_UNIT = ``
                let DUNGEON_TEMP_LEVEL = 0
                let DUNGEON_TEMP_PHYSICAL = 0
                let DUNGEON_TEMP_MAGICAL = 0

                DUNGEON_PARTY_LIST.forEach(unit => {
                    DUNGEON_TEMP_LEVEL += unit.Unit_Level
                    DUNGEON_TEMP_PHYSICAL += unit.Attributes.Physical_Value
                    DUNGEON_TEMP_MAGICAL += unit.Attributes.Magical_Value
                });

                if (DUNGEON_UNIT_AMOUNT == 0) {
                    DUNGEON_UNIT_AMOUNT = 0
                } else {
                    DUNGEON_AVERAGE_LEVEL = Math.floor(DUNGEON_TEMP_LEVEL / DUNGEON_UNIT_AMOUNT)
                };

                DUNGEON_PHYSICAL_POWER = DUNGEON_TEMP_PHYSICAL
                DUNGEON_MAGICAL_POWER = DUNGEON_TEMP_MAGICAL

                ////////////////////////////////////////////////

                const StartEmbed = new EmbedBuilder()
                    .setAuthor({ name: `${message.author.tag}'s Dungeon - Main`, iconURL: `${message.author.displayAvatarURL()}` })
                    .setThumbnail(DungeonGif)
                    .addFields(
                        { name: '**Party Stats**', value: `Average Level: \`💠 ${DUNGEON_AVERAGE_LEVEL}\`\nUnit Amount: \`👥 ${DUNGEON_UNIT_AMOUNT}/4\`\nPhysical Power: \`💪 ${DUNGEON_PHYSICAL_POWER}\`\nMagical Power: \`💫 ${DUNGEON_MAGICAL_POWER}\`\n` },
                    )
                    .setColor("#C0392B")
                    .setTimestamp()
                    .setFooter({ text: `Click ❌ to hide this message.\nClick ⚔️ to Select Dungeon.\nClick 👥 to Edit Party.` })

                DUNGEON_PARTY_LIST.forEach(unit => {
                    if (unit.Unit_Class == 'Warrior') {
                        DUNGEON_TEMP_ICON_UNIT = `⚔️`
                    } else {
                        if (unit.Unit_Class == 'Gunner') {
                            DUNGEON_TEMP_ICON_UNIT = `🔫`
                        } else {
                            if (unit.Unit_Class == 'Wizard') {
                                DUNGEON_TEMP_ICON_UNIT = `🪄`
                            } else {
                                if (unit.Unit_Class == 'Priest') {
                                    DUNGEON_TEMP_ICON_UNIT = `💉`
                                } else {

                                }
                            }
                        }
                    };
                    StartEmbed.addFields(
                        { name: `**${DUNGEON_TEMP_ICON_UNIT} ${unit.Unit_Name} (Lv. ${unit.Unit_Level})**`, value: ` ` },
                    )
                });

                MainEmbed.reactions.removeAll().catch(error => console.error('Failed to clear reactions: ', error));
                await MainEmbed.edit({ embeds: [StartEmbed] })
                await MainEmbed.react('❌');
                await MainEmbed.react('⚔️');
                await MainEmbed.react('👥');

                let MessageTimeOut = setTimeout(async () => {
                    if (talkedRecently.has(message.author.id)) {
                        talkedRecently.delete(message.author.id);
                    };
                    collector.stop()
                    MainEmbed.delete();
                }, 60000);

                const filter = (reaction, user) => {
                    return user.id === message.author.id;
                };
                const collector = MainEmbed.createReactionCollector({
                    filter
                });

                collector.on('collect', async (reaction, user) => {
                    switch (reaction.emoji.name) {
                        case "❌":
                            if (user.tag === message.author.tag) {
                                clearTimeout(MessageTimeOut);
                                if (talkedRecently.has(message.author.id)) {
                                    talkedRecently.delete(message.author.id);
                                };
                                collector.stop()
                                MainEmbed.delete();
                                break;
                            };
                        case "⚔️":
                            if (user.tag === message.author.tag) {
                                if (DUNGEON_PARTY_LIST.length <= 0) return reaction.users.remove(user);
                                clearTimeout(MessageTimeOut);
                                collector.stop()
                                await DungeonPage_DungeonSelector();
                                break;
                            };
                        case "👥":
                            if (user.tag === message.author.tag) {
                                clearTimeout(MessageTimeOut);
                                collector.stop()
                                await DungeonPage_Edit();
                                break;
                            };
                    }
                });

            };

            async function DungeonPage_DungeonSelector() {

                let DUNGEON_COMPLETION = users[userId].Dungeon.Completion
                let DUNGEON_UNIT_LIST = users[userId].Dungeon.Unit
                let DUNGEON_PARTY_LIST = users[userId].Dungeon.Party

                let DUNGEON_AVERAGE_LEVEL = 0
                let DUNGEON_UNIT_AMOUNT = DUNGEON_PARTY_LIST.length
                let DUNGEON_PHYSICAL_POWER = 0
                let DUNGEON_MAGICAL_POWER = 0

                ////////////////////////////////////////////////

                let DUNGEON_TEMP_ICON_UNIT = ``
                let DUNGEON_TEMP_LEVEL = 0
                let DUNGEON_TEMP_PHYSICAL = 0
                let DUNGEON_TEMP_MAGICAL = 0

                DUNGEON_PARTY_LIST.forEach(unit => {
                    DUNGEON_TEMP_LEVEL += unit.Unit_Level
                    DUNGEON_TEMP_PHYSICAL += unit.Attributes.Physical_Value
                    DUNGEON_TEMP_MAGICAL += unit.Attributes.Magical_Value
                });

                if (DUNGEON_UNIT_AMOUNT == 0) {
                    DUNGEON_UNIT_AMOUNT = 0
                } else {
                    DUNGEON_AVERAGE_LEVEL = Math.floor(DUNGEON_TEMP_LEVEL / DUNGEON_UNIT_AMOUNT)
                };

                DUNGEON_PHYSICAL_POWER = DUNGEON_TEMP_PHYSICAL
                DUNGEON_MAGICAL_POWER = DUNGEON_TEMP_MAGICAL

                ////////////////////////////////////////////////

                let DUNGEON_INSTANCE_LIST_PAGE = 1
                let DUNGEON_INSTANCE_LIST_NAME = `Magical Forest`
                let DUNGEON_INSTANCE_LIST_TEXT = `**⚔️ ${DUNGEON_INSTANCE_LIST_NAME}**\nDungeon Level: \`💠 1-10\`\nTotal Room: \`🚪 3\`\n`

                async function UpdatePage() {
                    if (DUNGEON_INSTANCE_LIST_PAGE == 1) {
                        DUNGEON_INSTANCE_LIST_NAME = `Magical Forest`
                        DUNGEON_INSTANCE_LIST_TEXT = `**⚔️ ${DUNGEON_INSTANCE_LIST_NAME}**\nDungeon Level: \`💠 1-10\`\nTotal Room: \`🚪 3\`\n`
                    } else {
                        if (DUNGEON_INSTANCE_LIST_PAGE == 2) {
                            DUNGEON_INSTANCE_LIST_NAME = `Frozen Tomb`
                            DUNGEON_INSTANCE_LIST_TEXT = `**⚔️ ${DUNGEON_INSTANCE_LIST_NAME}**\nDungeon Level: \`💠 11-20\`\nTotal Room: \`🚪 5\`\n`
                        } else {
                            if (DUNGEON_INSTANCE_LIST_PAGE == 3) {
                                DUNGEON_INSTANCE_LIST_NAME = `Serpent's Labyrinth`
                                DUNGEON_INSTANCE_LIST_TEXT = `**⚔️ ${DUNGEON_INSTANCE_LIST_NAME}**\nDungeon Level: \`💠 21-30\`\nTotal Room: \`🚪 7\`\n`
                            } else {
                                if (DUNGEON_INSTANCE_LIST_PAGE == 4) {
                                    DUNGEON_INSTANCE_LIST_NAME = `Eldritch Depths`
                                    DUNGEON_INSTANCE_LIST_TEXT = `**⚔️ ${DUNGEON_INSTANCE_LIST_NAME}**\nDungeon Level: \`💠 31-40\`\nTotal Room: \`🚪 9\`\n`
                                } else {

                                }
                            }
                        }
                    }

                    const StartEmbed = new EmbedBuilder()
                        .setAuthor({ name: `${message.author.tag}'s Dungeon - Dungeon Selector`, iconURL: `${message.author.displayAvatarURL()}` })
                        .setThumbnail(DungeonGif)
                        .addFields(
                            { name: '**Party Stats**', value: `Average Level: \`💠 ${DUNGEON_AVERAGE_LEVEL}\`\nUnit Amount: \`👥 ${DUNGEON_UNIT_AMOUNT}/4\`\nPhysical Power: \`💪 ${DUNGEON_PHYSICAL_POWER}\`\nMagical Power: \`💫 ${DUNGEON_MAGICAL_POWER}\`\n` },
                        )
                        .setColor("#C0392B")
                        .setTimestamp()
                        .setFooter({ text: `Click ◀ to go back.\nClick ⚔️ to Attack.\nClick ⬅️ or ➡️ to go change page.` })

                    StartEmbed.addFields({ name: `**Dungeon Details - Page ${DUNGEON_INSTANCE_LIST_PAGE}**`, value: `${DUNGEON_INSTANCE_LIST_TEXT}` })

                    await MainEmbed.edit({ embeds: [StartEmbed] })
                };

                MainEmbed.reactions.removeAll().catch(error => console.error('Failed to clear reactions: ', error));
                await UpdatePage();
                await MainEmbed.react('◀');
                await MainEmbed.react('⚔️');
                await MainEmbed.react('⬅️');
                await MainEmbed.react('➡️');

                let MessageTimeOut = setTimeout(async () => {
                    if (talkedRecently.has(message.author.id)) {
                        talkedRecently.delete(message.author.id);
                    };
                    collector.stop()
                    MainEmbed.delete();
                }, 60000);

                const filter = (reaction, user) => {
                    return user.id === message.author.id;
                };
                const collector = MainEmbed.createReactionCollector({
                    filter
                });

                collector.on('collect', async (reaction, user) => {
                    switch (reaction.emoji.name) {
                        case "◀":
                            if (user.tag === message.author.tag) {
                                clearTimeout(MessageTimeOut);
                                collector.stop();
                                await DungeonPage_Main();
                                break;
                            };
                        case "⚔️":
                            if (user.tag === message.author.tag) {
                                clearTimeout(MessageTimeOut);
                                collector.stop();
                                await DungeonPage_Instance(DUNGEON_INSTANCE_LIST_NAME)
                                break;
                            };
                        case "⬅️":
                            if (user.tag === message.author.tag) {
                                DUNGEON_INSTANCE_LIST_PAGE -= 1
                                if (DUNGEON_INSTANCE_LIST_PAGE < 1) { DUNGEON_INSTANCE_LIST_PAGE = 4 };
                                await UpdatePage();
                                reaction.users.remove(user);
                                break;
                            };
                        case "➡️":
                            if (user.tag === message.author.tag) {
                                DUNGEON_INSTANCE_LIST_PAGE += 1
                                if (DUNGEON_INSTANCE_LIST_PAGE > 4) { DUNGEON_INSTANCE_LIST_PAGE = 1 };
                                await UpdatePage();
                                reaction.users.remove(user);
                                break;
                            };
                    }
                });

            };

            async function DungeonPage_Edit() {

                let DUNGEON_COMPLETION = users[userId].Dungeon.Completion
                let DUNGEON_UNIT_LIST = users[userId].Dungeon.Unit
                let DUNGEON_PARTY_LIST = users[userId].Dungeon.Party

                let DUNGEON_AVERAGE_LEVEL = 0
                let DUNGEON_UNIT_AMOUNT = DUNGEON_PARTY_LIST.length
                let DUNGEON_PHYSICAL_POWER = 0
                let DUNGEON_MAGICAL_POWER = 0

                ////////////////////////////////////////////////

                let DUNGEON_TEMP_ICON_UNIT = ``
                let DUNGEON_TEMP_LEVEL = 0
                let DUNGEON_TEMP_PHYSICAL = 0
                let DUNGEON_TEMP_MAGICAL = 0

                DUNGEON_PARTY_LIST.forEach(unit => {
                    DUNGEON_TEMP_LEVEL += unit.Unit_Level
                    DUNGEON_TEMP_PHYSICAL += unit.Attributes.Physical_Value
                    DUNGEON_TEMP_MAGICAL += unit.Attributes.Magical_Value
                });

                if (DUNGEON_UNIT_AMOUNT == 0) {
                    DUNGEON_UNIT_AMOUNT = 0
                } else {
                    DUNGEON_AVERAGE_LEVEL = Math.floor(DUNGEON_TEMP_LEVEL / DUNGEON_UNIT_AMOUNT)
                };

                DUNGEON_PHYSICAL_POWER = DUNGEON_TEMP_PHYSICAL
                DUNGEON_MAGICAL_POWER = DUNGEON_TEMP_MAGICAL

                ////////////////////////////////////////////////

                let DUNGEON_UNIT_LIST_PAGE = 0
                let DUNGEON_PARTY_LIST_TEXT = ` `
                let DUNGEON_INFO_UNIT_TEXT = ` `
                let DUNGEON_INFO_UNIT_TYPE = ` `

                async function UpdateEmbed_Party() {

                    DungeonEvent_UpdateUnit();

                    let DUNGEON_COMPLETION = users[userId].Dungeon.Completion
                    let DUNGEON_UNIT_LIST = users[userId].Dungeon.Unit
                    let DUNGEON_PARTY_LIST = users[userId].Dungeon.Party

                    let DUNGEON_AVERAGE_LEVEL = 0
                    let DUNGEON_UNIT_AMOUNT = DUNGEON_PARTY_LIST.length
                    let DUNGEON_PHYSICAL_POWER = 0
                    let DUNGEON_MAGICAL_POWER = 0

                    ////////////////////////////////////////////////

                    let DUNGEON_TEMP_ICON_UNIT = ``
                    let DUNGEON_TEMP_LEVEL = 0
                    let DUNGEON_TEMP_PHYSICAL = 0
                    let DUNGEON_TEMP_MAGICAL = 0

                    DUNGEON_PARTY_LIST.forEach(unit => {
                        if (DUNGEON_PARTY_LIST.length < 1) return
                        DUNGEON_TEMP_LEVEL += unit.Unit_Level
                        DUNGEON_TEMP_PHYSICAL += unit.Attributes.Physical_Value
                        DUNGEON_TEMP_MAGICAL += unit.Attributes.Magical_Value
                    });

                    if (DUNGEON_UNIT_AMOUNT == 0) {
                        DUNGEON_UNIT_AMOUNT = 0
                    } else {
                        DUNGEON_AVERAGE_LEVEL = Math.floor(DUNGEON_TEMP_LEVEL / DUNGEON_UNIT_AMOUNT)
                    };

                    DUNGEON_PHYSICAL_POWER = DUNGEON_TEMP_PHYSICAL
                    DUNGEON_MAGICAL_POWER = DUNGEON_TEMP_MAGICAL

                    ////////////////////////////////////////////////

                    DUNGEON_PARTY_LIST_TEXT = ` `
                    DUNGEON_INFO_UNIT_TEXT = ` `
                    DUNGEON_INFO_UNIT_TYPE = ` `

                    const StartEmbed = new EmbedBuilder()
                        .setAuthor({ name: `${message.author.tag}'s Dungeon - Edit Party`, iconURL: `${message.author.displayAvatarURL()}` })
                        .setThumbnail(DungeonGif)
                        .addFields(
                            { name: '**Party Stats**', value: `Average Level: \`💠 ${DUNGEON_AVERAGE_LEVEL}\`\nUnit Amount: \`👥 ${DUNGEON_UNIT_AMOUNT}/4\`\nPhysical Power: \`💪 ${DUNGEON_PHYSICAL_POWER}\`\nMagical Power: \`💫 ${DUNGEON_MAGICAL_POWER}\`\n` },
                        )
                        .setColor("#C0392B")
                        .setTimestamp()
                        .setFooter({ text: `Click ◀ to go back.\nClick 🎒 to Clear Party.\nClick 📥 to Equip.\nClick ⬅️ or ➡️ to go change page.\n` })

                    if (DUNGEON_PARTY_LIST.length >= 1) {
                        DUNGEON_PARTY_LIST.forEach(unit => {
                            if (unit.Unit_Class == 'Warrior') {
                                DUNGEON_TEMP_ICON_UNIT = `⚔️`
                            } else {
                                if (unit.Unit_Class == 'Gunner') {
                                    DUNGEON_TEMP_ICON_UNIT = `🔫`
                                } else {
                                    if (unit.Unit_Class == 'Wizard') {
                                        DUNGEON_TEMP_ICON_UNIT = `🪄`
                                    } else {
                                        if (unit.Unit_Class == 'Priest') {
                                            DUNGEON_TEMP_ICON_UNIT = `💉`
                                        } else {

                                        }
                                    }
                                }
                            };
                            DUNGEON_PARTY_LIST_TEXT += `**${DUNGEON_TEMP_ICON_UNIT} ${unit.Unit_Name} (Lv. ${unit.Unit_Level})**\nHealth: \`💕️️ ${unit.Attributes.Health_Value}\`\nPhysical Power: \`💪 ${unit.Attributes.Physical_Value}\`\nMagical Power: \`💫 ${unit.Attributes.Magical_Value}\`\n`
                        });

                        StartEmbed.addFields({ name: `**Party Member**`, value: `${DUNGEON_PARTY_LIST_TEXT}` });
                    };
                    let TempList = DUNGEON_UNIT_LIST
                    if (TempList.length >= 1) {
                        TempList.forEach(unit => {
                            checkLevel(unit);
                        });
                        if (TempList[DUNGEON_UNIT_LIST_PAGE].Unit_Class == 'Warrior') {
                            DUNGEON_INFO_UNIT_TYPE = `⚔️`
                        } else {
                            if (TempList[DUNGEON_UNIT_LIST_PAGE].Unit_Class == 'Gunner') {
                                DUNGEON_INFO_UNIT_TYPE = `🔫`
                            } else {
                                if (TempList[DUNGEON_UNIT_LIST_PAGE].Unit_Class == 'Wizard') {
                                    DUNGEON_INFO_UNIT_TYPE = `🪄`
                                } else {
                                    if (TempList[DUNGEON_UNIT_LIST_PAGE].Unit_Class == 'Priest') {
                                        DUNGEON_INFO_UNIT_TYPE = `💉`
                                    } else {

                                    }
                                }
                            }
                        };
                        DUNGEON_INFO_UNIT_TEXT = `**${DUNGEON_INFO_UNIT_TYPE} ${TempList[DUNGEON_UNIT_LIST_PAGE].Unit_Name} (Lv. ${TempList[DUNGEON_UNIT_LIST_PAGE].Unit_Level})**\nHealth: \`💕️️ ${TempList[DUNGEON_UNIT_LIST_PAGE].Attributes.Health_Value}\`\nPhysical Power: \`💪 ${TempList[DUNGEON_UNIT_LIST_PAGE].Attributes.Physical_Value}\`\nMagical Power: \`💫 ${TempList[DUNGEON_UNIT_LIST_PAGE].Attributes.Magical_Value}\`\n`
                        StartEmbed.addFields({ name: `**Your Unit - Page ${(DUNGEON_UNIT_LIST_PAGE + 1)}/${TempList.length}**`, value: `${DUNGEON_INFO_UNIT_TEXT}` });
                    };
                    await MainEmbed.edit({ embeds: [StartEmbed] })
                };

                async function EquipUnit() {

                    if (DUNGEON_PARTY_LIST.length >= 4) {
                        return;
                    };

                    let TEMP_UNIT = DUNGEON_UNIT_LIST[DUNGEON_UNIT_LIST_PAGE];

                    if (DUNGEON_PARTY_LIST.some(unit => unit.Unit_Name === TEMP_UNIT.Unit_Name)) {
                        return;
                    };

                    DUNGEON_PARTY_LIST.push(TEMP_UNIT);

                    users = us_read();
                    updateUserData(userId, { Dungeon: { ...users[userId].Dungeon, Party: users[userId].Dungeon.Party = DUNGEON_PARTY_LIST } });
                    us_write(users)

                    await UpdateEmbed_Party();

                };

                async function ClearParty() {

                    DUNGEON_PARTY_LIST = [];

                    users = us_read();
                    updateUserData(userId, { Dungeon: { ...users[userId].Dungeon, Party: users[userId].Dungeon.Party = DUNGEON_PARTY_LIST } });
                    us_write(users)

                    await UpdateEmbed_Party();

                };

                MainEmbed.reactions.removeAll().catch(error => console.error('Failed to clear reactions: ', error));
                await UpdateEmbed_Party()
                await MainEmbed.react('◀');
                await MainEmbed.react('🎒');
                await MainEmbed.react('📥');
                await MainEmbed.react('⬅️');
                await MainEmbed.react('➡️');

                let MessageTimeOut = setTimeout(async () => {
                    if (talkedRecently.has(message.author.id)) {
                        talkedRecently.delete(message.author.id);
                    };
                    collector.stop();
                    MainEmbed.delete();
                }, 60000);

                const filter = (reaction, user) => {
                    return user.id === message.author.id;
                };
                const collector = MainEmbed.createReactionCollector({
                    filter
                });

                collector.on('collect', async (reaction, user) => {
                    switch (reaction.emoji.name) {
                        case "◀":
                            if (user.tag === message.author.tag) {
                                clearTimeout(MessageTimeOut);
                                collector.stop();
                                await DungeonPage_Main();
                                break;
                            };
                        case "🎒":
                            if (user.tag === message.author.tag) {
                                reaction.users.remove(user);
                                await ClearParty();
                                break;
                            };
                        case "📥":
                            if (user.tag === message.author.tag) {
                                reaction.users.remove(user);
                                await EquipUnit();
                                break;
                            };
                        case "⬅️":
                            if (user.tag === message.author.tag) {
                                DUNGEON_UNIT_LIST_PAGE -= 1
                                if (DUNGEON_UNIT_LIST_PAGE < 0) {
                                    DUNGEON_UNIT_LIST_PAGE = (DUNGEON_UNIT_LIST.length - 1)
                                };
                                reaction.users.remove(user);
                                await UpdateEmbed_Party();
                                break;
                            };
                        case "➡️":
                            if (user.tag === message.author.tag) {
                                DUNGEON_UNIT_LIST_PAGE += 1
                                if (DUNGEON_UNIT_LIST_PAGE >= DUNGEON_UNIT_LIST.length) {
                                    DUNGEON_UNIT_LIST_PAGE = 0
                                };
                                reaction.users.remove(user);
                                await UpdateEmbed_Party();
                                break;
                            };
                    }
                });

            };

            async function DungeonPage_Instance(instance_name) {

                let DUNGEON_COMPLETION = users[userId].Dungeon.Completion
                let DUNGEON_UNIT_LIST = users[userId].Dungeon.Unit
                let DUNGEON_PARTY_LIST = users[userId].Dungeon.Party

                let DUNGEON_AVERAGE_LEVEL = 0
                let DUNGEON_UNIT_AMOUNT = DUNGEON_PARTY_LIST.length
                let DUNGEON_PHYSICAL_POWER = 0
                let DUNGEON_MAGICAL_POWER = 0

                ////////////////////////////////////////////////

                let DUNGEON_TEMP_ICON_UNIT = ``
                let DUNGEON_TEMP_LEVEL = 0
                let DUNGEON_TEMP_PHYSICAL = 0
                let DUNGEON_TEMP_MAGICAL = 0

                DUNGEON_PARTY_LIST.forEach(unit => {
                    DUNGEON_TEMP_LEVEL += unit.Unit_Level
                    DUNGEON_TEMP_PHYSICAL += unit.Attributes.Physical_Value
                    DUNGEON_TEMP_MAGICAL += unit.Attributes.Magical_Value
                });

                if (DUNGEON_UNIT_AMOUNT == 0) {
                    DUNGEON_UNIT_AMOUNT = 0
                } else {
                    DUNGEON_AVERAGE_LEVEL = (DUNGEON_TEMP_LEVEL / DUNGEON_UNIT_AMOUNT)
                };

                DUNGEON_PHYSICAL_POWER = DUNGEON_TEMP_PHYSICAL;
                DUNGEON_MAGICAL_POWER = DUNGEON_TEMP_MAGICAL;

                ////////////////////////////////////////////////

                let INSTANCE_UNIT_LIST = DUNGEON_PARTY_LIST

                let DungeonTick;

                let INSTANCE_NAME = instance_name;
                let INSTANCE_ROOM_TOTAL = 0;
                let INSTANCE_ROOM_NOW = 1;
                let INSTANCE_MODE = `✅ ON`;
                let INSTANCE_TURN = `☠️ Mob`;
                let INSTANCE_TURN_AMOUNT = 0;
                let INSTANCE_ACTION = `...`;
                let INSTANCE_REWARD = ` `;
                let INSTANCE_MOB = [];

                if (INSTANCE_NAME == 'Magical Forest') {
                    INSTANCE_ROOM_TOTAL = 3;
                    INSTANCE_MOB =
                        [
                            {
                                Name: "Magical Slime",
                                Level: 1,
                                Base_Health: 15,
                                Health: 0,
                                Damage: 2,
                            },
                            {
                                Name: "Mutant Shroom",
                                Level: 2,
                                Base_Health: 25,
                                Health: 0,
                                Damage: 3,
                            },
                            {
                                Name: "Bolockee (Boss)",
                                Level: 3,
                                Base_Health: 35,
                                Health: 0,
                                Damage: 5,
                            }
                        ];
                } else {
                    if (INSTANCE_NAME == 'Frozen Tomb') {
                        INSTANCE_ROOM_TOTAL = 5;
                        INSTANCE_MOB =
                            [
                                {
                                    Name: "Frozen Zombie",
                                    Level: 10,
                                    Base_Health: 80,
                                    Health: 0,
                                    Damage: 14,
                                },
                                {
                                    Name: "Icebound Wraith",
                                    Level: 11,
                                    Base_Health: 100,
                                    Health: 0,
                                    Damage: 16,
                                },
                                {
                                    Name: "Blizzard Elemental",
                                    Level: 12,
                                    Base_Health: 120,
                                    Health: 0,
                                    Damage: 18,
                                },
                                {
                                    Name: "Arctic Frostwolf",
                                    Level: 13,
                                    Base_Health: 130,
                                    Health: 0,
                                    Damage: 20,
                                },
                                {
                                    Name: "Frostbite Yeti (Boss)",
                                    Level: 15,
                                    Base_Health: 150,
                                    Health: 0,
                                    Damage: 22,
                                }
                            ];
                    } else {
                        if (INSTANCE_NAME == `Serpent's Labyrinth`) {
                            INSTANCE_ROOM_TOTAL = 7;
                            INSTANCE_MOB =
                                [
                                    {
                                        Name: "Venomfang Viper",
                                        Level: 20,
                                        Base_Health: 180,
                                        Health: 0,
                                        Damage: 24,
                                    },
                                    {
                                        Name: "Shadowscale Serpent",
                                        Level: 22,
                                        Base_Health: 216,
                                        Health: 0,
                                        Damage: 28,
                                    },
                                    {
                                        Name: "Labyrinthian Cobra",
                                        Level: 24,
                                        Base_Health: 252,
                                        Health: 0,
                                        Damage: 32,
                                    },
                                    {
                                        Name: "Poisonmist Adder",
                                        Level: 26,
                                        Base_Health: 288,
                                        Health: 0,
                                        Damage: 36,
                                    },
                                    {
                                        Name: "Dreadcoil Asp",
                                        Level: 28,
                                        Base_Health: 324,
                                        Health: 0,
                                        Damage: 40,
                                    },
                                    {
                                        Name: "Emerald Naga",
                                        Level: 29,
                                        Base_Health: 342,
                                        Health: 0,
                                        Damage: 42,
                                    },
                                    {
                                        Name: "Broodmother Vashara (Boss)",
                                        Level: 30,
                                        Base_Health: 360,
                                        Health: 0,
                                        Damage: 45,
                                    }
                                ];
                        } else {
                            if (INSTANCE_NAME == `Eldritch Depths`) {
                                INSTANCE_ROOM_TOTAL = 9;
                                INSTANCE_MOB =
                                    [
                                        {
                                            Name: "Abyssal Lurker",
                                            Level: 30,
                                            Base_Health: 324,
                                            Health: 0,
                                            Damage: 59,
                                        },
                                        {
                                            Name: "Voidwalker Minion",
                                            Level: 32,
                                            Base_Health: 348,
                                            Health: 0,
                                            Damage: 62,
                                        },
                                        {
                                            Name: "Maddening Specter",
                                            Level: 34,
                                            Base_Health: 362,
                                            Health: 0,
                                            Damage: 64,
                                        },
                                        {
                                            Name: "Twisted Abomination",
                                            Level: 34,
                                            Base_Health: 389,
                                            Health: 0,
                                            Damage: 66,
                                        },
                                        {
                                            Name: "Darkspawn Crawler",
                                            Level: 36,
                                            Base_Health: 402,
                                            Health: 0,
                                            Damage: 68,
                                        },
                                        {
                                            Name: "Cthonic Shambler",
                                            Level: 36,
                                            Base_Health: 423,
                                            Health: 0,
                                            Damage: 71,
                                        },
                                        {
                                            Name: "Shadowfiend Devourer",
                                            Level: 37,
                                            Base_Health: 445,
                                            Health: 0,
                                            Damage: 72,
                                        },
                                        {
                                            Name: "Nightmare Wraith",
                                            Level: 38,
                                            Base_Health: 468,
                                            Health: 0,
                                            Damage: 74,
                                        },
                                        {
                                            Name: "Eldritch Overlord Xal'Tharoth (Boss)",
                                            Level: 40,
                                            Base_Health: 501,
                                            Health: 0,
                                            Damage: 76,
                                        }
                                    ];
                            } else {

                            }
                        }
                    }
                }

                INSTANCE_MOB.forEach(unit => {
                    unit.Health = Math.floor(Number((unit.Base_Health) + (((unit.Base_Health * INSTANCE_UNIT_LIST.length) * 10) / 100)));
                });

                async function INSTANCE_UPDATE() {
                    const StartEmbed = new EmbedBuilder()
                        .setAuthor({ name: `${message.author.tag}'s Dungeon - Instance`, iconURL: `${message.author.displayAvatarURL()}` })
                        .setThumbnail(DungeonGif)
                        .addFields(
                            { name: '**Instance Stats**', value: `Dungeon: \`⚔️ ${INSTANCE_NAME}\`\nRoom: \`🚪 ${INSTANCE_ROOM_NOW}/${INSTANCE_ROOM_TOTAL}\`\nAuto: \`${INSTANCE_MODE}\`\nTurn: \`${INSTANCE_TURN} (${INSTANCE_TURN_AMOUNT})\`\nAction: \`${INSTANCE_ACTION}\`` },
                        )
                        .setColor("#C0392B")
                        .setTimestamp()
                        .setFooter({ text: `Click ❌ to cancel.\nClick ⚙️ to change mode.\n ` })

                    let INSTANCE_PARTY_LIST = ` `

                    INSTANCE_UNIT_LIST.forEach(unit => {
                        if (unit.Unit_Class == 'Warrior') {
                            DUNGEON_TEMP_ICON_UNIT = `⚔️`
                        } else {
                            if (unit.Unit_Class == 'Gunner') {
                                DUNGEON_TEMP_ICON_UNIT = `🔫`
                            } else {
                                if (unit.Unit_Class == 'Wizard') {
                                    DUNGEON_TEMP_ICON_UNIT = `🪄`
                                } else {
                                    if (unit.Unit_Class == 'Priest') {
                                        DUNGEON_TEMP_ICON_UNIT = `💉`
                                    } else {

                                    }
                                }
                            }
                        };
                        INSTANCE_PARTY_LIST += `**${DUNGEON_TEMP_ICON_UNIT} ${unit.Unit_Name} (Lv. ${unit.Unit_Level}) - `
                        if (unit.Attributes.Health_Value >= 1) {
                            INSTANCE_PARTY_LIST += `💕️ ${unit.Attributes.Health_Value} `
                            if (unit.Unit_Class == 'Priest') {
                                INSTANCE_PARTY_LIST += `💊 ${(unit.Attributes.Magical_Value / 2)}**\n`
                            } else {
                                INSTANCE_PARTY_LIST += `💥 ${unit.Attributes.Physical_Value + unit.Attributes.Magical_Value}**\n`
                            }
                        } else {
                            INSTANCE_PARTY_LIST += `☠️ DEAD**\n`
                        };
                    });

                    StartEmbed.addFields({ name: `**Your Party**`, value: `${INSTANCE_PARTY_LIST}` });
                    StartEmbed.addFields({ name: `**Mob Details**`, value: `**☠️ ${INSTANCE_MOB[(INSTANCE_ROOM_NOW - 1)].Name} (Lv. ${INSTANCE_MOB[(INSTANCE_ROOM_NOW - 1)].Level})**\nHealth: \`💕️️ ${INSTANCE_MOB[(INSTANCE_ROOM_NOW - 1)].Health}\`\nDamage: \`💥 ${INSTANCE_MOB[(INSTANCE_ROOM_NOW - 1)].Damage}\`\n` });
                    await MainEmbed.edit({ embeds: [StartEmbed] });
                };

                async function INSTANCE_COMPLETE() {
                    now = new Date(Date.now());
                    now_hours = now.getHours().toString().padStart(2, '0');
                    now_mins = now.getMinutes().toString().padStart(2, '0');
                    now_seconds = now.getSeconds().toString().padStart(2, '0');

                    const StartEmbed = new EmbedBuilder()
                        .setAuthor({ name: `${message.author.tag}'s Dungeon - Instance`, iconURL: `${message.author.displayAvatarURL()}` })
                        .setThumbnail(DungeonGif)
                        .addFields(
                            { name: '**Instance Stats**', value: `Dungeon: \`⚔️ ${INSTANCE_NAME}\`\nRoom: \`🚪 ${INSTANCE_ROOM_NOW}/${INSTANCE_ROOM_TOTAL}\`\nAction: \`${INSTANCE_ACTION}\`` },
                        )
                        .setColor("#C0392B")
                        .setTimestamp()
                        .setFooter({ text: `Click ✅ to confirm.\nClick ⚔️ to play again.` })

                    users = us_read();

                    if (INSTANCE_NAME == 'Magical Forest') {
                        TEMP_INSTANCE_REWARD_XP_BASE = 600 + (INSTANCE_ROOM_NOW * 250)
                        TEMP_INSTANCE_REWARD_COINS = 2500 + (INSTANCE_ROOM_NOW * 1000)
                        TEMP_INSTANCE_REWARD_UXP = 500 + (INSTANCE_ROOM_NOW * 500)
                    } else {
                        if (INSTANCE_NAME == 'Frozen Tomb') {
                            TEMP_INSTANCE_REWARD_XP_BASE = 1500 + (INSTANCE_ROOM_NOW * 500)
                            TEMP_INSTANCE_REWARD_COINS = 4000 + (INSTANCE_ROOM_NOW * 2000)
                            TEMP_INSTANCE_REWARD_UXP = 2500 + (INSTANCE_ROOM_NOW * 2500)
                        } else {
                            if (INSTANCE_NAME == `Serpent's Labyrinth`) {
                                TEMP_INSTANCE_REWARD_XP_BASE = 2000 + (INSTANCE_ROOM_NOW * 750)
                                TEMP_INSTANCE_REWARD_COINS = 6500 + (INSTANCE_ROOM_NOW * 4000)
                                TEMP_INSTANCE_REWARD_UXP = 6000 + (INSTANCE_ROOM_NOW * 7500)
                            } else {
                                if (INSTANCE_NAME == `Eldritch Depths`) {
                                    TEMP_INSTANCE_REWARD_XP_BASE = 3000 + (INSTANCE_ROOM_NOW * 1250)
                                    TEMP_INSTANCE_REWARD_COINS = 10000 + (INSTANCE_ROOM_NOW * 6000)
                                    TEMP_INSTANCE_REWARD_UXP = 15000 + (INSTANCE_ROOM_NOW * 12000)
                                } else {

                                }
                            }
                        }
                    }

                    TEMP_INSTANCE_REWARD_XP = Number((TEMP_INSTANCE_REWARD_XP_BASE * users[userId].Multiplier).toFixed(2))

                    users = us_read();
                    DUNGEON_PARTY_LIST.forEach(unit => {
                        unit.Unit_XP += Math.floor(TEMP_INSTANCE_REWARD_UXP / INSTANCE_UNIT_LIST.length)
                    });
                    INSTANCE_REWARD += `**🔯 UXP x ${comma(TEMP_INSTANCE_REWARD_UXP)}**\n`
                    updateUserData(userId, { Dungeon: { ...users[userId].Dungeon, Party: users[userId].Dungeon.Party = DUNGEON_PARTY_LIST } });
                    us_write(users);

                    users = us_read();
                    INSTANCE_REWARD += `**✨ XP x ${comma(TEMP_INSTANCE_REWARD_XP)}**\n`
                    updateUserData(userId, { XP: users[userId].XP += TEMP_INSTANCE_REWARD_XP });
                    us_write(users);

                    users = us_read();
                    INSTANCE_REWARD += `**🪙 Coins x ${comma(TEMP_INSTANCE_REWARD_COINS)}**\n`
                    updateUserData(userId, { Coins: users[userId].Coins += TEMP_INSTANCE_REWARD_COINS });
                    us_write(users);

                    let TEMP_COMPLETION = {
                        ID: INSTANCE_NAME
                    };

                    if (DUNGEON_COMPLETION.some(comp => comp.ID === TEMP_COMPLETION.ID)) { }
                    else {
                        DUNGEON_COMPLETION.push(TEMP_COMPLETION);

                        users = us_read();
                        updateUserData(userId, { Dungeon: { ...users[userId].Dungeon, Completion: users[userId].Dungeon.Completion = DUNGEON_COMPLETION } });
                        us_write(users)
                    };

                    StartEmbed.addFields(
                        { name: '**Dungeon Rewards**', value: `${INSTANCE_REWARD}` },
                    );

                    bot.BotLogs(message.guild.name, `\x1b[91m\x1b[37m"${message.author.tag}" \x1b[91mCompleted Dungeon \x1b[37m"${INSTANCE_NAME}"`);

                    await MainEmbed.edit({ embeds: [StartEmbed] });
                    await MainEmbed.react('✅');
                    await MainEmbed.react('⚔️');

                    const filter = (reaction, user) => {
                        return user.id === message.author.id;
                    };
                    const collector = MainEmbed.createReactionCollector({
                        filter
                    });

                    collector.on('collect', async (reaction, user) => {
                        switch (reaction.emoji.name) {
                            case "✅":
                                if (user.tag === message.author.tag) {
                                    if (talkedRecently.has(message.author.id)) {
                                        talkedRecently.delete(message.author.id);
                                    };
                                    clearInterval(DungeonTick);
                                    clearTimeout(MessageTimeOut);
                                    collector.stop()
                                    MainEmbed.delete();
                                    break;
                                };
                            case "⚔️":
                                if (user.tag === message.author.tag) {
                                    clearInterval(DungeonTick);
                                    clearTimeout(MessageTimeOut);
                                    collector.stop()
                                    await DungeonPage_Instance(INSTANCE_NAME)
                                    break;
                                };
                        }
                    });


                    let MessageTimeOut = setTimeout(async () => {
                        if (talkedRecently.has(message.author.id)) {
                            talkedRecently.delete(message.author.id);
                        };
                        clearInterval(DungeonTick);
                        collector.stop()
                        MainEmbed.delete();
                    }, 20000);

                };

                async function INSTANCE_FAIL() {
                    const StartEmbed = new EmbedBuilder()
                        .setAuthor({ name: `${message.author.tag}'s Dungeon - Instance`, iconURL: `${message.author.displayAvatarURL()}` })
                        .setThumbnail(DungeonGif)
                        .addFields(
                            { name: '**Instance Stats**', value: `Dungeon: \`⚔️ ${INSTANCE_NAME}\`\nRoom: \`🚪 ${INSTANCE_ROOM_NOW}/${INSTANCE_ROOM_TOTAL}\`\nAction: \`${INSTANCE_ACTION}\`` },
                        )
                        .setColor("#C0392B")
                        .setTimestamp()
                        .setFooter({ text: `Click ✅ to confirm.` })

                    if (INSTANCE_NAME == 'Magical Forest') {
                        TEMP_INSTANCE_REWARD_XP_BASE = 100 + (INSTANCE_ROOM_NOW * 50)
                        TEMP_INSTANCE_REWARD_COINS = 500 + (INSTANCE_ROOM_NOW * 100)
                        TEMP_INSTANCE_REWARD_UXP = 50 + (INSTANCE_ROOM_NOW * 100)
                    } else {
                        if (INSTANCE_NAME == 'Frozen Tomb') {
                            TEMP_INSTANCE_REWARD_XP_BASE = 250 + (INSTANCE_ROOM_NOW * 75)
                            TEMP_INSTANCE_REWARD_COINS = 1000 + (INSTANCE_ROOM_NOW * 450)
                            TEMP_INSTANCE_REWARD_UXP = 250 + (INSTANCE_ROOM_NOW * 450)
                        } else {
                            if (INSTANCE_NAME == `Serpent's Labyrinth`) {
                                TEMP_INSTANCE_REWARD_XP_BASE = 500 + (INSTANCE_ROOM_NOW * 125)
                                TEMP_INSTANCE_REWARD_COINS = 1200 + (INSTANCE_ROOM_NOW * 600)
                                TEMP_INSTANCE_REWARD_UXP = 350 + (INSTANCE_ROOM_NOW * 600)
                            } else {
                                if (INSTANCE_NAME == `Eldritch Depths`) {
                                    TEMP_INSTANCE_REWARD_XP_BASE = 750 + (INSTANCE_ROOM_NOW * 175)
                                    TEMP_INSTANCE_REWARD_COINS = 1500 + (INSTANCE_ROOM_NOW * 750)
                                    TEMP_INSTANCE_REWARD_UXP = 500 + (INSTANCE_ROOM_NOW * 750)
                                } else {

                                }
                            }
                        }
                    }

                    TEMP_INSTANCE_REWARD_XP = Number((TEMP_INSTANCE_REWARD_XP_BASE * users[userId].Multiplier).toFixed(2))

                    users = us_read();
                    DUNGEON_PARTY_LIST.forEach(unit => {
                        unit.Unit_XP += Math.floor(TEMP_INSTANCE_REWARD_UXP / INSTANCE_UNIT_LIST.length)
                    });
                    INSTANCE_REWARD += `**🔯 UXP x ${comma(TEMP_INSTANCE_REWARD_UXP)}**\n`
                    updateUserData(userId, { Dungeon: { ...users[userId].Dungeon, Party: users[userId].Dungeon.Party = DUNGEON_PARTY_LIST } });
                    us_write(users);

                    users = us_read();
                    INSTANCE_REWARD += `**✨ XP x ${comma(TEMP_INSTANCE_REWARD_XP)}**\n`
                    updateUserData(userId, { XP: users[userId].XP += TEMP_INSTANCE_REWARD_XP });
                    us_write(users);

                    users = us_read();
                    INSTANCE_REWARD += `**🪙 Coins x ${comma(TEMP_INSTANCE_REWARD_COINS)}**\n`
                    updateUserData(userId, { Coins: users[userId].Coins += TEMP_INSTANCE_REWARD_COINS });
                    us_write(users);

                    StartEmbed.addFields(
                        { name: '**Dungeon Rewards**', value: `${INSTANCE_REWARD}` },
                    );

                    bot.BotLogs(message.guild.name, `\x1b[91m\x1b[37m"${message.author.tag}" \x1b[91mFailed Dungeon`);

                    await MainEmbed.edit({ embeds: [StartEmbed] });
                    await MainEmbed.react('✅');

                    const filter = (reaction, user) => {
                        return user.id === message.author.id;
                    };
                    const collector = MainEmbed.createReactionCollector({
                        filter
                    });

                    collector.on('collect', async (reaction, user) => {
                        switch (reaction.emoji.name) {
                            case "✅":
                                if (user.tag === message.author.tag) {
                                    if (talkedRecently.has(message.author.id)) {
                                        talkedRecently.delete(message.author.id);
                                    };
                                    clearInterval(DungeonTick);
                                    clearTimeout(MessageTimeOut);
                                    collector.stop()
                                    MainEmbed.delete();
                                    break;
                                };
                        }
                    });

                    let MessageTimeOut = setTimeout(async () => {
                        if (talkedRecently.has(message.author.id)) {
                            talkedRecently.delete(message.author.id);
                        };
                        clearInterval(DungeonTick);
                        collector.stop()
                        MainEmbed.delete();
                    }, 20000);

                };

                now = new Date(Date.now());
                now_hours = now.getHours().toString().padStart(2, '0');
                now_mins = now.getMinutes().toString().padStart(2, '0');
                now_seconds = now.getSeconds().toString().padStart(2, '0');

                bot.BotLogs(message.guild.name, `\x1b[91m\x1b[37m"${message.author.tag}" \x1b[91mstarted Dungeon Instance \x1b[37m"${INSTANCE_NAME}"`);

                MainEmbed.reactions.removeAll().catch(error => console.error('Failed to clear reactions: ', error));
                await INSTANCE_UPDATE();
                await MainEmbed.react('❌');
                await MainEmbed.react('⚙️');

                const filter = (reaction, user) => {
                    return user.id === message.author.id;
                };
                const collector = MainEmbed.createReactionCollector({
                    filter
                });

                collector.on('collect', async (reaction, user) => {
                    switch (reaction.emoji.name) {
                        case "❌":
                            if (user.tag === message.author.tag) {
                                if (talkedRecently.has(message.author.id)) {
                                    talkedRecently.delete(message.author.id);
                                };
                                bot.BotLogs(message.guild.name, `\x1b[91m\x1b[37m"${message.author.tag}" \x1b[91mcancelled Dungeon Instance!`);
                                clearInterval(DungeonTick);
                                collector.stop()
                                MainEmbed.delete();
                                break;
                            };
                        case "⚙️":
                            if (user.tag === message.author.tag) {
                                reaction.users.remove(user); return
                                if (INSTANCE_MODE == `❌ OFF`) {
                                    INSTANCE_MODE = `✅ ON`
                                } else {
                                    INSTANCE_MODE = `❌ OFF`
                                };
                                await INSTANCE_UPDATE();
                                break;
                            };
                    }
                });

                async function INSTANCE_BEGIN() {

                    DungeonEvent_UpdateUnit()

                    DungeonTick = setInterval(async () => {

                        try {

                            let TEMP_ICON = ` `

                            function getTurn(arr) {
                                if (arr.length === 0) {
                                    return null;
                                };
                                const randomIndex = Math.floor(Math.random() * arr.length);
                                return arr[randomIndex];
                            };

                            if (INSTANCE_TURN == `☠️ Mob`) {
                                INSTANCE_TURN_AMOUNT += 1
                                let TEMP_TURN
                                while (true) {
                                    TEMP_TURN = getTurn(INSTANCE_UNIT_LIST);
                                    if (TEMP_TURN.Attributes.Health_Value >= 1) break
                                };
                                if (TEMP_TURN.Unit_Class == 'Warrior') {
                                    TEMP_ICON = `⚔️`
                                } else {
                                    if (TEMP_TURN.Unit_Class == 'Gunner') {
                                        TEMP_ICON = `🔫`
                                    } else {
                                        if (TEMP_TURN.Unit_Class == 'Wizard') {
                                            TEMP_ICON = `🪄`
                                        } else {
                                            if (TEMP_TURN.Unit_Class == 'Priest') {
                                                TEMP_ICON = `💉`
                                            } else {

                                            }
                                        }
                                    }
                                };
                                INSTANCE_ACTION = `☠️ ${INSTANCE_MOB[(INSTANCE_ROOM_NOW - 1)].Name} attacked ${TEMP_ICON} ${TEMP_TURN.Unit_Name} (💕️ - ${INSTANCE_MOB[(INSTANCE_ROOM_NOW - 1)].Damage})`
                                TEMP_TURN.Attributes.Health_Value -= INSTANCE_MOB[(INSTANCE_ROOM_NOW - 1)].Damage
                                if (TEMP_TURN.Attributes.Health_Value < 0) { TEMP_TURN.Attributes.Health_Value = 0 };
                                if (INSTANCE_UNIT_LIST.every(unit => unit.Attributes.Health_Value <= 0)) {
                                    INSTANCE_ACTION = `❌ Challenge Failed!`
                                    clearInterval(DungeonTick);
                                    collector.stop()
                                    MainEmbed.reactions.removeAll().catch(error => console.error('Failed to clear reactions: ', error));
                                    await INSTANCE_FAIL();
                                    return;
                                } else {
                                    INSTANCE_TURN = `👥 Team`
                                    await INSTANCE_UPDATE();
                                };
                            } else {
                                INSTANCE_TURN_AMOUNT += 1
                                let TEMP_TURN
                                while (true) {
                                    TEMP_TURN = getTurn(INSTANCE_UNIT_LIST);
                                    if (TEMP_TURN.Attributes.Health_Value >= 1) break
                                };
                                if (TEMP_TURN.Unit_Class == 'Warrior') {
                                    TEMP_ICON = `⚔️`
                                } else {
                                    if (TEMP_TURN.Unit_Class == 'Gunner') {
                                        TEMP_ICON = `🔫`
                                    } else {
                                        if (TEMP_TURN.Unit_Class == 'Wizard') {
                                            TEMP_ICON = `🪄`
                                        } else {
                                            if (TEMP_TURN.Unit_Class == 'Priest') {
                                                TEMP_ICON = `💉`
                                            } else {

                                            }
                                        }
                                    }
                                };
                                if (TEMP_TURN.Unit_Class == 'Priest') {
                                    INSTANCE_ACTION = `${TEMP_ICON} ${TEMP_TURN.Unit_Name} healed everyone (💕️ + ${(TEMP_TURN.Attributes.Magical_Value / 2)})`
                                    INSTANCE_UNIT_LIST.forEach(unit => {
                                        if (unit.Attributes.Health_Value <= 0) return
                                        let maxHP = Math.floor(Number((unit.Base.Base_Health) + (((unit.Base.Base_Health * unit.Unit_Level) * 55) / 100)));
                                        unit.Attributes.Health_Value += (TEMP_TURN.Attributes.Magical_Value / 2)
                                        if (unit.Attributes.Health_Value >= maxHP) {
                                            unit.Attributes.Health_Value = maxHP
                                        };
                                    });
                                } else {
                                    INSTANCE_ACTION = `${TEMP_ICON} ${TEMP_TURN.Unit_Name} attacked ☠️ ${INSTANCE_MOB[(INSTANCE_ROOM_NOW - 1)].Name} (💕️ - ${(TEMP_TURN.Attributes.Physical_Value + TEMP_TURN.Attributes.Magical_Value)})`
                                    INSTANCE_MOB[(INSTANCE_ROOM_NOW - 1)].Health -= (TEMP_TURN.Attributes.Physical_Value + TEMP_TURN.Attributes.Magical_Value)
                                };
                                if (INSTANCE_MOB[(INSTANCE_ROOM_NOW - 1)].Health <= 0) {
                                    INSTANCE_ROOM_NOW += 1
                                    if (INSTANCE_ROOM_NOW > INSTANCE_ROOM_TOTAL) {
                                        INSTANCE_ROOM_NOW = INSTANCE_ROOM_TOTAL
                                        INSTANCE_ACTION = `✅ Challenge Completed!`
                                        clearInterval(DungeonTick);
                                        collector.stop()
                                        MainEmbed.reactions.removeAll().catch(error => console.error('Failed to clear reactions: ', error));
                                        await INSTANCE_COMPLETE();
                                    } else {
                                        INSTANCE_ACTION = `🚪 Entering Room (${INSTANCE_ROOM_NOW}/${INSTANCE_ROOM_TOTAL})`
                                        await INSTANCE_UPDATE();
                                    };
                                } else {
                                    INSTANCE_TURN = `☠️ Mob`
                                    await INSTANCE_UPDATE();
                                };
                            };

                        } catch (error) {
                            console.log(error);
                        };

                    }, 5000);

                };

                await INSTANCE_BEGIN();

            };

            DungeonEvent_UpdateUnit();

            await DungeonPage_Main();

        };

    } catch (error) {
        bot.BotLogs("SYSTEM", `\x1b[31m---------------------------------------------------------------`);
        bot.BotLogs("SYSTEM", `\x1b[31mError Occurred: \x1b[37m"${error}" \x1b[31mfrom \x1b[37m"${path.basename(__filename)}"`);
        bot.BotLogs("SYSTEM", `\x1b[31m---------------------------------------------------------------`);
    }

};

exports.help = {
    name: 'dungeon',
    aliases: ['dun', 'dungeons']
};