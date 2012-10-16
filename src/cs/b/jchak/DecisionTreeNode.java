package cs.b.jchak;

/**
 * This class is used to create objects that represent nodes in a BINARY decision tree
 */
public class DecisionTreeNode 
{
	private String featureName; //Name of the feature this node indicates
	private double lowerLimit;
	private double upperLimit;
	
	private boolean isLeaf; //Is this node a leaf
	private String ClassifiedResult;//If this is the leaf node what is the classified result
	
	private DecisionTreeNode Left, Right;//Left and Right Nodes
	
	/*
	 * This constructor Initializes the class variables with default values
	 * 		By default, this node is not a leaf and contains no children.
	 */
	public DecisionTreeNode()
	{
		Left = Right = null;
		lowerLimit = upperLimit = 0;
		isLeaf = false;
	}
	
	/*
	 * This constructor initializes the feature name of the (internal) node
	 */
	public DecisionTreeNode(String feature_name)
	{
		this.featureName = feature_name;
	}
	
	/*
	 * Converts the current node to a leaf node
	 */
	public void setAsLeaf()
	{
		isLeaf = true;
	}
	
	/*
	 * Returns true if this node is a leaf node
	 */
	public boolean isLeaf()
	{
		return isLeaf;
	}
	
	/*
	 * Sets lower limit of node feature value
	 */
	public void setLowerLimit(double low_value)
	{
		lowerLimit = low_value;
	}
	
	/*
	 * Sets upper limit of node feature value
	 */
	public void setUpperLimit(double high_value)
	{
		upperLimit = high_value;
	}
	
	/*
	 * Returns the value of UpperLImit variable
	 */
	public double getUpperLimit()
	{
		return upperLimit;
	}
	
	/*
	 * Can be used to modify the node's feature name
	 */
	public void setfeatureName(String feature_name)
	{
		featureName = feature_name;
	}
	
	/*
	 * Returns the node's feature name
	 */
	public String getfeatureName()
	{
		return featureName;
	}
	
	/*
	 * Initializes the node's left child
	 */
	public void setLeftNode(DecisionTreeNode left)
	{
		this.Left = left;
	}
	
	/*
	 * Returns the left child of this node
	 */
	public DecisionTreeNode getLeftNode()
	{
		return Left;
	}

	/*
	 * Returns the right child of this node
	 */
	public DecisionTreeNode getRightNode()
	{
		return Right;
	}

	
	/*
	 * Initializes the node's right child
	 */
	public void setRightNode(DecisionTreeNode right)
	{
		this.Right = right;
	}
	
	/*
	 * Sets the classification of a leaf node
	 */
	public void setClassifiedResult(String Class)
	{
		this.ClassifiedResult = Class;
	}
	
	/*
	 * Returns the classification of a leaf node
	 */
	public String getClassification() 
	{
		return ClassifiedResult;
	}

}
