package net.deviantevil.moderation;

import org.bukkit.command.CommandSender;

final class Moderation {
    
    static boolean kick(String target, CommandSender issuer, String reason)
        throws InvalidTargetException {
        return true;
    }

}

class InvalidTargetException extends Exception {
    private static final long serialVersionUID = 8567459953190717241L;
};
