// muhahahahaa

function hasAttribute(loreLines, attr) {
    return loreLines.some(line => line.toLowerCase().includes(attr.toLowerCase()));
};

export const checkGodRolls = {
    /// Aurora ///
    mage1(loreLines) {
        return hasAttribute(loreLines, "mana pool") && hasAttribute(loreLines, "mana regeneration");
    },
    mage2(loreLines) {
        return hasAttribute(loreLines, "mana pool") && hasAttribute(loreLines, "vitality");
    },
    mage3(loreLines) {
        return hasAttribute(loreLines, "mana pool") && hasAttribute(loreLines, "veteran");
    },
    mage4(loreLines) {
        return hasAttribute(loreLines, "mana pool") && hasAttribute(loreLines, "dominance");
    },
    /// Terror ///
    arch1(loreLines) {
        return hasAttribute(loreLines, "lifeline") && hasAttribute(loreLines, "mana pool");
    },
    arch2(loreLines) {
        return hasAttribute(loreLines, "dominance") && hasAttribute(loreLines, "vitality");
    },
    /// Crimson ///
    ber1(loreLines) {
        return hasAttribute(loreLines, "magic find") && hasAttribute(loreLines, "veteran");
    },
    ber2(loreLines) {
        return hasAttribute(loreLines, "magic find") && hasAttribute(loreLines, "vitality");
    },
    ber3(loreLines) {
        return hasAttribute(loreLines, "vitality") && hasAttribute(loreLines, "veteran");
    }
};