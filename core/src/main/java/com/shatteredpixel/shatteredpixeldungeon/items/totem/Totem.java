package com.shatteredpixel.shatteredpixeldungeon.items.totem;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

import java.util.ArrayList;

public class Totem extends Item {

    {
        image = ItemSpriteSheet.NONE_TOTEM;
    }

    @Override
    public ArrayList<String> actions(Hero hero ) {
        ArrayList<String> actions = new ArrayList<>();
        actions.remove( AC_THROW );
        actions.add( AC_DROP );
        return actions;
    }

    @Override
    public boolean doPickUp(Hero hero) {
        return doPickUp( hero, hero.pos );
    }

    @Override
    public void doDrop( Hero hero ) {
        super.doDrop(hero);
    }

    @Override
    public void onDetach(){}

    @Override
    public boolean isUpgradable() {
        return false;
    }

    @Override
    public void onHeroGainExp( float levelPercent, Hero hero ){
        //do nothing by default
    }

    @Override
    public String name() {
        return "Totem";
    }
    @Override
    public String desc() {
        return "A plain totem. Has no effects. Can be imbued with an effect through alchemy.";
    }
}
