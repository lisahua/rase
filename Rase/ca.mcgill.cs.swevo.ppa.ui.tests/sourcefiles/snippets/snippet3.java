IFolder finalFolder = srcFolder;

		if (packages != null) {
			int size = packages.length;
			for (int i = 0; i < size; i++) {
				if (!ValidatorUtil
						.validateEmpty(packages[i], "packages", false)) {
					break;
				}
				IFolder tempFolder = finalFolder.getFolder(packages[i]);
				if (!tempFolder.exists()) {
					tempFolder.create(true, true, new NullProgressMonitor());
				}
				finalFolder = tempFolder;
			}
		}

		return finalFolder;