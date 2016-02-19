String bindingText = "";
		if (binding != null) {
			if (binding instanceof IMethodBinding) {
				IMethodBinding mBinding = (IMethodBinding) binding;
				String methodName = mBinding.getName();
				if (!filterUnknown || methodName == null
						|| !methodName.equals(SnippetUtil.SNIPPET_METHOD)) {
					if (encoded) {
						bindingText = ASTUtil.getHandle(binding, augmented);
					} else {
						bindingText = PPABindingsUtil
								.getFullMethodSignature(mBinding);

					}
					bindings.add(bindingText);
					bindingsType.add(METHOD_TYPE);
					declarations.add(isDeclaration);
					nodes.add(node);
				}
			} else if (binding instanceof IVariableBinding) {
				IVariableBinding vBinding = (IVariableBinding) binding;
				if (vBinding.isField()) {
					if (encoded) {
						bindingText = ASTUtil.getHandle(binding, augmented);
					} else {
						String type = "nil";
						if (vBinding.getType() != null) {
							type = PPABindingsUtil.getTypeString(vBinding
									.getType());
						}

						String decType = "nil";
						if (vBinding.getDeclaringClass() != null) {
							decType = PPABindingsUtil.getTypeString(vBinding
									.getDeclaringClass());
						}
						bindingText = type + " " + decType + ":"
								+ vBinding.getName();
					}
					bindings.add(bindingText);
					bindingsType.add(FIELD_TYPE);
					declarations.add(isDeclaration);
					nodes.add(node);
				}
			} else if (binding instanceof ITypeBinding) {
				ITypeBinding typeBinding = (ITypeBinding) binding;
				bindingText = typeBinding.getName();
				// TODO Change SNIPPET FOR SOMETHING THAT WON't BREAK ANYTHING
				// AND HANDLE
				// SUPERSNIPPET!!!
				if (!filterUnknown
						|| (!bindingText
								.contains(PPATypeRegistry.UNKNWON_CLASS)
								&& !bindingText
										.contains(SnippetUtil.SNIPPET_CLASS) && !bindingText
								.contains(SnippetUtil.SNIPPET_SUPER_CLASS))) {
					if (encoded) {
						bindingText = ASTUtil.getHandle(binding, augmented);
					}
					bindings.add(bindingText);
					bindingsType.add(TYPE_TYPE);
					declarations.add(isDeclaration);
					nodes.add(node);
				}
			}
		}
		return bindingText;