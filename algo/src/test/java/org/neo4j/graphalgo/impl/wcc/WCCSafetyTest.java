/*
 * Copyright (c) 2017-2019 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.graphalgo.impl.wcc;

import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.neo4j.collection.primitive.PrimitiveLongIterable;
import org.neo4j.collection.primitive.PrimitiveLongIterator;
import org.neo4j.graphalgo.api.Graph;
import org.neo4j.graphalgo.api.RelationshipConsumer;
import org.neo4j.graphalgo.api.RelationshipIntersect;
import org.neo4j.graphalgo.api.NodeOrRelationshipProperties;
import org.neo4j.graphalgo.api.RelationshipWithPropertyConsumer;
import org.neo4j.graphalgo.core.loading.NullPropertyMap;
import org.neo4j.graphdb.Direction;
import org.neo4j.helpers.Exceptions;

import java.util.Collection;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.LongPredicate;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertSame;

@Deprecated
final class WCCSafetyTest {

    private static final WCC.Config ALGO_CONFIG = new WCC.Config(
            new NullPropertyMap(-1),
            Double.NaN
    );

    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    @ParameterizedTest
    @EnumSource(WCCType.class)
    void testWCCSafetyUnderFailure(WCCType wccType) {
        IllegalStateException error = new IllegalStateException("some error");
        Graph graph = new FlakyGraph(100, 10, new Random(42L), error);
        try {
            WCCHelper.run(
                    wccType,
                    graph,
                    10,
                    10,
                    ALGO_CONFIG
            );
        } catch (Throwable e) {
            assertSame(error, Exceptions.rootCause(e));
        }
    }

    private static final class FlakyGraph implements Graph {
        private final int nodes;
        private final int maxDegree;
        private final Random random;
        private final RuntimeException error;

        private FlakyGraph(int nodes, int maxDegree, Random random, RuntimeException error) {
            this.nodes = nodes;
            this.maxDegree = maxDegree;
            this.random = random;
            this.error = error;
        }

        @Override
        public void canRelease(final boolean canRelease) {
        }

        @Override
        public long nodeCount() {
            return nodes;
        }

        @Override
        public long relationshipCount() {
            return RELATIONSHIP_COUNT_NOT_SUPPORTED;
        }

        @Override
        public boolean isUndirected() {
            return false;
        }

        @Override
        public Direction getLoadDirection() {
            return Direction.OUTGOING;
        }

        @Override
        public void forEachRelationship(
                final long nodeId,
                final Direction direction,
                final RelationshipConsumer consumer) {
            if (nodeId == 0L) {
                throw error;
            }
            int degree = random.nextInt(maxDegree);
            int[] targets = IntStream.range(0, degree)
                    .map(i -> random.nextInt(nodes))
                    .filter(i -> (long) i != nodeId)
                    .distinct()
                    .toArray();
            for (int target : targets) {
                if (!consumer.accept(nodeId, target)) {
                    break;
                }
            }
        }

        @Override
        public boolean hasRelationshipProperty() {
            return false;
        }

        @Override
        public RelationshipIntersect intersection() {
            throw new UnsupportedOperationException(
                    "org.neo4j.graphalgo.impl.wcc.WCCSafetyTest.FlakyGraph.intersection is not implemented.");
        }

        @Override
        public Collection<PrimitiveLongIterable> batchIterables(final int batchSize) {
            throw new UnsupportedOperationException(
                    "org.neo4j.graphalgo.impl.wcc.WCCSafetyTest.FlakyGraph.batchIterables is not implemented.");
        }

        @Override
        public int degree(final long nodeId, final Direction direction) {
            throw new UnsupportedOperationException(
                    "org.neo4j.graphalgo.impl.wcc.WCCSafetyTest.FlakyGraph.degree is not implemented.");
        }

        @Override
        public long toMappedNodeId(final long nodeId) {
            throw new UnsupportedOperationException(
                    "org.neo4j.graphalgo.impl.wcc.WCCSafetyTest.FlakyGraph.toMappedNodeId is not implemented.");
        }

        @Override
        public long toOriginalNodeId(final long nodeId) {
            throw new UnsupportedOperationException(
                    "org.neo4j.graphalgo.impl.wcc.WCCSafetyTest.FlakyGraph.toOriginalNodeId is not implemented.");
        }

        @Override
        public boolean contains(final long nodeId) {
            throw new UnsupportedOperationException(
                    "org.neo4j.graphalgo.impl.wcc.WCCSafetyTest.FlakyGraph.contains is not implemented.");
        }

        @Override
        public void forEachNode(final LongPredicate consumer) {

        }

        @Override
        public PrimitiveLongIterator nodeIterator() {
            throw new UnsupportedOperationException(
                    "org.neo4j.graphalgo.impl.wcc.WCCSafetyTest.FlakyGraph.nodeIterator is not implemented.");
        }

        @Override
        public NodeOrRelationshipProperties nodeProperties(final String type) {
            throw new UnsupportedOperationException(
                    "org.neo4j.graphalgo.impl.wcc.WCCSafetyTest.FlakyGraph.nodeProperties is not implemented.");
        }

        @Override
        public long getTarget(final long nodeId, final long index, final Direction direction) {
            throw new UnsupportedOperationException(
                    "org.neo4j.graphalgo.impl.wcc.WCCSafetyTest.FlakyGraph.getTarget is not implemented.");
        }

        @Override
        public void forEachRelationship(
                final long nodeId,
                final Direction direction,
                double fallbackValue,
                final RelationshipWithPropertyConsumer consumer) {
            throw new UnsupportedOperationException(
                    "org.neo4j.graphalgo.impl.unionfind.UnionFindSafetyTest.FlakyGraph.forEachRelationship is not implemented.");
        }

        @Override
        public boolean exists(final long sourceNodeId, final long targetNodeId, final Direction direction) {
            throw new UnsupportedOperationException(
                    "org.neo4j.graphalgo.impl.wcc.WCCSafetyTest.FlakyGraph.exists is not implemented.");
        }

        @Override
        public double relationshipProperty(final long sourceNodeId, final long targetNodeId, double fallbackValue) {
            throw new UnsupportedOperationException(
                    "org.neo4j.graphalgo.impl.wcc.WCCSafetyTest.FlakyGraph.relationshipProperty");
        }

        @Override
        public Set<String> availableNodeProperties() {
            throw new UnsupportedOperationException(
                    "org.neo4j.graphalgo.impl.wcc.WCCSafetyTest.FlakyGraph.availableNodeProperties is not implemented.");
        }
    }
}
