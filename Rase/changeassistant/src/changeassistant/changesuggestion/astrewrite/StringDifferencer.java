package changeassistant.changesuggestion.astrewrite;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import changeassistant.changesuggestion.astrewrite.EditOperation.EditType;
import changeassistant.versions.treematching.edits.ITreeEditOperation.EDIT;
import changeassistant.versions.treematching.measure.LevenshteinCalculator;

public class StringDifferencer extends Differencer<String> {
	
	@Override
	public String apply(String original, EditScript<String> editScripts) {
		String result = null;
		List<String> unchangedStrings = new ArrayList<String>();
		if(isApplicable(original, editScripts, unchangedStrings)){
			List<EditOperation<String>> seos = editScripts.editOperations;
			for(EditOperation<String> seo : seos){
				switch(seo.type){
				case SUBSTITUTE_AFTER: {
									String toChangedString = unchangedStrings.get(seo.anchor);
									toChangedString= toChangedString + seo.nElem;
									unchangedStrings.set(seo.anchor, toChangedString);
									}break;
				case SUBSTITUTE_BEFORE: {
									String toChangedString = unchangedStrings.get(seo.anchor);
									toChangedString = seo.nElem + toChangedString;
									unchangedStrings.set(seo.anchor, toChangedString);
									}break;
				}
			}
			StringBuffer sb = new StringBuffer();
			for(String subStr : unchangedStrings){
				sb.append(subStr);
			}
			result = sb.toString();
		}
		return result;
	}
	
	public void editScript(String left, String right){
		fEditScript = new StringEditScript();
		List<CommonADT<String>> commonSubstrings = longestCommonSubsequence(left, right);//LevenshteinCalculator.calculateEditScript(left, right);
		int leftStart = 0;
		int rightStart = 0;
		int size = commonSubstrings.size();
		String deletedString, insertedString;
		for(int i = 0; i < size; i ++){			
			CommonADT<String> csADT = commonSubstrings.get(i);
			deletedString = left.substring(leftStart, csADT.leftIndex);
			insertedString = right.substring(rightStart, csADT.rightIndex);
			fEditScript.add(new StringEditOperation(EditType.SUBSTITUTE_BEFORE, i, deletedString, insertedString));
			leftStart += deletedString.length() + commonSubstrings.get(i).commonElement.length();
			rightStart+= insertedString.length() + commonSubstrings.get(i).commonElement.length();
		}	
		deletedString = insertedString = "";
		if(leftStart < left.length() - 1){
			deletedString = left.substring(leftStart, left.length() - 1);			
		}
		if(rightStart < right.length() - 1){
			insertedString = right.substring(rightStart, right.length() - 1);			
		}
		if(!deletedString.isEmpty() || !insertedString.isEmpty()){
			fEditScript.add(new StringEditOperation(EditType.SUBSTITUTE_AFTER, size - 1, deletedString,
					insertedString));
		}		
		fEditScript.commonSubElements = commonSubstrings;
	}
	
	@Override
	protected void extractLCS(int[][] b, String l, String r, int i, int j,
			List<CommonADT<String>> lcs) {
		if((i!=0) && (j != 0)){
			if(b[i][j] == DIAG){
				lcs.add(new CommonStringADT(Character.toString(l.charAt(i)), i, j));
				extractLCS(b, l, r, i-1, j-1, lcs);
			}else if(b[i][j] == UP){
				extractLCS(b, l, r, i-1, j, lcs);
			}else{
				extractLCS(b, l, r, i, j-1, lcs);
			}
		}	
	}
	
	@Override
	protected boolean isApplicable(String original,
			EditScript<String> editScripts, List<String> unchangedElements) {
		boolean flag = true;
		List<EditOperation<String>> seos = editScripts.editOperations;	
		int indexOfDeletion;
		for(EditOperation<String> seo : seos){
			switch(seo.type){
			case SUBSTITUTE_BEFORE:
			case SUBSTITUTE_AFTER: if(!seo.elem.isEmpty() && !original.contains(seo.elem)){
										flag = false;
										return flag;
								   }else{
									   indexOfDeletion = original.indexOf(seo.elem);
									   if(indexOfDeletion != 0){
										   unchangedElements.add(original.substring(0, indexOfDeletion));
									   }									   
									   original = 
										   original.substring
										   (original.indexOf(seo.elem) + seo.elem.length());
								   }
								   break;
			default: break;
			}
		}
		if(!original.isEmpty()){
			unchangedElements.add(original);
		}
		return flag;
	}
	
	protected List<CommonADT<String>> longestCommonSubsequence(String left, String right){
		int m = left.length();
		int n = right.length();
		
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
				if(left.charAt(i-1) == right.charAt(j-1)){
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
		List<CommonADT<String>> commonSubstrings = new ArrayList<CommonADT<String>>();
		CommonStringADT csADT = null;
		for(int i = m, j = n; i > 0 & j > 0;){				
			int direction = b[i][j];			
			List<Character> ss = new ArrayList<Character>();
			switch(direction){
			case DIAG: {
				while(b[i][j] == DIAG && i > 0 && j > 0){
					ss.add(0, left.charAt(i - 1));
					i = i-1;
					j = j-1;
				}	
				if(ss.size() >= 2){
					StringBuffer sb = new StringBuffer();//to convert the Char list to String
					for (int k = 0; k < ss.size(); k++) {
						sb.append(ss.get(k));
					}					
					csADT = new CommonStringADT(sb.toString(), i, j);
					commonSubstrings.add(0, csADT);
				}				
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
