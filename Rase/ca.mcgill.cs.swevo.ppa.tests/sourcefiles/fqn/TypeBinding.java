package p1;

public class TypeBinding implements ITypeBinding {
	org.eclipse.jdt.internal.compiler.lookup.TypeBinding binding;

	
	public ITypeBinding getBound() {
		switch (this.binding.kind()) {
		case Binding.WILDCARD_TYPE:
		case Binding.INTERSECTION_TYPE:
			WildcardBinding wildcardBinding = (WildcardBinding) this.binding;
			if (wildcardBinding.bound != null) {
				return this.resolver.getTypeBinding(wildcardBinding.bound);
			}
			break;
		}
		return null;
	}

}