package p1;

import java.util.Set;
import java.util.HashSet;

public enum Modifier {
  PUBLIC(1 << 0),
  PRIVATE(1 << 1),
  PROTECTED(1 << 2),
  
  private final int value;
  
  private Modifier(int value) {
    this.value = value;
  }

  public String toString() {
    return name().toLowerCase();
  }
  
  public static int convertToInt(Set<Modifier> modifiers) {
    int value = 0;
    for (Modifier mod : modifiers) {
      value |= mod.value;
    }
    return value;
  }
  
  public static String convertToString(Set<Modifier> modifiers) {
    return Integer.toString(convertToInt(modifiers));
  }
  
  public static Set<Modifier> convertFromInt(int modifiers) {
    Set<Modifier> mods = new HashSet<Modifier>();
    for (Modifier mod : values()) {
      if ((mod.value & modifiers) == 1) {
        mods.add(mod);
      }
    }
    return mods;
  }
  
  public static Set<Modifier> convertFromString(String modifiers) {
    return convertFromInt(Integer.parseInt(modifiers)); 
  }
}
