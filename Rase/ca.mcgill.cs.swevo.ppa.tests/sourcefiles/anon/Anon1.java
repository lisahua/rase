package p1;

public class Anon1 {

	public void m2() {
		final Listener l = new Listener() {
			public void handleEvent(Event e) {
				if (true) {
					Long l = NumberFormat.getInstance().parse(sPos).longValue();
					double pct = (double) (100 - l) / 100;
				}
				if (true) {
					double d = 22.2;
					Double l = new Double(d);
					COConfigurationManager.setParameter((int) (l.doubleValue() * 10000));
				}
			}
		};
	}

	public void m1(Double l) {
		COConfigurationManager.setParameter((int) (l.doubleValue() * 10000));

	}

}