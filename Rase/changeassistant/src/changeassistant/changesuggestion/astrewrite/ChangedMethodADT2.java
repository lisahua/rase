package changeassistant.changesuggestion.astrewrite;

import changeassistant.versions.comparison.ChangedMethodADT;

public class ChangedMethodADT2 {

	public ChangedMethodADT originalADT;
	public ChangedMethodADT newADT;
	private String originalMethod;
	private String newMethod;
	private STATUS status;
	public enum STATUS{NOT_APPLICABLE, NOT_APPLIED, APPLIED};
	
	public ChangedMethodADT2(ChangedMethodADT oADT, ChangedMethodADT nADT, String oMethod, String nMethod,
			STATUS s){
		this.originalADT = oADT;
		this.newADT = nADT;
		this.originalMethod = oMethod;
		this.newMethod = nMethod;
		this.status = s;
	}
	
	public String getNewVersion(){
		return this.newMethod;
	}
	
	public String getOldVersion(){
		return this.originalMethod;
	}
	
	public STATUS getStatus(){
		return status;
	}
	
	public void setStatus(STATUS s){
		this.status = s;
	}
}
