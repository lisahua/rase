package changeassistant;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "ChangeAssistant";

	// private static final String LOG_PROPERTIES_FILE =
	// "config/log4j.properties";
	private static final String LOG_PROPERTIES_FILE = "/Users/nm8247/Software/workspace/changeassistant/config/log4j.properties";

	// The shared instance
	private static Activator plugin;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	private void configure() {
		// try {
		// InputStream propertiesInputStream =
		// openBundledFile(LOG_PROPERTIES_FILE);
		// if (propertiesInputStream != null) {
		// Properties props = new Properties();
		// props.load(propertiesInputStream);
		// propertiesInputStream.close();
		//
		// PropertyConfigurator.configure(props);
		// }
		//
		// propertiesInputStream.close();
		// } catch (IOException e) {
		// String errorString = "Error while initializing log properties.";
		// IStatus status =
		// new Status(IStatus.ERROR, getDefault().getBundle().getSymbolicName(),
		// IStatus.ERROR, errorString
		// + e.getMessage(), e);
		// getLog().log(status);
		// }
	}

	public static InputStream openBundledFile(String filePath)
			throws IOException {
		// System.out.println("Platform.getBundle(PLUGIN_ID).getEntry(filePath) = "
		// + Platform.getBundle(PLUGIN_ID).getEntry(filePath));
		// System.out.println("plugin.getBundle().getEntry(filepath) = " +
		// plugin.getBundle().getEntry(filePath));
		// return plugin.getBundle().getEntry(filePath).openStream();
		return new FileInputStream(new File(filePath));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		configure();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	@Override
	protected void initializeImageRegistry(ImageRegistry reg) {
		super.initializeImageRegistry(reg);
		put(reg, "main", "icons/main.gif");
	}

	private void put(ImageRegistry reg, String key, String path) {
		reg.put(key, ImageDescriptor.createFromURL(getBundle().getEntry(
				"./" + path)));
	}
}
