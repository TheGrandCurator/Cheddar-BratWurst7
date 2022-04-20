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
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.EnumSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.util.CyclingButtonWidget;

@SearchTags({"no background", "NoGuiBackground", "no gui background",
        "NoGradient", "no gradient"})
public final class NoBackgroundHack extends Hack {
    private final CheckboxSetting noShading = new CheckboxSetting("Perfectly Clear", true);
    private final SliderSetting transparency = new SliderSetting("transparency",
            "Set background transparency",
            108, 0, 171, 1, SliderSetting.ValueDisplay.INTEGER);



    public NoBackgroundHack() {
        super("NoBackground");
        setCategory(Category.RENDER);
        addSetting(noShading);
        addSetting(transparency);
    }

    public int getTransparency() {
        return transparency.getValueI();
    }

    public boolean isNoShading() {
        return noShading.isChecked();
    }
    // See ScreenMixin.onRenderBackground()
}
