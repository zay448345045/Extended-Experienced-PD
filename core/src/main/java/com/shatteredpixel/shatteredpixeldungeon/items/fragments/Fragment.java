/*
 *
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * Experienced Pixel Dungeon
 * Copyright (C) 2019-2024 Trashbox Bobylev
 *
 * Extended Experienced Pixel Dungeon
 * Copyright (C) 2023-2024 John Nollas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 */

package com.shatteredpixel.shatteredpixeldungeon.items.fragments;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;

public class Fragment extends Item {
    {
        levelKnown = true;
        identify();
    }

    protected static long fragmentLevel(Class<? extends Fragment> fragmentType ){
        if (Dungeon.hero == null || Dungeon.hero.belongings == null){
            return 0;
        }

        Fragment fragment = Dungeon.hero.belongings.getItem(fragmentType);

        if (fragment != null){
            return fragment.buffedLvl();
        } else {
            return 0;
        }
    }

    @Override
    public String name() {
        return "Fragment";
    }

    @Override
    public String desc() {
        return "A plain fragment. Has no effects, can be used to craft a new fragment.";
    }
}
