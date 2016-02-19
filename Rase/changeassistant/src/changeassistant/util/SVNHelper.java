//package changeassistant.util;
//
//import java.io.File;
//
//import org.tmatesoft.svn.core.SVNDepth;
//import org.tmatesoft.svn.core.SVNDirEntry;
//import org.tmatesoft.svn.core.SVNException;
//import org.tmatesoft.svn.core.SVNURL;
//import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
//import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
//import org.tmatesoft.svn.core.io.SVNRepository;
//import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
//import org.tmatesoft.svn.core.wc.SVNClientManager;
//import org.tmatesoft.svn.core.wc.SVNRevision;
//import org.tmatesoft.svn.core.wc.SVNUpdateClient;
//import org.tmatesoft.svn.core.wc.SVNWCUtil;
//
//public class SVNHelper {
//
//	public String url;
//	public String outputFolder;
//
//	private SVNClientManager clientManager;
//
//	public SVNHelper(String url, String outputFolder) {
//		this.url = url;
//		this.outputFolder = outputFolder;
//
//		SVNRepositoryFactoryImpl.setup();
//
//		DefaultSVNOptions options = SVNWCUtil.createDefaultOptions(true);
//		clientManager = SVNClientManager.newInstance(options);
//	}
//
//	public long checkout(int revision) throws SVNException {
//		File dir = new File(outputFolder + File.separator + revision);
//		SVNRevision initialRevision = SVNRevision.create(revision);
//		SVNUpdateClient updateClient = clientManager.getUpdateClient();
//		updateClient.setIgnoreExternals(false);
//		return updateClient.doCheckout(getRepositoryURL(), dir,
//				SVNRevision.UNDEFINED, initialRevision,
//				SVNDepth.fromRecurse(true), true);
//	}
//
//	public long currentHeadRevision() throws SVNException {
//		long rev = -1;
//		SVNRepository repository = SVNRepositoryFactory.create(SVNURL
//				.parseURIEncoded(url));
//		SVNDirEntry entry = repository.info(".", -1);
//		if (entry != null) {
//			rev = entry.getRevision();
//		}
//		return rev;
//	}
//
//	public static SVNHelper getHelperFromProperties() {
//		String svnURL = PropertyLoader.props.getProperty("SVN_URL");
//		String svnBranch = PropertyLoader.props.getProperty("SVN_Branch");
//		String outputFolder = PropertyLoader.props
//				.getProperty("SVN_Output_URL");
//		return new SVNHelper(svnURL + File.separator + svnBranch, outputFolder);
//	}
//
//	public SVNURL getRepositoryURL() throws SVNException {
//		return SVNURL.parseURIEncoded(url);
//	}
//
//	public static void main(String[] args) {
//		PropertyLoader.load(new File("/Users/mn8247/Software/"
//				+ "workspaceForStaticAnalysis/changeassistant/"
//				+ "config/properties"));
//		SVNHelper helper = getHelperFromProperties();
//		// try {
//		// BufferedReader in = new BufferedReader(
//		// new FileReader(
//		// "/Users/mn8247/Software/workspaceObserve/extract_cloned_changes/tmp/swt/versions.txt"));
//		// String line = null;
//		// int i = 0;
//		// while ((line = in.readLine()) != null) {
//		// try {
//		// i = Integer.valueOf(line);
//		// System.out.println("check out version " + i);
//		// helper.checkout(i);
//		// } catch (NumberFormatException e) {
//		// // TODO Auto-generated catch block
//		// e.printStackTrace();
//		// } catch (SVNException e) {
//		// // TODO Auto-generated catch block
//		// e.printStackTrace();
//		// }
//		// }
//		// in.close();
//		// } catch (FileNotFoundException e) {
//		// // TODO Auto-generated catch block
//		// e.printStackTrace();
//		// } catch (IOException e) {
//		// // TODO Auto-generated catch block
//		// e.printStackTrace();
//		// }
//
//		for (int i = 9676; i < 9680; i++) {
//			try {
//				System.out.println("check out version " + i);
//				helper.checkout(i);
//			} catch (SVNException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//	}
//
// }
