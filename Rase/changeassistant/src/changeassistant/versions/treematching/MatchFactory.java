package changeassistant.versions.treematching;

import java.util.Set;

import changeassistant.versions.treematching.measure.ChawatheCalculator;
import changeassistant.versions.treematching.measure.DiceNodeSimilarity;
import changeassistant.versions.treematching.measure.INodeSimilarityCalculator;
import changeassistant.versions.treematching.measure.IStringSimilarityCalculator;
import changeassistant.versions.treematching.measure.LevenshteinCalculator;
import changeassistant.versions.treematching.measure.NGramsCalculator;

public final class MatchFactory {
	
	   public static final int COMMON_TREE_MATCH = 1; 
	
	   public static enum STRING_SIM_MEASURE{NGRAMS, LEVENSHTEIN};
	   public static enum NODE_SIM_MEASURE{CHAWATHE, DICE};
	   public static enum LEAF_MATCHING{FIRST, BEST};
	   
	   private static final int N = 2;	   
	   
	   private static final float COMMON_TREE_MEASURE_THRESHOLD = 1;
	   private static final STRING_SIM_MEASURE LEAF_CALC_STR = STRING_SIM_MEASURE.NGRAMS;	   
//	   private static final STRING_SIM_MEASURE LEAF_CALC_STR = STRING_SIM_MEASURE.LEVENSHTEIN;
	   private static final double LEAF_STR_SIM_THRESHOLD = 0.6;
	   
	   private static final boolean NODE_STR_SIM_ENABLE = true;
	   private static final STRING_SIM_MEASURE NODE_STR_CALC_STR = STRING_SIM_MEASURE.LEVENSHTEIN;
	   private static final double NODE_STR_SIM_THRESHOLD = 0.6;
	   
	   private static final NODE_SIM_MEASURE NODE_CALC_STR = NODE_SIM_MEASURE.CHAWATHE;
	   private static final double NODE_SIM_THRESHOLD = 0.6;
	   
	   private static final LEAF_MATCHING MATCHING = LEAF_MATCHING.BEST;
	   
	   private static final boolean DYNAMIC_THRE_ENABLEMENT = true;
	   private static final double DYNAMIC_THRESHOLD = 0.4;
	   private static final int DYNAMIC_DEPTH = 4;
	   
	   private MatchFactory() {}

	   public static ITreeMatcher getMatcher(Set<NodePair> matchingSet, int exactMatch){
		   if(exactMatch == COMMON_TREE_MATCH){
			   // leaf string matching	  
			   IStringSimilarityCalculator leafCalc = null;
		        if(LEAF_CALC_STR.equals(STRING_SIM_MEASURE.NGRAMS)){
		        	leafCalc = new NGramsCalculator();
		        }else{
		        	leafCalc = new LevenshteinCalculator();
		        }
		        
		        if (leafCalc instanceof NGramsCalculator) {
		            ((NGramsCalculator) leafCalc).setN(N);
		        }
		        
		        double lTh = COMMON_TREE_MEASURE_THRESHOLD;	  
		        
		     // node string matching
		        IStringSimilarityCalculator nodeStringCalc = null;
		        if(NODE_STR_SIM_ENABLE){	        		        	
		        	if(NODE_STR_CALC_STR.equals(STRING_SIM_MEASURE.NGRAMS)){
		        		nodeStringCalc = new NGramsCalculator();
		        		((NGramsCalculator)nodeStringCalc).setN(N);	        		
		        	}else{
		        		nodeStringCalc = new LevenshteinCalculator();
		        	}
		        }
		        double nStTh = COMMON_TREE_MEASURE_THRESHOLD;
		        
		      //node matching
		        INodeSimilarityCalculator nodeCalc = null;
		        if(NODE_CALC_STR.equals(NODE_SIM_MEASURE.CHAWATHE)){
		        	nodeCalc = new ChawatheCalculator();
		        }else{
		        	nodeCalc = new DiceNodeSimilarity(nodeStringCalc, nStTh);
		        }
		        	     
		        nodeCalc.setLeafMatchSet(matchingSet);
		        double nTh = COMMON_TREE_MEASURE_THRESHOLD; 
		        
		      // best match
		        ITreeMatcher result = null;
		        if(MATCHING.equals(LEAF_MATCHING.BEST)){
		        	result = new BestLeafTreeMatcher();
		        }else{
		        	result = new DefaultTreeMatcher();
		        }	       
		        result.init(leafCalc, lTh, nodeStringCalc, nStTh, nodeCalc, nTh);
		
		        result.setMatchingSet(matchingSet);
		        return result;
		   }
		   return null;
	   }
	    /**
	     * Returns an {@link ITreeMatcher} according to specified preference values.
	     * 
	     * @param matchingSet
	     *            in which the matcher stores the match pairs
	     * @return the tree matcher out of specified preference values
	     */
	    public static ITreeMatcher getMatcher(Set<NodePair> matchingSet) {	        

	        // leaf string matching	  	    	
	        IStringSimilarityCalculator leafCalc = null;
	        if(LEAF_CALC_STR.equals(STRING_SIM_MEASURE.NGRAMS)){
	        	leafCalc = new NGramsCalculator();
	        }else{
	        	leafCalc = new LevenshteinCalculator();
	        }
	        
	        if (leafCalc instanceof NGramsCalculator) {
	            ((NGramsCalculator) leafCalc).setN(N);
	        }
	        double lTh = LEAF_STR_SIM_THRESHOLD;	       

	        // node string matching
	        IStringSimilarityCalculator nodeStringCalc = null;
	        if(NODE_STR_SIM_ENABLE){	        		        	
	        	if(NODE_STR_CALC_STR.equals(STRING_SIM_MEASURE.NGRAMS)){
	        		nodeStringCalc = new NGramsCalculator();
	        		((NGramsCalculator)nodeStringCalc).setN(N);	        		
	        	}else{
	        		nodeStringCalc = new LevenshteinCalculator();
	        	}
	        }
	        double nStTh = NODE_STR_SIM_THRESHOLD;
	        
	        //node matching
	        INodeSimilarityCalculator nodeCalc = null;
	        if(NODE_CALC_STR.equals(NODE_SIM_MEASURE.CHAWATHE)){
	        	nodeCalc = new ChawatheCalculator();
	        }else{
	        	nodeCalc = new DiceNodeSimilarity(nodeStringCalc, nStTh);
	        }
	        	     
	        nodeCalc.setLeafMatchSet(matchingSet);
	        double nTh = NODE_SIM_THRESHOLD; 
	        
	        // best match
	        ITreeMatcher result = null;
	        if(MATCHING.equals(LEAF_MATCHING.BEST)){
	        	result = new BestLeafTreeMatcher();
	        }else{
	        	result = new DefaultTreeMatcher();
	        }	       
	        result.init(leafCalc, lTh, nodeStringCalc, nStTh, nodeCalc, nTh);

	        // dynamic threshold
	        if(DYNAMIC_THRE_ENABLEMENT){
	        	result.enableDynamicThreshold(DYNAMIC_DEPTH, DYNAMIC_THRESHOLD);
	        }else{
	        	result.disableDynamicThreshold();
	        }
	        	        
	        result.setMatchingSet(matchingSet);
	        return result;
	    }
}
