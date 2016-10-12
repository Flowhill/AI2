import java.util.*;
import java.lang.Math.*;

public class KMeans extends ClusteringAlgorithm
{
	// Number of clusters
	private int k;

	// Dimensionality of the vectors
	private int dim;
	
	// Threshold above which the corresponding html is prefetched
	private double prefetchThreshold;
	
	// Array of k clusters, class cluster is used for easy bookkeeping
	private Cluster[] clusters;
	
	// This class represents the clusters, it contains the prototype (the mean of all it's members)
	// and memberlists with the ID's (which are Integer objects) of the datapoints that are member of that cluster.
	// You also want to remember the previous members so you can check if the clusters are stable.
	static class Cluster
	{
		float[] prototype;

		Set<Integer> currentMembers;
		Set<Integer> previousMembers;
		  
		public Cluster()
		{
			currentMembers = new HashSet<Integer>();
			previousMembers = new HashSet<Integer>();
		}
	}
	// These vectors contains the feature vectors you need; the feature vectors are float arrays.
	// Remember that you have to cast them first, since vectors return objects.
	private Vector<float[]> trainData;
	private Vector<float[]> testData;

	// Results of test()
	private double hitrate;
	private double accuracy;
	
	public KMeans(int k, Vector<float[]> trainData, Vector<float[]> testData, int dim)
	{
		this.k = k;
		this.trainData = trainData;
		this.testData = testData; 
		this.dim = dim;
		prefetchThreshold = 0.5;
		
		// Here k new cluster are initialized
		clusters = new Cluster[k];
		for (int ic = 0; ic < k; ic++)
			clusters[ic] = new Cluster();
	}
  
  /// Calculates the mean of a cluster
  public float[] calculatePrototype(int clusterNumber)
  {
    float[] prototype = new float[200];
    /// For all members in a certain cluster
    for (int member : clusters[clusterNumber].currentMembers)
    {
      /// Loop over all data of that member and add it to the prototype
      for (int value = 0; value < 200; ++value)
      {
        prototype[value] += trainData.get(member)[value];
      }
    }
    /// Divide the prototype's values by the amount of members to get the mean
    for (int value = 0; value < 200; ++value) prototype[value] /= trainData.size();
    return prototype;
  }

	public boolean train()
	{
	 	//implement k-means algorithm here:
		// Step 1: Select an initial random partioning with k clusters
    // Step 2: Generate a new partition by assigning each datapoint to its closest cluster center
    // Step 3: recalculate cluster centers
    // Step 4: repeat until clustermembership stabilizes
    
    boolean stable = false;
    
    // Step 1
    Random rand = new Random();                         /// Create a random number generator
    for (int i = 0; i < trainData.size(); ++i)          /// Loop from 0 to the last user
    {
      clusters[rand.nextInt(k)].currentMembers.add(i);  /// Add the user to a random cluster
    }
    
    /// For all clusters calculate its mean
    for (int i = 0; i < k; ++i) clusters[k].prototype = calculatePrototype(k);
    
    // Step 4
    while (!stable)
    {
      // Step 2
      /// For every member in the trainingset choose it's closest prototype
      for(int member = 0; member < trainData.size(); ++member)
      {
        /// Variables for saving the current best cluster and current smallest distance
        int bestCluster = -1;
        double smallestDist = Double.POSITIVE_INFINITY;
        for(int currentCluster = 0; currentCluster < k; ++currentCluster) /// For every cluster
        {
          double distance = 0; /// Set the distance at 0, it will be incremented
          /// Calculate the Euclidean distance between the member and the current cluster its prototype
          for (int value = 0; value < 200; ++value)
            distance += Math.pow(trainData.get(member)[value] - clusters[currentCluster].prototype[value], 2);
          distance = Math.sqrt(distance);
          /// Is the calculated distance the smallest? if so: alter the best cluster choice
          if (distance < smallestDist) 
          {
            bestCluster = currentCluster; /// Alter the best cluster choice
            smallestDist = distance;      /// Set the new smallest distance
          }
        }
        clusters[bestCluster].currentMembers.add(member);
      }
      // Step 3
      for(int i = 0; i < k; ++i)                        /// For every cluster
      {
        clusters[i].prototype = calculatePrototype(i);  /// Calculate the prototype
        // Step 4
        /// Check whether the set has changed since the last iteration
        if (!clusters[i].currentMembers.equals(clusters[i].previousMembers)) stable = false;
      }
      /// If membership is not stable set all members to the previous members and clear the current set
      if (!stable)
      {
        for(int i = 0; i < k; ++i)
        {
          clusters[i].previousMembers = clusters[i].currentMembers;
          clusters[i].currentMembers.clear();
        }
      }
    }
		return false;
	}

	public boolean test()
	{
    int memberCluster = -1, hitrate, accuracy;
    
    // iterate along all clients. Assumption: the same clients are in the same order as in the testData
    for (int member = 0; member < trainData.size(); ++member)
    {
      int hits = 0;
      // for each client find the cluster of which it is a member
      for (int currentCluster = 0; currentCluster < k; ++currentCluster) 
        if (clusters[currentCluster].currentMembers.contains(member)) memberCluster = currentCluster;
            
      // get the actual testData (the vector) of this client
      // iterate along all dimensions
      // and count prefetched htmls
      // count number of hits
      // count number of requests
      for (int value = 0; value < 200; ++value)
      {
        if (testData.get(member)[value] == clusters[memberCluster].prototype[value])
        {
          ++hits;
        }
      }

      // set the global variables hitrate and accuracy to their appropriate value
    }
		

		return true;
	}


	// The following members are called by RunClustering, in order to present information to the user
	public void showTest()
	{
		System.out.println("Prefetch threshold=" + this.prefetchThreshold);
		System.out.println("Hitrate: " + this.hitrate);
		System.out.println("Accuracy: " + this.accuracy);
		System.out.println("Hitrate+Accuracy=" + (this.hitrate + this.accuracy));
	}
	
	public void showMembers()
	{
		for (int i = 0; i < k; i++)
			System.out.println("\nMembers cluster["+i+"] :" + clusters[i].currentMembers);
	}
	
	public void showPrototypes()
	{
		for (int ic = 0; ic < k; ic++) {
			System.out.print("\nPrototype cluster["+ic+"] :");
			
			for (int ip = 0; ip < dim; ip++)
				System.out.print(clusters[ic].prototype[ip] + " ");
			
			System.out.println();
		 }
	}

	// With this function you can set the prefetch threshold.
	public void setPrefetchThreshold(double prefetchThreshold)
	{
		this.prefetchThreshold = prefetchThreshold;
	}
}
