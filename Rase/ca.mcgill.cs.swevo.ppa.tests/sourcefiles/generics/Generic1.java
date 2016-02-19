package p1;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.mapping.Table;

public class Generic1 extends Configuration {
	private Map<Table, ExtendedMappings.ColumnNames> bindingColumnNamePerTable;
	private Map<? extends Object, String> myMap;
	private MyContainer<? extends Column> myContainer;

	@Override
	protected void reset() {
		super.reset();
		bindingColumnNamePerTable = new HashMap<Table, ExtendedMappings.ColumnNames>();
		myMap = new HashMap<String, String>();
		myContainer = new MyContainer<DataColumn>();
	}

}
