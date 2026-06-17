package studio.fantasyit.path_script.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import studio.fantasyit.path_script.action.IAction;

import java.util.*;

public class PathSet {
    final Map<BlockPos, PathNode> map;
    final BlockPos startPos;
    final List<PathNode> nodes;
    final Map<BlockPos, Set<BlockPos>> parentMap;


    public static final Codec<PathSet> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BlockPos.CODEC.fieldOf("start_pos").forGetter(PathSet::getStartPos),
                    PathNode.CODEC.listOf().fieldOf("nodes").forGetter(PathSet::getNodes)
            ).apply(instance, PathSet::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, PathSet> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, PathSet::getStartPos,
            PathNode.STREAM_CODEC.apply(ByteBufCodecs.list()), PathSet::getNodes,
            PathSet::new
    );

    public PathSet(BlockPos startPos, List<PathNode> nodes) {
        this.startPos = startPos;
        this.nodes = nodes;
        this.map = new HashMap<>();
        for (PathNode node : nodes) {
            map.put(node.pos(), node);
        }
        parentMap = new HashMap<>();
        Deque<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        queue.add(startPos);
        visited.add(startPos);
        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            for (BlockPos nextPos : getNext(current)) {
                parentMap.computeIfAbsent(nextPos, k -> new HashSet<>()).add(current);
                if (!visited.contains(nextPos)) {
                    visited.add(nextPos);
                    queue.add(nextPos);
                }
            }
        }
    }

    public Set<BlockPos> getParent(BlockPos pos) {
        return parentMap.getOrDefault(pos, Collections.emptySet());
    }

    public boolean isAncestor(BlockPos ancestor, BlockPos descendant) {
        Set<BlockPos> visited = new HashSet<>();
        Deque<BlockPos> stack = new ArrayDeque<>();
        stack.push(descendant);
        while (!stack.isEmpty()) {
            BlockPos current = stack.pop();
            if (current.equals(ancestor)) {
                return true;
            }
            if (!visited.add(current)) {
                continue;
            }
            for (BlockPos parent : getParent(current)) {
                if (!visited.contains(parent)) {
                    stack.push(parent);
                }
            }
        }
        return false;
    }

    public BlockPos getStartPos() {
        return startPos;
    }

    public List<PathNode> getNodes() {
        return nodes;
    }

    public boolean contains(BlockPos pos) {
        return map.containsKey(pos);
    }

    public PathSet addNode(BlockPos currentPos, BlockPos newPos) {
        List<PathNode> newNodes = new ArrayList<>(nodes);
        if (currentPos != null && map.containsKey(currentPos)) {
            PathNode oldNode = map.get(currentPos);
            List<BlockPos> newNext = new ArrayList<>(oldNode.next());
            newNext.add(newPos);
            newNodes.replaceAll(n -> n.pos().equals(currentPos) ? new PathNode(currentPos, newNext, oldNode.actions()) : n);
        }
        newNodes.add(new PathNode(newPos, List.of(), List.of()));
        return new PathSet(startPos, newNodes);
    }

    public PathSet addEdge(BlockPos from, BlockPos to) {
        if (!map.containsKey(from) || !map.containsKey(to)) {
            return this;
        }
        PathNode fromNode = map.get(from);
        if (fromNode.next().contains(to)) {
            return this;
        }
        List<PathNode> newNodes = new ArrayList<>();
        for (PathNode node : nodes) {
            if (node.pos().equals(from)) {
                List<BlockPos> newNext = new ArrayList<>(node.next());
                newNext.add(to);
                newNodes.add(new PathNode(node.pos(), newNext, node.actions()));
            } else {
                newNodes.add(node);
            }
        }
        return new PathSet(startPos, newNodes);
    }

    public List<BlockPos> getNext(BlockPos pos) {
        PathNode node = map.get(pos);
        return node == null ? Collections.emptyList() : node.next();
    }

    public PathNode getNearest(BlockPos pos) {
        PathNode nearest = null;
        double nearestDist = Double.MAX_VALUE;
        for (PathNode node : nodes) {
            double dist = node.pos().distSqr(pos);
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = node;
            }
        }
        return nearest;
    }

    public List<IAction> getAction(BlockPos pos) {
        PathNode node = map.get(pos);
        return node == null ? Collections.emptyList() : node.actions();
    }

    public PathSet removeNode(BlockPos pos) {
        if (pos.equals(startPos)) {
            return this;
        }
        if (!map.containsKey(pos)) {
            return this;
        }
        List<PathNode> newNodes = new ArrayList<>();
        for (PathNode node : nodes) {
            if (node.pos().equals(pos)) {
                continue;
            }
            List<BlockPos> newNext = new ArrayList<>();
            for (BlockPos nextPos : node.next()) {
                if (!nextPos.equals(pos)) {
                    newNext.add(nextPos);
                }
            }
            newNodes.add(new PathNode(node.pos(), newNext, node.actions()));
        }
        if (newNodes.isEmpty()) {
            return null;
        }
        return new PathSet(startPos, newNodes);
    }

    public PathSet addAction(BlockPos pos, IAction action) {
        if (!map.containsKey(pos)) {
            return this;
        }
        List<PathNode> newNodes = new ArrayList<>();
        for (PathNode node : nodes) {
            if (node.pos().equals(pos)) {
                List<IAction> newActions = new ArrayList<>(node.actions());
                newActions.add(action);
                newNodes.add(new PathNode(node.pos(), node.next(), newActions));
            } else {
                newNodes.add(node);
            }
        }
        return new PathSet(startPos, newNodes);
    }

    public PathSet setActions(BlockPos pos, List<IAction> actions) {
        if (!map.containsKey(pos)) {
            return this;
        }
        List<PathNode> newNodes = new ArrayList<>();
        for (PathNode node : nodes) {
            if (node.pos().equals(pos)) {
                newNodes.add(new PathNode(node.pos(), node.next(), actions));
            } else {
                newNodes.add(node);
            }
        }
        return new PathSet(startPos, newNodes);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != PathSet.class) return false;
        PathSet other = (PathSet) obj;
        return this.startPos.equals(other.startPos) && this.nodes.equals(other.nodes);
    }

    @Override
    public int hashCode() {
        return startPos.hashCode() ^ nodes.hashCode();
    }

    public PathNode getNode(BlockPos nextPos) {
        return map.get(nextPos);
    }
}
