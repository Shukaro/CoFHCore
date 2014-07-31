package cofh.api.world;

import cofh.util.position.ChunkCoord;
import net.minecraft.world.World;

import java.util.Random;
import java.util.Set;

public interface ISuspendedFeatureGeneration
{
	/**
	 * Returns the parent feature of this paused generation
	 */
	public IFeatureGenerator getFeature();

	/**
	 * Returns the set of chunks this generation needs to process
	 */
	public Set<ChunkCoord> getChunksToGen();

	/**
	 * @return True if generation is finished
	 */
	public boolean isCompleted();

	/**
	 * Generates the world feature.
	 *
	 * @param random
	 *			Random derived from the world seed.
	 * @param chunkX
	 *			Minimum X chunk-coordinate of the chunk. (x16 for block coordinate)
	 * @param chunkZ
	 *			Minimum Z chunk-coordinate of the chunk. (x16 for block coordinate)
	 * @param world
	 *			The world to generate in.
	 * @param newGen
	 *			True on initial generation, false on retrogen.
	 * @return True if generation happened, false otherwise.
	 */
	public boolean generateFeature(Random random, int chunkX, int chunkZ, World world, boolean newGen);
}
