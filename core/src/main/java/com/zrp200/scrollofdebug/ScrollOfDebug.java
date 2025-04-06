
package com.zrp200.scrollofdebug;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.*;
// backwards compatible until v0.9.4, before, returned void (21de6d38)
import static com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation.teleportToLocation;
import static java.util.Arrays.copyOfRange;

import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;

import com.badlogic.gdx.utils.StringBuilder;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
// Commands
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
// needed for HelpWindow
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;

import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
// WndTextInput (added in v0.9.4)
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTextInput;
// Output
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

import com.watabou.noosa.Game;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.FileUtils;
import com.watabou.utils.Reflection;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Scroll of Debug uses ClassLoader to get every class that can be directly created and provides a command interface with which to interact with them.
 *
 *
 * @author  <a href="https://github.com/zrp200/scrollofdebug">
 *              Zrp200
 * @version v2.1.0
 *
 * @apiNote Compatible with Shattered Pixel Dungeon v1.3.0+, and compatible with any LibGDX Shattered Pixel Dungeon version (post v0.8) with minimal changes.
 * **/
@SuppressWarnings({"rawtypes", "unchecked"})
public class ScrollOfDebug extends Scroll {
    {
        image = ItemSpriteSheet.SCROLL_HOLDER;
    }

    static String lastCommand = ""; // used with '!!'

    /** this is where all the game files are supposed to be located. **/
    private static final String ROOT = "com.shatteredpixel.shatteredpixeldungeon";

    private enum Command {
        HELP(null, // ...
                "[COMMAND | all]",
                "Gives more information on commands",
                "Specifying a command after the help will give an explanation for how to use that command."),
        // todo add more debug-oriented commands
        CHANGES(null, "", "Gives a history of changes to Scroll of Debug."),
        // generation commands.
        GIVE(Item.class,
                "<item> [+<level>] [x<quantity>] [-f|--force] [<method> [<args..>] ]",
                "Creates and puts into your inventory the generated item",
                "Any method specified will be called prior to collection.",
                "Specifying _level_ will set the level of the item to the indicated amount using Item#level. This is the method called when restoring items from a save file. If it's not giving you want you want, please try passing \"upgrade\" <level> as your method.",
                "_--force_ (or _-f_ for short) will disable all on-pickup logic (specifically Item#doPickUp) that may be affecting how the item gets collected into your inventory."),
        SPAWN(Mob.class,
                "<mob> [x<quantity>|(-p|--place)] [<method>]",
                "Creates the indicated mob and places them on the depth.",
                "Specifying [quantity] will attempt to spawn that many mobs ",
                "_-p_ allows manual placement, though it cannot be combined with a quantity argument."),
        SET(Trap.class,
                "<trap>",
                "Sets a trap at an indicated position"),
        AFFECT(Buff.class,
                "<buff> [<duration>] [<method> [<args..>]]",
                "Allows you to attach a buff to a character in sight.",
                "This can be potentially hazardous if a buff is applied to something that it was not designed for.",
                "Specifying _duration_ will attempt to set the duration of the buff. In the cases of buffs that are active in nature (e.g. buffs.Burning), you may need to call a method to properly set its duration.",
                "The method is called after the buff is attached, or on the existing buff if one existed already. This means you can say \"affect doom detach\" to remove doom from that character."),
        SEED(Blob.class,
                "<blob> [<amount>]",
                "Seed a blob of the specified amount to a targeted tile"),
        USE(Object.class, "<object> method [args]", "Use a specified method from a desired class.",
                "It may be handy to see _inspect_ to see usable methods for your object",
                "If you set a variable from this command, the return value of the method will be stored into the variable."),
        INSPECT(Object.class, "<object>", "Gives a list of supported methods for the indicated class."),
        GOTO(null, "<depth>", "Sends your character to the indicated depth."),
        WARP(null, "[<cell>]", "Targeted teleportation. Optionally takes a cell location, most easily assigned by variable"),
        MACRO(null, "<name>",
                "Store a sequence of scroll of debug commands to a single name",
                "Macros are a way to store and reproduce multiple scroll of debug commands at once.",
                "This is an experimental feature. Anything that prompts the player should be at the last line of a macro.",
                "Macros can call other macros",
                "To take parameters, write '%n', where n is the nth input after the macro name when calling it. For example `mymacro rat` can reference 'rat' via '%1'.",
                "Macros are saved and are kept independent of run."
        ),
        VARIABLES(null,
                "_@_<variable> [ [COMMAND ...] | i[nv] | c[ell] ]",
                "store game objects for later use as method targets or parameters",
                "The variables can be referenced later with their names for the purposes of methods from commands, as well as the _use_ and _inspect_ commands.",
                "You can see all active variable names by typing _@_.",
                "Specifying \"inv\" (or \"i\") will have the game prompt you to select an item from your inventory.",
                "Specifying \"cell\" (or \"c\") will allow you to select a tile. ",
                "When selecting a cell, you may or may not be able to directly select things in the tile you select, depending on the Scroll of Debug implementation.",
                "Please note that variables are not saved when you close the game."
        );

        final Class<?> paramClass;
        final String syntax;
        // a short description intended to fit on one line.
        final String summary;
        // more details on usage. a length of 1 will be treated as an extended description, more will be treated as a list.
        final String[] notes;

        Command(Class<?> paramClass, String syntax, String summary, String... notes) {
            this.paramClass = paramClass;
            this.syntax = syntax;
            this.summary = summary;
            this.notes = notes;
        }

        @Override public String toString() { return name().toLowerCase(); }

        String documentation() { return documentation(this, syntax, summary); }
        static String documentation(Object command, String syntax, String description) {
            return String.format("_%s_ %s\n%s", command, syntax, description);
        }

        // adds more information depending on what the paramClass actually is.
        String fullDocumentation(PackageTrie trie, boolean showClasses) {
            String documentation = documentation();
            if(notes.length > 0) {
                documentation += "\n";
                if(notes.length == 1) documentation += "\n" + notes[0];
                else for(String note : notes) documentation += "\n_-_ " + note;
            }
            if(showClasses && paramClass != null && !paramClass.isPrimitive() && paramClass != Object.class) {
                documentation += "\n\n_Valid Classes_:" + listAllClasses(trie,paramClass);
            }
            return documentation;
        }
        String fullDocumentation(PackageTrie trie) { return fullDocumentation(trie, true); }


        static Command get(String string) { try {
            return string.equals("@") ? VARIABLES :
                    valueOf(string.toUpperCase());
        } catch (Exception e) { return null; } }
    }

    // -- macro logic
    private static final String MACRO_FILE = "debug-macros.dat", KEYS="KEYS", VALUES="VALUES";

    private static HashMap<String, String> macros = null;

    // always returns non-null value
    private static Map<String, String> getMacros() {
        if (macros == null) try {
            Bundle macroBundle = FileUtils.bundleFromFile(MACRO_FILE);
            String[] keys=macroBundle.getStringArray(KEYS), values=macroBundle.getStringArray(VALUES);
            if (keys == null || values == null) throw new IOException("bad macro bundle!");
            macros = new HashMap<>(keys.length);
            for (int i=0; i < keys.length; i++) macros.put(keys[i], values[i]);
        } catch (IOException e) {
            // just... yea. Assuming the file just isn't there or something?
            Game.reportException(new IOException("Failed to retrieve Scroll of Debug macros", e));
            macros = new HashMap<>(); // initialize empty array
        }
        return macros;
    }

    /** creates or modifies macro with value. If value is empty, delete the macro. **/
    public static void setMacro(String macro, String value) {
        getMacros();
        if(value.isEmpty() ? macros.remove(macro) == null : value.equals(macros.put(macro, value))) return;
        // only run this if we actually changed macros
        Bundle bundle = new Bundle();
        String[] a = {};
        bundle.put(KEYS, macros.keySet().toArray(a));
        bundle.put(VALUES, macros.values().toArray(a));
        try {
            FileUtils.bundleToFile(MACRO_FILE, bundle);
        } catch (IOException e) {
            Game.reportException(new IOException("Failed to save Scroll of Debug macros", e));
        }
    }

    // -- general logic

    // fixme should be able to buffer a store location for a macro
    private String storeLocation;

    @Override
    public void doRead() {
        collect(); // you don't lose scroll of debug.
        GameScene.show(new WndTextInput("Enter Command:", null, "", 100, false,
                "Execute", "Cancel") {

            private String[] handleVariables(String[] input) {
                storeLocation = null;
                if (input.length > 0 && input[0].startsWith(Variable.MARKER)) {
                    // drop from the start, save for later.
                    storeLocation = input[0];
                    if (storeLocation.length() == 1) {
                        if (input.length > 1)
                            GLog.w("warning: remaining arguments were discarded");
                        // list them all
                        StringBuilder s = new StringBuilder();
                        for (Map.Entry<String, Variable> e : Variable.assigned.entrySet())
                            if (e.getValue().isActive()) {
                                s.append("\n_").append(e.getKey()).append("_ - ").append(e.getValue());
                            }
                        GameScene.show(new HelpWindow("Active Variables: \n" + s));
                        return null;
                    }
                    input = Arrays.copyOfRange(input, 1, input.length);

                    // variable-specific actions
                    if (input.length == 0) {
                        GLog.p("%s = %s", storeLocation, Variable.toString(storeLocation));
                        return input;
                    }
                    String vCommand = input[0].toLowerCase();
                    if (vCommand.matches("i(nv(entory)?)?")) {
                        Variable.putFromInventory(storeLocation);
                        return null;
                    } else if (vCommand.matches("c(ell)?")) {
                        Variable.putFromCell(storeLocation);
                        return null;
                    }

                }
                return input;
            }

            @Override public void onSelect(boolean positive, String text) {
                if(!positive) return;

                // !! handling
                {
                    Matcher m = Pattern.compile("!!").matcher(text);
                    if (m.find()) {
                        GLog.newLine();
                        GLog.i("> %s", text = m.replaceAll(lastCommand));
                        GLog.newLine();
                    }
                }
                lastCommand = text;

                String[] initialInput = text.split(" ");
                Callback init = null;

                final String[] input = handleVariables(initialInput);

                if (input == null || input.length == 0) return;

                interpret(input);
            }

            // returns whether a macro exists
            private boolean handleMacro(String[] input) {
                String macro = getMacros().get(input[0]);
                if(macro == null) return false; // only false output of handleMacro

                Pattern argPattern = Pattern.compile("%(\\d)");
                // avoid stupid infinite loops caused by parameter substitution
                // I want to allow it but infinite loops are dumb
                int[] placeholders = new int[input.length];
                Arrays.fill(placeholders, -2); // -2 is unprocessed
                for (int i = 0; i < input.length; i++) {
                    if (placeholders[i] > -2) continue; // already processed
                    int cur = i;
                    StringBuilder loop = new StringBuilder();
                    do {
                        if (!loop.isEmpty()) loop.append("->");
                        loop.append(cur);
                        if (placeholders[cur] != -2) {
                            GLog.n("infinite parameter loop: " + loop);
                            return true;
                        }
                        Matcher matcher = argPattern.matcher(input[cur]);
                        cur = placeholders[cur] = matcher.matches() ? Integer.parseInt(matcher.group(1)) : -1;
                    } while(cur >= 0 && placeholders[cur] != -1);
                }
                String[] lines = macro.split("\n");
                for (String line : lines) {
                    try {
                        while (true) {
                            Matcher argMatcher = argPattern.matcher(line);
                            if (argMatcher.find()) {
                                int index = Integer.parseInt(argMatcher.group(1));
                                argMatcher.reset();
                                line = argMatcher.replaceFirst(input[index]);
                                continue;
                            }
                            break;
                        }
                        String[] line_input = handleVariables(line.split(" "));
                        if (line_input == null) break; // fixme should also indicate end of parsing
                        // todo fix for when command isn't actually...given
                        GLog.newLine();
                        GLog.i("> " + line);

                        // interpret until we can't
                        if (!interpret(line_input)) {
                            return true;
                        }
                    } catch (Exception ex) {
                        reportException(ex);
                        break;
                    }
                }
                return true;
            }

            // todo have redirect-able output for better logging
            // command logic
            // returns true if another command is safely called after it.
            // errors generally return false to stop macro flow.
            private boolean interpret(String... input) {
                Command command = Command.get(input[0]);

                if (command == null) {
                    // fixme drawbacks of the current system make it impossible to verify macro call safety
                    if (handleMacro(input)) {
                        return true; // dig your own grave...
                    }
                    GLog.w("\"" + input[0] + "\" is not a valid command.");
                    return false;
                }

                if(command == Command.CHANGES) {
                    GameScene.show(new HelpWindow(CHANGELOG));
                }
                else if(command == Command.HELP) {
                    String output = null;
                    boolean all = false;
                    if (input.length > 1) {
                        // we only care about the initial argument.
                        Command cmd = Command.get(input[1]);
                        if (cmd != null) output = cmd.fullDocumentation(trie);
                        else all = input[1].equalsIgnoreCase("all");
                    }
                    if (output == null) {
                        StringBuilder builder = new StringBuilder();
                        for (Command cmd : Command.values()) {
                            if (all) {
                                // extensive. help is omitted because we are using help.
                                if (cmd != Command.HELP) {
                                    builder.append("\n\n")
                                            .append(cmd.fullDocumentation(trie, false));
                                }
                            } else {
                                // use documentation. (show syntax in addition to description)
                                builder.append('\n').appendLine(cmd.documentation());
                            }
                        }
                        output = builder.toString().trim();
                    }
                    GameScene.show(new HelpWindow(output));
                    return false;
                }
                else if (command == Command.MACRO) {
                    getMacros();
                    if (input.length == 1) {
                        StringBuilder msg = new StringBuilder();
                        msg.append(command.documentation());
                