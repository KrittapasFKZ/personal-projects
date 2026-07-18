import Settings from "../config";

let hudElements = [];

/**
 * Registers a HUD element for managed rendering.
 * @param {function} renderFunc - The function to render this HUD element.
 * @param {function} shouldRender - Optional condition for whether it should render.
 */
export function registerHudElement(renderFunc, shouldRender = () => true) {
    hudElements.push({ renderFunc, shouldRender });
}

register("renderOverlay", () => {
    for (const hud of hudElements) {
        try {
            if (hud.shouldRender()) {
                GL11.glPushMatrix();   
                hud.renderFunc();     
                GL11.glPopMatrix();    
            }
        } catch (e) {
            ChatLib.chat(`${Settings.prefix} &cError in HUD element: &f${e}`);
        }
    }
});