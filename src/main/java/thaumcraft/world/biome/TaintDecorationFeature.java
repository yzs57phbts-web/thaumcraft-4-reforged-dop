package thaumcraft.world.biome;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import thaumcraft.taint.TaintBiomes;

public final class TaintDecorationFeature extends Feature<NoneFeatureConfiguration> {
    public TaintDecorationFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        // An undecorated placed feature has an origin at Y=0. That may be a
        // cave biome even when the surface is tainted; check each surface site.
        TaintDecorationHook.decorate(level, context.random(), origin);
        return true;
    }
}
