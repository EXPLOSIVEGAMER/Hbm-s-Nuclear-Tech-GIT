package com.hbm.uninos;

import com.hbm.util.fauxpointtwelve.BlockPos;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraft.world.World;
import scala.tools.nsc.doc.base.comment.Link;

import java.util.*;

public class NetworkPathFinder {
	public static class BFS {
		public static List<BlockPos> findPath(World world, BlockPos start, BlockPos goal, INetworkProvider provider) {
			GenNode startNode = getNode(world, start, provider);
			GenNode goalNode = getNode(world, goal, provider);
			if (startNode == null || goalNode == null) return null;

			Map<GenNode, GenNode> prev = new HashMap<>();
			Deque<GenNode> queue = new ArrayDeque<>();

			prev.put(startNode, null);
			queue.add(startNode);

			while (!queue.isEmpty()) {
				GenNode current = queue.poll();
				if (current == goalNode) break;

				for (DirPos con : current.connections) {
					GenNode next = getNode(world, con , provider);
					if (next == null) continue;
					if (prev.containsKey(next)) continue;
					if (!UniNodespace.checkConnection(next, con, false)) continue;

					prev.put(next, current);
					queue.add(next);
				}
			}

			if (!prev.containsKey(goalNode)) return null;

			LinkedList<BlockPos> path = new LinkedList<>();
			for (GenNode n = goalNode; n != null; n = prev.get(n)) {
				path.addFirst(n.positions[0]);
			}
			return path;
		}
	}

	public static GenNode getNode(World world, BlockPos pos, INetworkProvider provider) {
		GenNode node = UniNodespace.getNode(world, pos.getX(), pos.getY(), pos.getZ(), provider);
		return node == null || node.expired ? null : node;
	}
}
