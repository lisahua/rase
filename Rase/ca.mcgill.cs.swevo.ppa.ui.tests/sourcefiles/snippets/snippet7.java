IStatus result = StatusInfo.OK_STATUS;
		try {
			String projectName = fFirstPage.getProjectName();

			fCurrProject = ResourcesPlugin.getWorkspace().getRoot().getProject(
					projectName);
			fCurrProjectLocation = fFirstPage.getProjectLocationURI();

			URI realLocation = getRealLocation(projectName,
					fCurrProjectLocation);
			fKeepContent = hasExistingContent(realLocation);

			try {
				createProject(fCurrProject, fCurrProjectLocation);
			} catch (Exception e) {
			}

			initializeBuildPath(JavaCore.create(fCurrProject));
			configureJavaProject();
			List list = getDefaultClassPath(fJavaProject);
			try {
				List<IClasspathEntry> entries = new ArrayList<IClasspathEntry>();
				for (Object object : list) {
					CPListElement elem = (CPListElement) object;
					entries.add(elem.getClasspathEntry());
				}
				fJavaProject.setRawClasspath(entries
						.toArray(new IClasspathEntry[0]), true,
						new NullProgressMonitor());
			} catch (Exception e) {
				e.printStackTrace();
			}

		} catch (Exception e) {
		}
		return result;