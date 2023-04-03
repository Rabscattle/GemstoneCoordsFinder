package com.github.rabscattle.gemstonecoordsfinder.structs;

import net.kyori.adventure.text.Component;
import com.github.rabscattle.gemstonecoordsfinder.GemstoneCoordsFinder;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Consumer;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public class GemstoneScanner {
    private static final Vector CH_START = new Vector(202, 31, 202);
    private static final Vector CH_END = new Vector(823, 188, 823);

    private static final Vector SPAWN_START = new Vector(456, 0, 566);
    private static final Vector SPAWN_END = new Vector(567, 190, 456);

    private static final GemstoneCoordsFinder plugin = GemstoneCoordsFinder.getInstance();

    private final int blocksToScan;
    private final AtomicInteger blocksScanned = new AtomicInteger();
    private final AtomicBoolean inScan = new AtomicBoolean();
    private final Map<GemstoneType, Set<Waypoint>> result = new ConcurrentHashMap<>();
    private final World world;
    private final Consumer<Map<GemstoneType, Set<Waypoint>>> onFinish;
    private final long start;
    private final int blocksPerBatch;
    private final BoundingBox spawnArea;
    private final Queue<BukkitRunnable> inTask = new ConcurrentLinkedQueue<>();

    public GemstoneScanner(World world, Consumer<Map<GemstoneType, Set<Waypoint>>> onFinish) {
        this.world = world;
        this.onFinish = onFinish;
        this.start = System.currentTimeMillis();
        this.blocksPerBatch = plugin.getBlocksPerSecond();
        this.spawnArea = new BoundingBox(SPAWN_START.getX(), SPAWN_START.getY(), SPAWN_START.getZ(), SPAWN_END.getX(), SPAWN_END.getY(), SPAWN_END.getZ());

        int xLength = CH_END.getBlockX() - CH_START.getBlockX();
        int yLength = CH_END.getBlockY() - CH_START.getBlockY();
        int zLength = CH_END.getBlockZ() - CH_START.getBlockZ();

        this.blocksToScan = xLength * yLength * zLength;
    }

    public void startScan() {
        new StatsReporter().runTaskTimer(plugin, 20, 20);

        CompletableFuture.runAsync(() -> {
            inScan.set(true);
            int xStart = CH_START.getBlockX();
            int yStart = CH_START.getBlockY();
            int zStart = CH_START.getBlockZ();

            int xEnd = CH_END.getBlockX();
            int yEnd = CH_END.getBlockY();
            int zEnd = CH_END.getBlockZ();

            for (int x = xStart; x <= xEnd; x++) {
                for (int y = yStart; y <= yEnd; y++) {
                    for (int z = zStart; z <= zEnd; z++) {
                        if (spawnArea.contains(x, y, z)) {
                            continue;
                        }
                        new WaypointRetriever(x, y, z).runTask(plugin);
                        if (((x - xStart) * (yEnd - yStart) * (zEnd - zStart) + (y - yStart) * (zEnd - zStart) + (z - zStart)) % blocksPerBatch == 0) {
                            try {
                                Thread.sleep(1000); // wait for 1 seconds after every batch of x blocks
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
            }

            while (!inTask.isEmpty()) {
                Bukkit.getLogger().log(Level.INFO, "Waiting for %d Tasks to finish".formatted(inTask.size()));
                try {
                    Thread.sleep(1000); // wait for 1 seconds after every batch of x blocks
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            inScan.set(false);
        }).whenComplete((unused, throwable) -> new FinishTask().runTask(plugin));
    }

    public boolean isInProgress() {
        return inScan.get();
    }

    public class StatsReporter extends BukkitRunnable {
        @Override
        public void run() {
            if (!inScan.get()) {
                cancel();
                return;
            }
            Bukkit.broadcast(Component.text("Scanned %d/%d (%.2f%%) Blocks".formatted(blocksScanned.get(), blocksToScan, (((float) blocksScanned.get()) / blocksToScan) * 100)));
        }
    }

    public class FinishTask extends BukkitRunnable {
        @Override
        public void run() {
            final float seconds = (System.currentTimeMillis() - start) / 1000f;
            Bukkit.broadcast(Component.text("Scanner finished in %.2f seconds".formatted(seconds)));
            GemstoneScanner.this.onFinish.accept(result);
        }
    }

    public class WaypointRetriever extends BukkitRunnable {
        final int x;
        final int y;
        final int z;

        public WaypointRetriever(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
            inTask.add(this);
        }

        @Override
        public void run() {
            try {
                if (!Bukkit.isPrimaryThread()) {
                    throw new RuntimeException("Not primary thread");
                }

                blocksScanned.incrementAndGet();
                final Block blockAt = world.getBlockAt(x, y, z);
                final GemstoneType gemstoneType = GemstoneType.ofMaterial(blockAt.getType());

                if (gemstoneType == null)
                    return;

                final Set<Waypoint> gemstoneWaypoints = result.computeIfAbsent(gemstoneType, gemstoneType1 -> new HashSet<>());
                gemstoneWaypoints.add(new Waypoint(x, y, z));
                if (plugin.isDebugMode())
                    Bukkit.getLogger().log(Level.INFO, "Found Gemstone [Type: %s] at [Location: %d, %d, %d]".formatted(gemstoneType.name(), x, y, z));
            } finally {
                inTask.remove(this);
            }
        }

    }
}
