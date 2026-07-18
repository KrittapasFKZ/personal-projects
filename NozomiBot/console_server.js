const net = require('net');
const server_port = 4000
const fs = require("fs");
const path = require('path');

function formatTime(milliseconds) {
    const totalSeconds = Math.floor(milliseconds / 1000);
    const days = Math.floor(totalSeconds / (24 * 60 * 60));
    const hours = Math.floor((totalSeconds % (24 * 60 * 60)) / (60 * 60));
    const minutes = Math.floor((totalSeconds % (60 * 60)) / 60);
    const seconds = totalSeconds % 60;

    return `${days} days, ${hours} hours, ${minutes} minutes, ${seconds} seconds`;
};

function config_read() {
    const rawData = fs.readFileSync('./config.json');
    return JSON.parse(rawData);
};

const server = net.createServer((socket) => {
    const { bot } = require('./index.js');
    const logsDir = path.join(__dirname, 'logs');
    const logFileName = path.join(logsDir, `socket-${new Date().toISOString().replace(/:/g, '-')}.log`);
    const logStream = fs.createWriteStream(logFileName, { flags: 'a' });

    function BotLogs(host, msg) {
        let now = new Date(Date.now());
        let now_hours = now.getHours().toString().padStart(2, '0');
        let now_mins = now.getMinutes().toString().padStart(2, '0');
        let now_seconds = now.getSeconds().toString().padStart(2, '0');
        if (host == "SERVER") {
            let new_host = "SERVER_CMD"
            console.log(`\x1b[90m[\x1b[0m${now_hours}:${now_mins}:${now_seconds}\x1b[90m|\x1b[97mServer\x1b[90m] ${msg}`, "\x1b[0m");
            logStream.write(`server: ${msg}\n`);
        };
    };

    BotLogs("SERVER", `\x1b[96mA client has \x1b[92mconnected \x1b[96mto the server!`);

    socket.on('end', () => {
        BotLogs("SERVER", `\x1b[96mA client has \x1b[31mdisconnected \x1b[96mfrom the server!`);
    });

    socket.on('error', (err) => {
        if (err.code === 'ECONNRESET') {
            BotLogs("SERVER", `\x1b[96mA client has \x1b[31mdisconnected \x1b[96mfrom the server!`);
        } else {
            BotLogs("SERVER", `\x1b[31m---------------------------------------------------------------`);
            BotLogs("SERVER", `\x1b[31mError Occurred: \x1b[97m"${err}"`);
            BotLogs("SERVER", `\x1b[31m---------------------------------------------------------------`);
        }
    });

    socket.on('data', (data) => {
        const input = data.toString().trim().toLowerCase();
        const [command, ...args] = input.split(' ');
        logStream.write(`client: ${input}\n`);
        let response;

        switch (command.toLowerCase()) {
            default:
                response = `\x1b[31mUnknown command: \x1b[37m${command}\n\x1b[31mCheck Available commands: \x1b[37mhelp\x1b[0m`;
                break;
            case 'status':
                let config = config_read();
                let currentTime = new Date();
                let startTime = new Date(config.startTime)
                let timeDifference = currentTime - startTime;
                let uptime = formatTime(timeDifference);
                response = `\x1b[34m---------------------------------------------------------------\n`
                response += `\x1b[34m Online since: \x1b[37m${config.online_since}\n`
                response += `\x1b[34m Last Backup: \x1b[37m${config.last_backup}\n`
                response += `\x1b[34m Last Restart: \x1b[37m${config.rt_time}\n`
                response += `\x1b[34m Restart due to: \x1b[37m"${config.rt_error}"\n`
                response += `\x1b[34m Error Count: \x1b[37m${config.rt_attempt} times since start\n`
                response += `\x1b[34m Uptime: \x1b[37m${uptime}\n`
                response += `\x1b[34m---------------------------------------------------------------\x1b[0m`
                break;
            case 'help':
                let commandList = `help, status, bot`;
                response = `\x1b[90mAvailable CLI commands: [${commandList}]\x1b[0m`;
                break;
            case 'bot':
                if (args.length > 0) {
                    switch (args[0]) {
                        default:
                            response = `\x1b[31mUnknown command: \x1b[37m${command} ${args[0]}\n\x1b[31mCheck Available commands: \x1b[37mbot help\x1b[0m`;
                            break;
                        case 'help':
                            let commandList = '';
                            let disableList = '';
                            let administrator = 'enable, disable';
                            bot.commands.forEach((command) => {
                                commandList += `${command.help.name}, `;
                            });
                            bot.disable_commands.forEach((command) => {
                                commandList += `${command.help.name}, `;
                            });
                            commandList = commandList.slice(0, -2);
                            disableList = disableList.slice(0, -2);
                            response = `\x1b[95m= NozomiBot Commands =\n\x1b[90mAdministrator commands: \x1b[37m[${administrator}]\n\x1b[90mDisabled commands: \x1b[37m[${disableList}]\n\x1b[90mAvailable commands: \x1b[37m[${commandList}]\x1b[0m`;
                            break;
                        case 'disable':
                            if (args[1]) {
                                let commandName = args[1];
                                if (bot.commands.has(commandName)) {
                                    const command = bot.commands.get(commandName);
                                    bot.disable_commands.set(commandName, command);
                                    bot.commands.delete(commandName);
                                    response = `\x1b[95mCommand \x1b[90m[\x1b[37m${commandName}.js\x1b[90m] \x1b[95mhas been \x1b[31mDisabled\x1b[0m`;
                                    BotLogs("SERVER", `\x1b[95mCommand \x1b[90m[\x1b[37m${commandName}.js\x1b[90m] \x1b[95mhas been \x1b[31mDisabled\x1b[0m`);
                                } else {
                                    response = `\x1b[31mCommand \x1b[90m[\x1b[37m${commandName}.js\x1b[90m] \x1b[31mis already disabled or does not exist.\x1b[0m`;
                                };
                            } else {
                                response = `\x1b[31mMissing Arguments!\n\x1b[31mCommands usage: \x1b[37mbot disable <file>\x1b[0m`;
                            };
                            break;
                        case 'enable':
                            if (args[1]) {
                                let commandName = args[1];
                                if (bot.disable_commands.has(commandName)) {
                                    delete require.cache[require.resolve(`./commands/${commandName}.js`)];
                                    const command = require(`./commands/${commandName}.js`);
                                    bot.commands.set(commandName, command);
                                    bot.disable_commands.delete(commandName);
                                    response = `\x1b[95mCommand \x1b[90m[\x1b[37m${commandName}.js\x1b[90m] \x1b[95mhas been \x1b[92mEnabled\x1b[0m`;
                                    BotLogs("SERVER", `\x1b[95mCommand \x1b[90m[\x1b[37m${commandName}.js\x1b[90m] \x1b[95mhas been \x1b[92mEnabled\x1b[0m`);
                                } else {
                                    response = `\x1b[31mCommand \x1b[90m[\x1b[37m${commandName}.js\x1b[90m] \x1b[31mis already enabled or does not exist.\x1b[0m`;
                                };
                            } else {
                                response = `\x1b[31mMissing Arguments!\n\x1b[31mCommands usage: \x1b[37mbot enable <file>\x1b[0m`;
                            };
                            break;
                    };
                } else {
                    response = `\x1b[31mUnknown command: \x1b[37m${command}\n\x1b[31mCheck Available commands: \x1b[37mbot help\x1b[0m`;
                }
                break;
        };

        if (response) {
            socket.write(response);
            logStream.write(`server: ${response}\n`);
        }
    });
});

server.listen(server_port, () => {
    function BotLogs(host, msg) {
        let now = new Date(Date.now());
        let now_hours = now.getHours().toString().padStart(2, '0');
        let now_mins = now.getMinutes().toString().padStart(2, '0');
        let now_seconds = now.getSeconds().toString().padStart(2, '0');
        if (host == "SERVER") {
            console.log(`\x1b[90m[\x1b[0m${now_hours}:${now_mins}:${now_seconds}\x1b[90m|\x1b[97mServer\x1b[90m] ${msg}`, "\x1b[0m");
        };
    };
    BotLogs("SERVER", `\x1b[96mServer is listening on port \x1b[37m${server_port}`);
    //const consoleProcess = spawn('cmd.exe', ['/c', 'start', 'cmd', '/k', 'console.bat'], { shell: true });
});

exports.console_server = server