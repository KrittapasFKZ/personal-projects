const net = require('net');
const readline = require('readline');
const server_port = 4000;

const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout
});

console.clear();

function client_disconnect(msg) {
    console.error(`\n${msg}`, "\x1b[0m");
    setTimeout(() => {
        rl.close();
    }, 2500);
};

console.log(`\x1b[90mConnecting...`, `\x1b[0m`);
setTimeout(() => {
    const client = net.createConnection({ port: server_port }, () => {
        console.log("\n\x1b[92mConnected to NozomiBot server.", `\x1b[0m`);
        console.log(`\x1b[95mWelcome to NozomiBot Console CLI V 0.1\n`, "\x1b[0m");
        rl.setPrompt('> ');
        rl.prompt();

        rl.on('line', (line) => { 
            const command = line.trim();
            client.write(command);
        }); 
    });

    client.on('data', (data) => {
        console.log(data.toString());
        rl.prompt(); 
    });

    client.on('error', (err) => {
        client_disconnect("\x1b[31mUnable to connect to NozomiBot server! Try again later!");
    });

    client.on('end', () => {
        client_disconnect("\x1b[31mDisconnected from NozomiBot server. Auto-closing in 2s.");
    });

}, 1000);