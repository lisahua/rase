
boolean isDeclaration = false;

		ASTNode declaration = getDeclarationParent(name.getParent());

		if (declaration != null) {
			if (declaration instanceof AbstractTypeDeclaration) {
				isDeclaration = name == ((AbstractTypeDeclaration) declaration)
						.getName();
			} else if (declaration instanceof MethodDeclaration) {
				isDeclaration = name == ((MethodDeclaration) declaration)
						.getName();
			} else if (declaration instanceof EnumConstantDeclaration) {
				isDeclaration = name == ((EnumConstantDeclaration) declaration)
						.getName();
			} else if (declaration instanceof AnnotationTypeMemberDeclaration) {
				isDeclaration = name == ((AnnotationTypeMemberDeclaration) declaration)
						.getName();
			} else if (declaration instanceof VariableDeclarationFragment) {
				// Covers FieldDeclaration too!
				isDeclaration = name == ((VariableDeclarationFragment) declaration)
						.getName();
			} else if (declaration instanceof SingleVariableDeclaration) {
				isDeclaration = name == ((SingleVariableDeclaration) declaration)
						.getName();
			}
		}

		return isDeclaration;