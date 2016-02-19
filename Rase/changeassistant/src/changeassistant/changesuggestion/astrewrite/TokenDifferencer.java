package changeassistant.changesuggestion.astrewrite;

import java.util.ArrayList;
import java.util.List;

public class TokenDifferencer extends Differencer<List<String>>{

	@Override
	public List<String> apply(List<String> original,
			EditScript<List<String>> editScripts) {
		
		return null;
	}

	@Override
	public void editScript(List<String> left, List<String> right) {
		fEditScript = new TokenEditScript();
		List<CommonADT<List<String>>> commonSubstrings = longestCommonSubsequence(left, right);
		int leftStart = 0;
		int rightStart = 0;
		int size = commonSubstrings.size();
		String deletedString, insertedString;
		
	}
	
	@Override
	protected void extractLCS(int[][] b, List<String> l, List<String> r, int i,
			int j, List<CommonADT<List<String>>> lcs) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected boolean isApplicable(List<String> original,
			EditScript<List<String>> editScripts,
			List<List<String>> unchangedElements) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	protected List<CommonADT<List<String>>> longestCommonSubsequence(
			List<String> left, List<String> right) {
		int m = left.size();
		int n = right.size();
		
		int[][] c = new int[m+1][n+1];
		int[][] b = new int[m+1][n+1];
		
		for(int i = 0; i <=m; i++){
			c[i][0] = 0;
			b[i][0] = 0;		
		}
		for(int i = 0; i <= n; i ++){
			c[0][i] = 0;
			b[0][i] = 0;
		}
		for(int i = 1; i <= m; i ++){
			for(int j = 1; j <= n; j++){
				if(left.get(i-1).equals(right.get(j-1))){
					c[i][j]= c[i-1][j-1] + 1;
					b[i][j] = DIAG;
				}else if(c[i-1][j] >= c[i][j-1]){
					c[i][j] = c[i-1][j];
					b[i][j] = UP;
				}else{
					c[i][j] = c[i][j-1];
					b[i][j] = LEFT;
				}
			}
		}
		
		//sequential common subsequences
		List<CommonADT<List<String>>> commonSubstrings = new ArrayList<CommonADT<List<String>>>();
		CommonADT<List<String>> cADT = null;
		for(int i = m, j = n; i > 0 & j > 0;){				
			int direction = b[i][j];			
			List<String> ss = new ArrayList<String>();
			switch(direction){
			case DIAG: {
				while(b[i][j] == DIAG && i > 0 && j > 0){
					ss.add(0, left.get(i - 1));
					i = i-1;
					j = j-1;
				}
				cADT = new CommonADT<List<String>>(ss, i, j);
				commonSubstrings.add(0, cADT);						
			}break;
			case UP: {
						i-=1;
					 }break;
			case LEFT: {
						j-=1;
						}break;
			}			
		}		
		return commonSubstrings;
	}
}
