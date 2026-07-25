package com.touhoumaid.eatproperly.command;

import com.touhoumaid.eatproperly.EatProperlyMod;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = EatProperlyMod.MOD_ID)
public final class CommandEvents {
    private CommandEvents() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        EatProperlyCommands.register(event.getDispatcher());
    }
}
