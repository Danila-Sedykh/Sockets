package org.example.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class IsolationForest {

    private int numTrees;
    private int subsampleSize;
    private List<DecisionTree> trees;
    private static final int MAX_DEPTH = 10;
    private Random random;

    public IsolationForest(int numTrees, int subsampleSize) {
        this.numTrees = numTrees;
        this.subsampleSize = subsampleSize;
        this.trees = new ArrayList<>();
        this.random = new Random();
    }

    public void train(LinkedHashMap<LocalDateTime, Integer> data) {
        for (int i = 0; i < numTrees; i++) {
            List<double[]> subsample = getSubsample(data);
            DecisionTree tree = new DecisionTree();
            tree.buildTree(subsample);
            trees.add(tree);
            //System.out.println("Tree " + (i + 1) + " built with " + subsample.size() + " samples.");
        }
    }

    public double anomalyScore(double[] instance) {
        double averageDepth = 0.0;
        for (DecisionTree tree : trees) {
            averageDepth += tree.depthOfTree(instance);
        }
        averageDepth /= numTrees;
        return Math.pow(2.0, -averageDepth / c(subsampleSize));
    }

    private List<double[]> getSubsample(LinkedHashMap<LocalDateTime, Integer> data) {
        List<double[]> subsample = new ArrayList<>();
        List<Map.Entry<LocalDateTime, Integer>> entries = new ArrayList<>(data.entrySet());
        for (int i = 0; i < subsampleSize; i++) {
            int randomIndex = random.nextInt(entries.size());
            double[] item = new double[]{entries.get(randomIndex).getValue()};
            subsample.add(item);
        }
        return subsample;
    }

    private double c(int n) {
        if (n > 1) {
            return 2 * (Math.log(n - 1) + 0.5772156649) - (2 * (n - 1)) / n;
        } else {
            return 0;
        }
    }

    private static class DecisionTree {
        private TreeNode root;

        public void buildTree(List<double[]> data) {
            root = buildTreeHelper(data, 0);
        }

        private TreeNode buildTreeHelper(List<double[]> data, int currentDepth) {
            if (data.isEmpty() || currentDepth >= MAX_DEPTH) {
                return new TreeNode();
            }

            int numFeatures = data.get(0).length;
            int randomFeatureIndex = new Random().nextInt(numFeatures);

            double minVal = Double.MAX_VALUE;
            double maxVal = -Double.MAX_VALUE;
            for (double[] instance : data) {
                minVal = Math.min(minVal, instance[randomFeatureIndex]);
                maxVal = Math.max(maxVal, instance[randomFeatureIndex]);
            }

            if (minVal == maxVal) {
                return new TreeNode();
            }

            double randomSplitValue = minVal + new Random().nextDouble() * (maxVal - minVal);

            List<double[]> leftData = new ArrayList<>();
            List<double[]> rightData = new ArrayList<>();
            for (double[] instance : data) {
                if (instance[randomFeatureIndex] < randomSplitValue) {
                    leftData.add(instance);
                } else {
                    rightData.add(instance);
                }
            }

            TreeNode leftChild = buildTreeHelper(leftData, currentDepth + 1);
            TreeNode rightChild = buildTreeHelper(rightData, currentDepth + 1);


            return new TreeNode(randomFeatureIndex, randomSplitValue, leftChild, rightChild);
        }

        public double depthOfTree(double[] instance) {
            return root.depthOfTree(instance, 0);
        }

        private class TreeNode {
            private int featureIndex;
            private double splitValue;
            private TreeNode leftChild;
            private TreeNode rightChild;

            public TreeNode() {
                this.featureIndex = -1;
                this.splitValue = Double.NaN;
                this.leftChild = null;
                this.rightChild = null;
            }

            public TreeNode(int featureIndex, double splitValue, TreeNode leftChild, TreeNode rightChild) {
                this.featureIndex = featureIndex;
                this.splitValue = splitValue;
                this.leftChild = leftChild;
                this.rightChild = rightChild;
            }

            public double depthOfTree(double[] instance, int currentDepth) {
                if (featureIndex == -1 || Double.isNaN(splitValue)) {
                    return currentDepth;
                }

                if (instance[0] < splitValue) {
                    return leftChild.depthOfTree(instance, currentDepth + 1);
                } else {
                    return rightChild.depthOfTree(instance, currentDepth + 1);
                }
            }
        }
    }
}
