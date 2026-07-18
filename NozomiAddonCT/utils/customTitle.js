import { registerHudElement } from "../utils/HUDManager";

let text = " "
let hideAt = 0; 

const TriggerTitle = (msg) => {
    text = msg
    hideAt = Date.now() + 2000;
};
 
registerHudElement(() => {
    if (!text || Date.now() > hideAt) return;
    let scale = 4
    let w = Renderer.screen.getWidth() / 2 - Renderer.getStringWidth(text) * scale / 2
    let h = Renderer.screen.getHeight() / 3

    Renderer.scale(scale, scale)
    Renderer.drawStringWithShadow(text, w / scale, h / scale)
    Renderer.scale(1 / scale, 1 / scale)
}, () => true);

export { TriggerTitle };