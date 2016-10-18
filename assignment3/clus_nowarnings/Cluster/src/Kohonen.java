import java.util.*;

public class Kohonen extends ClusteringAlgorithm
{
	// Size of clustersmap
	private int n;

	// Number of epochs
	private int epochs;
	
	// Dimensionality of the vectors
	private int dim;
	
	// Threshold above which the corresponding html is prefetched
	private double prefetchThreshold;

	private double initialLearningRate; 
	
	// This class represents the clusters, it contains the prototype (the mean of all it's members)
	// and a memberlist with the ID's (Integer objects) of the datapoints that are member of that cluster.  
	private Cluster[][] clusters;

	// Vector which contains the train/test data
	private Vector<float[]> trainData;
	private Vector<float[]> testData;
	
	// Results of test()
	private double hitrate;
	private double accuracy;
	
	static class Cluster
	{
			float[] prototype;

			Set<Integer> currentMembers;

			public Cluster()
			{
				currentMembers = new HashSet<Integer>();
			}
	}
	
	public Kohonen(int n, int epochs, Vector<float[]> trainData, Vector<float[]> testData, int dim)
	{
		this.n = n;
		this.epochs = epochs;
		prefetchThreshold = 0.5;
		initialLearningRate = 0.8;
		this.trainData = trainData;
		this.testData = testData; 
		this.dim = dim;       
		
		Random rnd = new Random();

		// Here n*n new cluster are initialized
		clusters = new Cluster[n][n];
		for (int i = 0; i < n; i++)  {
			for (int i2 = 0; i2 < n; i2++) {
				clusters[i][i2] = new Cluster();
				clusters[i][i2].prototype = new float[dim];
				for(int i3 = 0; i3 < dim; i3++)	/// Randomly initialize the prototypes
					clusters[i][i2].prototype[i3] = rnd.nextFloat();
			}
		}
	}

	
	public boolean train()
	{

		// Step 1: initialize map with random vectors (A good place to do this, is in the initialisation of the clusters)
		float squareSize, learningRate;
		// Repeat 'epochs' times:
		System.out.print("Progress Bar: [");
		
		for (int currentEpoch = 0; currentEpoch < epochs; ++currentEpoch)
		{
			// Step 2: Calculate the squareSize and the learningRate, these decrease linearly with the number of epochs.
			squareSize = (n / 2) * (1 - (currentEpoch / (float) epochs));
			learningRate = (float) initialLearningRate * (1 - (currentEpoch / (float) epochs));
			
			// Step 3: Every input vector is presented to the map (always in the same order)
		      for(int input = 0; input < trainData.size(); ++input)
		      {
		    	  int BMUx = -1, BMUy = -1;
		    	  double smallestDist = Double.POSITIVE_INFINITY;
		    	  
		    	  for (int i1 = 0; i1 < n; ++i1)
		    	  {
		    		  for (int i2 = 0; i2 < n; ++i2)
		    		  {
		    			  double distance = 0; /// Set the distance at 0, it will be incremented
		    			  /// Calculate the Euclidean distance
		    			  for (int value = 0; value < 200; ++value)
		    				  distance += Math.pow(trainData.get(input)[value] - clusters[i1][i2].prototype[value], 2);
		    			  distance = Math.sqrt(distance);
		    			  
		    			  /// Is the calculated distance the smallest? if so: alter the best cluster choice
		    			  if (distance < smallestDist) 
		    			  {
		    				  BMUy = i1; 	/// Alter the best cluster choice
		    				  BMUx = i2;
		    				  smallestDist = distance;    /// Set the new smallest distance
		    			  }	
		    		  }
		    	  }
		    	  // For each vector its Best Matching Unit is found, and : 
		    	  // Step 4: All nodes within the neighbourhood of the BMU are changed, you don't have to use distance relative learning.
		    	  for (int yCoor = BMUy - (int) squareSize; yCoor < BMUy + squareSize; ++yCoor)
		    	  {
		    		  if (yCoor < 0) yCoor = 0;
		    		  if (yCoor >= n) break;
		    		  
		    		  for (int xCoor = BMUx - (int) squareSize; xCoor < BMUx + squareSize; ++xCoor)
		    		  {
		    			  if (xCoor < 0) xCoor = 0;
		    			  if (xCoor >= n) break;
		    			  for (int value = 0; value < dim; ++value)
		    				  clusters[yCoor][xCoor].prototype[value] = (1 - learningRate) * clusters[yCoor][xCoor].prototype[value] + learningRate * trainData.get(input)[value];
                
		    		  }
		    	  }
		    	  /// If it is the last epoch, definitely assign the member to its BMU
		    	  if (currentEpoch == epochs - 1) clusters[BMUy][BMUx].currentMembers.add(input);
		      }
		      
		      // Since training kohonen maps can take quite a while, presenting the user with a progress bar would be nice
		      if (currentEpoch % Math.max((epochs / 20), 20) == 0) System.out.print("-|");
		}
		System.out.print("-]");
		System.out.println();
		return true;
	}
	
	public boolean test()
	{
		//for (double itrThreshold = 0.1; itrThreshold <= 1; itrThreshold += 0.1)
		//{
			//prefetchThreshold = itrThreshold;
		int prefetches, hits, requests;
    prefetches = hits = requests = 0;
		// iterate along all clients
		for (int input = 0; input < trainData.size(); ++input)
		{
			// for each client find the cluster of which it is a member
			for (int i1 = 0; i1 < n; ++i1)
	    	  {
	    		  for (int i2 = 0; i2 < n; ++i2)
	    		  {
	    			  if (clusters[i1][i2].currentMembers.contains(input))
	    			  {
	    				  float[] prototype = clusters[i1][i2].prototype;
	    			      float[] datapoint = testData.get(input); // get the actual testData (the vector) of this client
	    			      boolean a, b;
	    			      for (int value = 0; value < 200; ++value) // iterate along all dimensions
	    			      {
	    			        a = datapoint[value] == 1;
	    			        b = prototype[value] >= prefetchThreshold;
	    			        if (b) prefetches++;  // and count prefetched htmls
	    			        if (a & b) hits++;    // count number of hits
	    			        if (a) requests++;    // count number of requests
	    			      }  
	    			  }
	    		  }
	    	  }
		}
		// set the global variables hitrate and accuracy to their appropriate value
	    this.hitrate = (double) hits / (double) requests;
	    this.accuracy = (double) hits / (double) prefetches;
      //showTest();
		//}
		return true;
	}


	public void showTest()
	{
		System.out.println("Initial learning Rate=" + initialLearningRate);
		System.out.println("Prefetch threshold=" + prefetchThreshold);
		System.out.println("Hitrate: " + hitrate);
		System.out.println("Accuracy: " + accuracy);
		System.out.println("Hitrate+Accuracy=" + (hitrate + accuracy));
	}
 
 
	public void showMembers()
	{
		for (int i = 0; i < n; i++)
			for (int i2 = 0; i2 < n; i2++)
				System.out.println("\nMembers cluster["+i+"]["+i2+"] :" + clusters[i][i2].currentMembers);
	}

	public void showPrototypes()
	{
		for (int i = 0; i < n; i++) {
			for (int i2 = 0; i2 < n; i2++) {
				System.out.print("\nPrototype cluster["+i+"]["+i2+"] :");
				
				for (int i3 = 0; i3 < dim; i3++)
					System.out.print(" " + clusters[i][i2].prototype[i3]);
				
				System.out.println();
			}
		}
	}

	public void setPrefetchThreshold(double prefetchThreshold)
	{
		this.prefetchThreshold = prefetchThreshold;
	}
}

