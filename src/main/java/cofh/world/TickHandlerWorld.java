package cofh.world;

import cofh.CoFHCore;
import cofh.api.world.ISuspendedFeatureGeneration;
import cofh.util.position.ChunkCoord;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.WorldTickEvent;
import cpw.mods.fml.relauncher.Side;

import gnu.trove.map.TMap;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

import java.util.ArrayDeque;
import java.util.Random;

import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;

public class TickHandlerWorld {

	public static TickHandlerWorld instance = new TickHandlerWorld();

	public static TMap<Integer, ArrayDeque<RetroChunkCoord>> chunksToGen = new THashMap<Integer, ArrayDeque<RetroChunkCoord>>();
	public static TMap<Integer, THashSet<ISuspendedFeatureGeneration>> suspendedGenerations = new THashMap<Integer, THashSet<ISuspendedFeatureGeneration>>();

	@SubscribeEvent
	public void tickEnd(WorldTickEvent event) {

		if (event.phase != Phase.END | event.side != Side.SERVER) {
			return;
		}
		World world = event.world;
		int dim = world.provider.dimensionId;
		THashSet<ISuspendedFeatureGeneration> suspended = suspendedGenerations.get(Integer.valueOf(dim));
		ArrayDeque<RetroChunkCoord> chunks = chunksToGen.get(Integer.valueOf(dim));

		if (suspended != null && suspended.size() > 0) {
			long worldSeed = world.getSeed();
			Random rand = new Random(worldSeed);
			long xSeed = rand.nextLong() >> 2 + 1L;
			long zSeed = rand.nextLong() >> 2 + 1L;
			ChunkCoord c = null;
			ISuspendedFeatureGeneration gen = null;
			for (ISuspendedFeatureGeneration g : suspended) {
				for (ChunkCoord coord : g.getChunksToGen()) {
					if (world.getChunkFromChunkCoords(coord.chunkX, coord.chunkZ).isChunkLoaded) {
						c = coord;
						gen = g;
						suspended.remove(g);
						break;
					}
				}
				if (c != null) break;
			}
			if (c != null) {
				rand.setSeed(xSeed * c.chunkX + zSeed * c.chunkZ ^ worldSeed);
				gen.generateFeature(rand, c.chunkX, c.chunkZ, world, true);
				if (gen.isCompleted()) {
					suspended.remove(gen);
					suspendedGenerations.put(dim, suspended);
				}
			}
		}

		if (chunks != null && chunks.size() > 0) {
			RetroChunkCoord r = chunks.pollFirst();
			ChunkCoord c = r.coord;
			CoFHCore.log.info("RetroGening " + c.toString() + ".");
			long worldSeed = world.getSeed();
			Random rand = new Random(worldSeed);
			long xSeed = rand.nextLong() >> 2 + 1L;
			long zSeed = rand.nextLong() >> 2 + 1L;
			rand.setSeed(xSeed * c.chunkX + zSeed * c.chunkZ ^ worldSeed);
			WorldHandler.instance.generateWorld(rand, r, world, false);
			chunksToGen.put(Integer.valueOf(dim), chunks);
		}
	}

	public static class RetroChunkCoord {

		private static final THashSet<String> emptySet = new THashSet<String>(0);
		public final ChunkCoord coord;
		public final THashSet<String> generatedFeatures;

		public RetroChunkCoord(ChunkCoord pos, NBTTagList features) {

			coord = pos;
			if (features == null) {
				generatedFeatures = emptySet;
			} else {
				int i = 0, e = features.tagCount();
				generatedFeatures = new THashSet<String>(e);
				for (; i < e; ++i) {
					generatedFeatures.add(features.getStringTagAt(i));
				}
			}
		}
	}

}
