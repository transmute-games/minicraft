package minicraft.network;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Wire protocol shared by client and dedicated server (adapted from Minicraft+ Online).
 */
public interface MinicraftProtocol
{
    int PORT = 4225;

    enum InputType
    {
        INVALID, PING, USERNAMES, LOGIN, GAME, INIT, LOAD, TILES, ENTITIES, TILE, ENTITY, PLAYER, MOVE, ADD, REMOVE, DISCONNECT, SAVE, NOTIFY, INTERACT, PUSH, PICKUP, CHESTIN, CHESTOUT, ADDITEMS, BED, POTION, HURT, DIE, RESPAWN, DROP, STAMINA, SHIRT, STOPFISHING;

        public static final InputType[] values = InputType.values();
        public static final List<InputType> serverOnly = Arrays.asList(INIT, TILES, ENTITIES, ADD, REMOVE, HURT, GAME, ADDITEMS, STAMINA, STOPFISHING);
        public static final List<InputType> entityUpdates = Arrays.asList(ENTITY, ADD, REMOVE);
        public static final List<InputType> tileUpdates = Collections.singletonList(TILE);
    }

    /**
     * Encode type as a non-null char (ordinal + 1) for the null-delimited stream framing.
     */
    static char encodeType(InputType type)
    {
        return (char) (type.ordinal() + 1);
    }

    static InputType getInputType(char idxChar)
    {
        int idx = idxChar - 1;
        if (idx < InputType.values.length && idx >= 0)
        {
            return InputType.values[idx];
        }
        return null;
    }

    void endConnection();

    boolean isConnected();
}
