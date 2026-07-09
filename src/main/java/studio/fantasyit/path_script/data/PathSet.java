package studio.fantasyit.path_script.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;
import studio.fantasyit.path_script.Config;
import studio.fantasyit.path_script.action.IAction;

import java.util.*;

public class PathSet {
    final Map<BlockPos, PathNode> map;
    final List<PathNode> nodes;
    final Map<BlockPos, Set<BlockPos>> parentMap;


    public static final Codec<PathSet> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    PathNode.CODEC.listOf().fieldOf("nodes").forGetter(PathSet::getNodes)
            ).apply(instance, PathSet::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, PathSet> STREAM_CODEC = StreamCodec.composite(
            PathNode.STREAM_CODEC.apply(ByteBufCodecs.list()), PathSet::getNodes,
            PathSet::new
    );

    public PathSet(List<PathNode> nodes) {
        this.nodes = nodes;
        this.map = new HashMap<>();
        for (PathNode node : nodes) {
            map.put(node.pos(), node);
        }
        parentMap = new HashMap<>();
        for (PathNode node : nodes) {
            for (BlockPos nextPos : node.next()) {
                parentMap.computeIfAbsent(nextPos, k -> new HashSet<>()).add(node.pos());
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

    public int distanceOnPath(BlockPos pos1, BlockPos pos2) {
        Map<BlockPos, Integer> visited = new HashMap<>();
        Deque<BlockPos> stack = new ArrayDeque<>();
        visited.put(pos1, 0);
        stack.push(pos1);
        while (!stack.isEmpty()) {
            BlockPos current = stack.pop();
            if (current.equals(pos2)) {
                return visited.getOrDefault(current, 0);
            }
            for (BlockPos parent : getParent(current)) {
                if (!visited.containsKey(parent)) {
                    visited.put(parent, visited.getOrDefault(current, 0) + 1);
                    stack.push(parent);
                }
            }
        }
        return -1;
    }

    public BlockPos getStartPos() {
        Set<BlockPos> hasParent = new HashSet<>();
        for (PathNode node : nodes) {
            hasParent.addAll(node.next());
        }
        for (PathNode node : nodes) {
            if (!hasParent.contains(node.pos())) {
                return node.pos();
            }
        }
        return null;
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
        return new PathSet(newNodes);
    }

    public boolean hasEdge(BlockPos from, BlockPos to) {
        if (!map.containsKey(from) || !map.containsKey(to)) {
            return false;
        }
        PathNode fromNode = map.get(from);
        return fromNode.next().contains(to);
    }

    public PathSet removeEdge(BlockPos from, BlockPos to) {
        if (!map.containsKey(from) || !map.containsKey(to)) {
            return this;
        }
        PathNode fromNode = map.get(from);
        if (!fromNode.next().contains(to)) {
            return this;
        }
        List<PathNode> newNodes = new ArrayList<>();
        for (PathNode node : nodes) {
            if (node.pos().equals(from)) {
                newNodes.add(new PathNode(node.pos(), new ArrayList<>(node.next().stream().filter(t -> !t.equals(to)).toList()), node.actions()));
            } else {
                newNodes.add(node);
            }
        }
        return new PathSet(newNodes);
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
        return new PathSet(newNodes);
    }

    public List<BlockPos> getNext(BlockPos pos) {
        PathNode node = map.get(pos);
        return node == null ? Collections.emptyList() : node.next();
    }

    public PathNode getNearest(BlockPos pos) {
        return getNearest(pos, null);
    }

    public PathNode getNearest(BlockPos pos, @Nullable PathNode referenceNode) {
        PathNode nearest = null;
        double nearestDist = Double.MAX_VALUE;
        for (PathNode node : nodes) {
            double dy = node.pos().getY() - pos.getY();
            if (Math.abs(dy) > Config.yMaxHeight) continue;

            double yWeight = dy >= 0 ? Config.yPositiveWeight : Config.yNegativeWeight;
            double dx = node.pos().getX() - pos.getX();
            double dz = node.pos().getZ() - pos.getZ();
            double dist = dx * dx + dz * dz + (yWeight * dy) * (yWeight * dy);
            if (referenceNode != null) {
                int nr;
                if (isAncestor(node.pos(), referenceNode.pos())) {
                    nr = distanceOnPath(referenceNode.pos(), node.pos());
                } else if (isAncestor(referenceNode.pos(), node.pos())) {
                    nr = distanceOnPath(node.pos(), referenceNode.pos());
                } else continue;
                dist += nr * Config.nodeDistWeight;
            }
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
        return new PathSet(newNodes);
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
        return new PathSet(newNodes);
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
        return new PathSet(newNodes);
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != PathSet.class) return false;
        PathSet other = (PathSet) obj;
        return this.nodes.equals(other.nodes);
    }

    @Override
    public int hashCode() {
        return nodes.hashCode();
    }

    public PathNode getNode(BlockPos nextPos) {
        return map.get(nextPos);
    }
}
