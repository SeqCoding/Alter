package gg.rsmod.game.message.handler

import gg.rsmod.game.action.ObjectPathAction
import gg.rsmod.game.message.MessageHandler
import gg.rsmod.game.message.impl.OpLoc1Message
import gg.rsmod.game.model.*
import gg.rsmod.game.model.entity.Client
import gg.rsmod.game.model.entity.GameObject
import gg.rsmod.game.model.entity.Player
import java.lang.ref.WeakReference

/**
 * @author Tom <rspsmods@gmail.com>
 */
class OpLoc1Handler : MessageHandler<OpLoc1Message> {

    override fun handle(client: Client, message: OpLoc1Message) {
        /**
         * If player can't move, we don't do anything.
         */
        if (!client.lock.canMove()) {
            return
        }

        /**
         * If tile is too far away, don't process it.
         */
        val tile = Tile(message.x, message.z, client.tile.height)
        if (!tile.viewableFrom(client.tile, Player.TILE_VIEW_DISTANCE)) {
            return
        }

        /**
         * Get the region chunk that the object would belong to.
         */
        val chunk = client.world.chunks.getOrCreate(tile)
        val obj = chunk.getEntities<GameObject>(tile, EntityType.STATIC_OBJECT, EntityType.DYNAMIC_OBJECT).firstOrNull { it.id == message.id }

        /**
         * [GameObject] doesn't exist in the region.
         */
        if (obj == null) {
            logVerificationFail(client, "Object action 1: id=%d, x=%d, z=%d, movement=%d", message.id, message.x, message.z, message.movementType)
            return
        }

        log(client, "Object action 1: id=%d, x=%d, z=%d, movement=%d", message.id, message.x, message.z, message.movementType)

        client.interruptPlugins()
        client.resetInteractions()

        if (message.movementType == 1 && client.world.privileges.isEligible(client.privilege, Privilege.ADMIN_POWER)) {
            val def = obj.getDef(client.world.definitions)
            client.teleport(client.world.findRandomTileAround(obj.tile, radius = 1, centreWidth = def.width, centreLength = def.length) ?: obj.tile)
        }

        client.attr.put(INTERACTING_OPT_ATTR, 1)
        client.attr.put(INTERACTING_OBJ_ATTR, WeakReference(obj))
        if (!client.world.plugins.executeCustomObjectPath(client, obj.id)) {
            client.executePlugin(ObjectPathAction.walkPlugin)
        }
    }
}