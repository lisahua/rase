package p1;

import java.util.Map;
import java.util.HashMap;
import javax.persistence.Entity;

@Entity
public class ParserA {
	private Map<Allo,Holla> entries = new HashMap<Allo,Holla>();
	
	public Map<Allo, Holla> getEntries() {
		return entries;
	}
}
