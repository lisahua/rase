
IFolder parentFolder = (IFolder) file.getParent();
		file.delete(true, new NullProgressMonitor());
		while (isEmpty(parentFolder) && !isSrcFolder(parentFolder)) {
			IFolder tempFolder = (IFolder) parentFolder.getParent();
			parentFolder.delete(true, new NullProgressMonitor());
			parentFolder = tempFolder;
		}