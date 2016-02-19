
IMethodBinding methodBinding = (IMethodBinding) binding;
		handle.append(METHOD_KIND);
		if (augmented) {
			handle.append(AUGMENTED_HANDLE_SEPARATOR);
			if (methodBinding.isAnnotationMember()) {
				handle.append(ANNOTATION_PARAMETER_KIND);
			} else {
				handle.append(METHOD_KIND);
			}
		}
		handle.append(HANDLE_SEPARATOR);
		handle.append(getNonEmptyTypeString(getTypeString(methodBinding
				.getDeclaringClass())));
		handle.append(HANDLE_SEPARATOR);
		handle.append(getNonEmptyName(methodBinding.getName()));
		for (ITypeBinding param : methodBinding.getParameterTypes()) {
			handle.append(HANDLE_SEPARATOR);
			handle.append(getNonEmptyTypeString(getTypeString(param)));
		}