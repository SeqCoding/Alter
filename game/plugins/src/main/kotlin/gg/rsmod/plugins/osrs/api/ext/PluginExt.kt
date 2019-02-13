package gg.rsmod.plugins.osrs.api.ext

import gg.rsmod.game.fs.def.NpcDef
import gg.rsmod.game.model.*
import gg.rsmod.game.model.entity.GameObject
import gg.rsmod.game.model.entity.Npc
import gg.rsmod.game.model.entity.Pawn
import gg.rsmod.game.model.entity.Player
import gg.rsmod.game.model.item.Item
import gg.rsmod.game.plugin.Plugin
import gg.rsmod.plugins.osrs.api.Skills

/**
 * A decoupled file that holds extensions and helper functions, related to plugins,
 * that can be used throughout plugins.
 *
 * @author Tom <rspsmods@gmail.com>
 */

/**
 * The child id of the chat box in the gameframe interface. This can change
 * with revision.
 */
const val CHATBOX_CHILD = 561

/**
 * The default action that will occur when interrupting or finishing a dialog.
 */
private val closeDialog = { it: Plugin ->
    it.player().closeComponent(parent = 162, child = CHATBOX_CHILD)
}

/**
 * Gets the [ctx] as a [Pawn]. If [ctx] is not a [Pawn], a cast exception
 * will be thrown.
 */
fun Plugin.pawn(): Pawn = ctx as Pawn

/**
 * Gets the [ctx] as a [Player]. If [ctx] is not a [Player], a cast exception
 * will be thrown.
 */
fun Plugin.player(): Player = ctx as Player

/**
 * Gets the [ctx] as an [Npc]. If [ctx] is not an [Npc], a cast exception
 * will be thrown.
 */
fun Plugin.npc(): Npc = ctx as Npc

fun Plugin.getCommandArgs(): Array<String> = pawn().attr[COMMAND_ARGS_ATTR]!!

fun Plugin.getInteractingSlot(): Int = pawn().attr[INTERACTING_SLOT_ATTR]!!

fun Plugin.getInteractingItem(): Item = pawn().attr[INTERACTING_ITEM]!!.get()!!

fun Plugin.getInteractingItemId(): Int = pawn().attr[INTERACTING_ITEM_ID]!!

fun Plugin.getInteractingItemSlot(): Int = pawn().attr[INTERACTING_ITEM_SLOT]!!

fun Plugin.getInteractingOption(): Int = pawn().attr[INTERACTING_OPT_ATTR]!!

fun Plugin.getInteractingGameObj(): GameObject = pawn().attr[INTERACTING_OBJ_ATTR]!!.get()!!

fun Plugin.getInteractingNpc(): Npc = pawn().attr[INTERACTING_NPC_ATTR]!!.get()!!

/**
 * Prompts the player with options.
 *
 * @return
 * The id of the option chosen. The id can range from [1] inclusive to [options.size] inclusive.
 */
suspend fun Plugin.options(vararg options: String, title: String = "Select an Option"): Int {
    val p = player()

    p.setVarbit(5983, 1)
    p.runClientScript(2379)
    p.setInterfaceEvents(parent = 219, child = 1, from = 1, to = options.size, setting = 1)
    p.openInterface(parent = 162, child = CHATBOX_CHILD, component = 219)
    p.runClientScript(58, title, options.joinToString("|"))

    interruptAction = closeDialog
    waitReturnValue()
    interruptAction?.invoke(this)

    return requestReturnValue as? Int ?: -1
}

/**
 * Prompts the player with an input dialog where they can only enter an integer.
 *
 * @return
 * The integer input.
 */
suspend fun Plugin.inputInteger(description: String = "Enter amount"): Int {
    val p = player()

    p.runClientScript(108, description)

    waitReturnValue()

    return requestReturnValue as? Int ?: -1
}

/**
 * Sends a normal message dialog.
 *
 * @message
 * The message to render on the dialog box.
 *
 * @lineSpacing
 * The spacing, in pixels, in between each line that will be rendered on the
 * dialog box.
 */
suspend fun Plugin.messageDialog(message: String, lineSpacing: Int = 31) {
    val p = player()

    p.setComponentText(parent = 229, child = 1, text = message)
    p.setComponentText(parent = 229, child = 2, text = "Click here to continue")
    p.setInterfaceEvents(parent = 229, child = 2, range = -1..-1, setting = 1)
    p.openInterface(parent = 162, child = CHATBOX_CHILD, component = 229)
    p.runClientScript(600, 1, 1, lineSpacing, 15007745)

    interruptAction = closeDialog
    waitReturnValue()
    interruptAction?.invoke(this)
}

/**
 * Send an npc dialog.
 *
 * @param message
 * The message to render on the dialog box.
 *
 * @npc
 * The npc id which represents the npc that will be drawn on the dialog box.
 *
 * @animation
 * The animation id of the npc's head model.
 *
 * @title
 * The title of the dialog, if left as [null], the npc's name will be used.
 */
suspend fun Plugin.npcDialog(message: String, npc: Int, animation: Int = 588, title: String? = null) {
    val p = player()

    var dialogTitle = title
    if (dialogTitle == null) {
        dialogTitle = p.world.definitions.get(NpcDef::class.java, npc).name
    }

    p.setComponentNpcHead(parent = 231, child = 1, npc = npc)
    p.setComponentText(parent = 231, child = 2, text = dialogTitle)
    p.setComponentText(parent = 231, child = 3, text = "Click here to continue")
    p.setComponentText(parent = 231, child = 4, text = message)
    p.setComponentAnim(parent = 231, child = 1, anim = animation)
    p.setInterfaceEvents(parent = 231, child = 3, from = -1, to = -1, setting = 1)
    p.openInterface(parent = 162, child = CHATBOX_CHILD, component = 231)
    p.runClientScript(600, 1, 1, 16, 15138820)

    interruptAction = closeDialog
    waitReturnValue()
    interruptAction?.invoke(this)
}

/**
 * Sends a single item dialog.
 *
 * @param message
 * The message to render on the dialog box.
 *
 * @param item
 * The id of the item to show on the dialog.
 *
 * @param amount
 * The amount of the item to show on the dialog.
 */
suspend fun Plugin.itemDialog(message: String, item: Int, amount: Int = 1) {
    val p = player()

    p.setComponentItem(parent = 193, child = 1, item = item, amountOrZoom = amount)
    p.setComponentText(parent = 193, child = 2, text = message)
    p.setComponentText(parent = 193, child = 3, text = "Click here to continue")
    p.setInterfaceEvents(parent = 193, child = 3, range = -1..-1, setting = 1)
    p.setInterfaceEvents(parent = 193, child = 4, range = -1..-1, setting = 0)
    p.setInterfaceEvents(parent = 193, child = 5, range = -1..-1, setting = 0)
    p.openInterface(parent = 162, child = CHATBOX_CHILD, component = 193)

    interruptAction = closeDialog
    waitReturnValue()
    interruptAction?.invoke(this)
}

suspend fun Plugin.doubleItemDialog(message: String, item1: Int, item2: Int, amount1: Int = 1, amount2: Int = 1) {
    val p = player()

    p.setComponentItem(parent = 11, child = 1, item = item1, amountOrZoom = amount1)
    p.setComponentText(parent = 11, child = 2, text = message)
    p.setComponentItem(parent = 11, child = 3, item = item2, amountOrZoom = amount2)
    p.setComponentText(parent = 11, child = 4, text = "Click here to continue")
    p.setInterfaceEvents(parent = 11, child = 4, range = -1..-1, setting = 1)
    p.openInterface(parent = 162, child = CHATBOX_CHILD, component = 11)

    interruptAction = closeDialog
    waitReturnValue()
    interruptAction?.invoke(this)
}

suspend fun Plugin.searchItemDialog(message: String): Int {
    val p = player()

    p.runClientScript(750, message, 1, -1)

    waitReturnValue()

    return requestReturnValue as? Int ?: -1
}

suspend fun Plugin.levelUpDialog(skill: Int, levels: Int) {
    val p = player()

    if (skill != Skills.HUNTER) {
        val children = mapOf(
                Skills.AGILITY to 4,
                Skills.ATTACK to 6,
                Skills.CONSTRUCTION to 9,
                Skills.COOKING to 12,
                Skills.CRAFTING to 14,
                Skills.DEFENCE to 17,
                Skills.FARMING to 19,
                Skills.FIREMAKING to 21,
                Skills.FISHING to 23,
                Skills.FLETCHING to 25,
                Skills.HERBLORE to 28,
                Skills.HITPOINTS to 30,
                Skills.MAGIC to 32,
                Skills.MINING to 34,
                Skills.PRAYER to 36,
                Skills.RANGED to 38,
                Skills.RUNECRAFTING to 41,
                Skills.SLAYER to 43,
                Skills.SMITHING to 45,
                Skills.STRENGTH to 47,
                Skills.THIEVING to 49,
                Skills.WOODCUTTING to 51)

        children.forEach { key, value ->
            p.setComponentHidden(parent = 233, child = value, hidden = skill != key)
        }

        val skillName = Skills.getSkillName(p.world, skill)
        val initialChar = Character.toLowerCase(skillName.toCharArray().first())
        val vowel = initialChar == 'a' || initialChar == 'e' || initialChar == 'i' || initialChar == 'o' || initialChar == 'u'
        val levelFormat = if (levels == 1) (if (vowel) "an" else "a") else "$levels"

        p.setComponentText(parent = 233, child = 1, text = "<col=000080>Congratulations, you just advanced $levelFormat $skillName ${"level".plural(levels)}.")
        p.setComponentText(parent = 233, child = 2, text = "Your $skillName level is now ${p.getSkills().getMaxLevel(skill)}.")
        p.setComponentText(parent = 233, child = 3, text = "Click here to continue")
        p.openInterface(parent = 162, child = CHATBOX_CHILD, component = 233)
    } else {
        val levelFormat = if (levels == 1) "a" else "$levels"

        p.setInterfaceEvents(parent = 132, child = 3, from = -1, to = -1, setting = 1)
        p.setInterfaceEvents(parent = 132, child = 4, from = -1, to = -1, setting = 0)
        p.setInterfaceEvents(parent = 132, child = 5, from = -1, to = -1, setting = 0)

        p.setComponentItem(parent = 193, child = 1, item = 9951, amountOrZoom = 400)

        p.setComponentText(parent = 193, child = 2, text = "<col=000080>Congratulations, you've just advanced $levelFormat Hunter ${"level".plural(levels)}." +
                "<col=000000><br><br>Your Hunter level is now ${p.getSkills().getMaxLevel(skill)}.")
        p.setComponentText(parent = 193, child = 3, text = "Click here to continue")
        p.setComponentText(parent = 193, child = 4, text = "")
        p.setComponentText(parent = 193, child = 5, text = "")

        p.openInterface(parent = 162, child = CHATBOX_CHILD, component = 193)
    }

    interruptAction = closeDialog
    waitReturnValue()
    interruptAction?.invoke(this)
}