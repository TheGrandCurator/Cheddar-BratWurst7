/*
 * Copyright (c) 2014-2022 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.SliderSetting;

@SearchTags({"no background", "NoGuiBackground", "no gui background",
        "NoGradient", "no gradient"})
public final class NoBackgroundHack extends Hack {
    private final SliderSetting transparency = new SliderSetting("Transparency",
            "Set background transparency",
            0, 0, 1, 0.05, SliderSetting.ValueDisplay.DECIMAL);

    public NoBackgroundHack() {
        super("NoBackground");
        setCategory(Category.RENDER);
        addSetting(transparency);
    }

    public SliderSetting getTransparency() {
        return transparency;
    }

    // See ScreenMixin.onRenderBackground()
}
