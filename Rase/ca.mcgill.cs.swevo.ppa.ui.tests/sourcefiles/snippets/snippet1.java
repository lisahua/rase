
String mainPath = null;
		try {
			Bundle ppaTestBundle = Platform.getBundle("ca.mcgill.cs.swevo.ppa.ui.tests");
			mainPath = FileLocator.toFileURL(ppaTestBundle.getEntry("sourcefiles/snippets"))
					.getFile();
			snippet1f = new File(mainPath, "java1.java");
			FileInputStream fis = new FileInputStream(snippet1f);
			snippet1 = IOUtils.toString(fis);
			fis.close();
			snippet2f = new File(mainPath, "java2.java");
			fis = new FileInputStream(snippet2f);
			snippet2 = IOUtils.toString(fis);
			fis.close();
			snippet3f = new File(mainPath, "java3.java");
			fis = new FileInputStream(snippet3f);
			snippet3 = IOUtils.toString(fis);
			fis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}