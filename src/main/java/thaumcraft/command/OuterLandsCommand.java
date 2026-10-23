package thaumcraft.command;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.world.dim.MazeHandler;
import thaumcraft.world.dim.OuterLands;

@EventBusSubscriber(modid = Thaumcraft.MODID)
public final class OuterLandsCommand {
    private OuterLandsCommand() {
    }

    @SubscribeEvent
    static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal(Thaumcraft.MODID)
            .requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS))
            .then(Commands.literal("outer")
                .then(Commands.literal("prepare")
                    .then(Commands.argument("pos", BlockPosArgument.blockPos())
                        .executes(context -> maze(context, true))))
                .then(Commands.literal("info")
                    .then(Commands.argument("pos", BlockPosArgument.blockPos())
                        .executes(context -> maze(context, false))))));
    }

    private static int maze(CommandContext<CommandSourceStack> context, boolean prepare) {
        CommandSourceStack source = context.getSource();
        ServerLevel outer = source.getServer().getLevel(OuterLands.OUTER_LANDS);
        if (outer == null) {
            source.sendFailure(Component.literal("Outer Lands dimension is unavailable"));
            return 0;
        }
        BlockPos pos = BlockPosArgument.getBlockPos(context, "pos");
        int x = pos.getX() >> 4;
        int z = pos.getZ() >> 4;
        boolean ready = prepare
            ? MazeHandler.ensureMaze(outer, x, z, outer.getRandom().nextLong())
            : MazeHandler.isReady(outer, x, z);
        source.sendSuccess(() -> Component.literal("Outer Lands maze " + x + "," + z + ": " + (ready ? "ready" : "pending")), false);
        BlockPos entrance = MazeHandler.entrancePos(outer, x, z);
        if (entrance != null) {
            source.sendSuccess(() -> Component.literal("Entrance: " + entrance.toShortString()), false);
        }
        return ready ? 1 : 0;
    }
}
