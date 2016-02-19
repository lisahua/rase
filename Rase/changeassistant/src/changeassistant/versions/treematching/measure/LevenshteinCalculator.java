
package changeassistant.versions.treematching.measure;

import java.util.ArrayList;
import java.util.List;

import changeassistant.changesuggestion.astrewrite.CommonStringADT;
import simpack.accessor.string.StringAccessor;
import simpack.measure.sequence.Levenshtein;

public class LevenshteinCalculator implements IStringSimilarityCalculator {

	private static final int UP = 1;
	private static final int LEFT = 2;
	private static final int DIAG = 3;

	
    /**
     * {@inheritDoc}
     */
    public double calculateSimilarity(String left, String right) {
        if (left.equals("") && right.equals("")) {
            return 1.0;
        }
        Levenshtein<String> lm = new Levenshtein<String>(new StringAccessor(left), new StringAccessor(right));
        return lm.getSimilarity();
    }
    
    public static List<CommonStringADT> calculateEditScript(String s, String t){
    	int m = s.length();
    	int n = t.length();
    	int d[][] = new int[m + 1][n + 1];
    	int b[][] = new int[m + 1][n + 1];
    	for(int i = 0; i <= m ; i++){
    		d[i][0] = i;
    	}
    	for(int j = 0; j <= n; j ++){
    		d[0][j] = j;
    	}
    	for(int j = 1; j <= n; j ++){
    		for(int i = 1; i <= m; i ++){
    			if(s.charAt(i-1) == t.charAt(j-1)){
    				d[i][j] = d[i-1][j-1];
    				b[i][j] = DIAG;//this marks that s(i-1)== t(j-1)
    			}else{
    				int min = d[i-1][j - 1] + 1;
    				int direction = DIAG;
    				if(min > d[i-1][j] + 1){
    					min = d[i-1][j] + 1;
    					direction = UP;
    				}else if(min > d[i][j-1]){
    					min = d[i][j-1] + 1;
    					direction = LEFT;
    				}
    				d[i][j] = min;
    				b[i][j] = direction;    			
    			}
    		}
    	}
    	//sequential common subsequence
    	List<CommonStringADT> commonSubstrings = new ArrayList<CommonStringADT>();
		for(int i = m, j = n; i > 0 & j > 0;){				
			int direction = b[i][j];
			switch(direction){
			case DIAG: {
						List<Character> ss = new ArrayList<Character>();
						//to look for the place where substitution is not applied, but just copy
						while(b[i][j] == DIAG && i > 0 && j > 0 && d[i][j] > d[i-1][j-1]){ 
							i-=1;
							j-=1;
						}
						if(i > 0 && j > 0 && d[i][j] == d[i-1][j-1]){
							while(i > 0 && j > 0 &&d[i][j] == d[i-1][j-1]){
								ss.add(0, s.charAt(i-1));
								i-=1;
								j-=1;
							}
							if(ss.size() > 0){
								commonSubstrings.add(0, new CommonStringADT(ss.toString(), i, j));
//								System.out.println(ss.toString());
							}							
						}						
						}break;
			case LEFT: {
						j-=1;
						}break;
			case UP:{
					 i-=1;
					}break;
			}						
		}		
	    return commonSubstrings;
    }
}
